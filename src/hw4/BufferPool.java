package hw4;

import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

import java.util.*;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    class LRUCache<T1, T2> {
        class Node {
            T1 key;
            T2 val;
            Node next;
            Node prev;
            boolean dirty;

            public Node(T1 key, T2 val) {
                this.key = key;
                this.val = val;
                this.dirty = false;
            }
        }

        private Map<T1, Node> cache;
        private Node head;
        private Node tail;


        public LRUCache() {
            cache = new HashMap<>();
            head = new Node(null, null);
            tail = new Node(null, null);
            head.next = tail;
            tail.prev = head;
        }

        public T2 get(T1 key) {
            if (!cache.containsKey(key)) return null;
            // put the node to the front of the list
            use(cache.get(key));
            return cache.get(key).val;
        }

        public int size() {
            return cache.size();
        }

        /*
         * Put an entry into the LRU, evict the last used non-dirty node from the cache if needed.
         * @return: If the cache is full and all nodes are dirty, return false.
         * */
        public boolean put(T1 key, T2 value) {
            if (cache.containsKey(key)) {
                // update the key-value pair
                cache.get(key).val = value;
                use(cache.get(key));
            } else {
                Node newNode = new Node(key, value);
                cache.put(key, newNode);
                addFirst(newNode);
            }
            return true;
        }

        public Node remove() {
            Node evicted = tail.prev;
            while (evicted != head) {
                if (!evicted.dirty) break;
                evicted = evicted.prev;
            }
            // No node is non-dirty, put failed.
            if (evicted == head) return null;
            cache.remove(evicted.key);
            remove(evicted);
//            System.out.println(((TablePage)evicted.key).pid);
            return evicted;
        }

        public void setDirtyPage(T1 pid, boolean dirty) {
            cache.get(pid).dirty = dirty;
        }
        private void addFirst(Node node) {
            Node headNext = head.next;
            head.next = node;
            node.prev = head;
            node.next = headNext;
            headNext.prev = node;
        }

        private void remove(Node node) {
            Node prevNode = node.prev, nextNode = node.next;
            prevNode.next = nextNode;
            nextNode.prev = prevNode;
        }

        private void use(Node node) {
            remove(node);
            addFirst(node);
        }

        public boolean containsKey(T1 key) {
            return this.cache.containsKey(key);
        }
    }

    /*
    * Unique identifier for a heap page from a table (heap file).
    * */
    class TablePage {
        private int tableid;
        private int pid;

        public TablePage(int tableid, int pid) {
            this.tableid = tableid;
            this.pid = pid;
        }
        // We need to use TablePage objects as keys in the HashMap, so equals() and hashCode() are required
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof TablePage)) return false;
            TablePage tp = (TablePage) obj;
            return tp.tableid == this.tableid && tp.pid == this.pid;
        }

        @Override
        public int hashCode() {
            return tableid * PAGE_SIZE + pid;
        }
    }

    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    private LRUCache<TablePage, HeapPage> buffer;
    private int size;
//    private Catalog catalog;

    // key : tableID + PageID , value: set of transactionID
    private Map<TablePage, Set<Integer>> readLocks;
    // key: tableID + PageID, value: TransactionID
    private Map<TablePage, Integer> writeLocks;

//    private Map<Integer, Integer> tidToPid;
//
//    private Map<Integer, Integer> pidToTableId; // key: page id, value = table id
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
        buffer = new LRUCache<>();
        readLocks = new HashMap<>();
        writeLocks = new HashMap<>();
        size = numPages;
//        pidToTableId = new HashMap<>();
//        tidToPid = new HashMap<>();

    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
        // your code here
        TablePage tp = new TablePage(tableId, pid);
        HeapFile hf = Database.getCatalog().getDbFile(tableId);
        // 0. If this page is not locked, acquire a read/write lock.
        if (!this.writeLocks.containsKey(tp) && !this.readLocks.containsKey(tp)) {
            if (perm.equals(Permissions.READ_ONLY)) {
                this.readLocks.put(tp, new HashSet<>(Collections.singletonList(tid)));
            } else if (perm.equals(Permissions.READ_WRITE)) {
                this.writeLocks.put(tp, tid);
            }
            return addPageToBuffer(tp, hf);
        }
        // 1. Check if it this page from this table is locked by write lock by another transaction.
        else if (this.writeLocks.containsKey(tp) && this.writeLocks.get(tp) != tid) {
            // Abort
            transactionComplete(tid, false);
            return null;
        }

        // 2. If this transaction has required write lock
        else if (this.writeLocks.containsKey(tp) && this.writeLocks.get(tp) == tid)
            return addPageToBuffer(tp, hf);
        // 3. If this transaction has required read lock
        else if (this.readLocks.containsKey(tp) && this.readLocks.get(tp).contains(tid)) {
            // Upgrade the read lock to write lock if there is only one read lock of this page.
            if (perm.equals(Permissions.READ_WRITE)) {
                if (this.readLocks.get(tp).size() == 1) {
                    this.readLocks.remove(tp);
                    this.writeLocks.put(tp, tid);
                } else {
                    transactionComplete(tid, false);
                    return null;
                }
            }
            return addPageToBuffer(tp, hf);
        }
        // If this page has been locked by other transactions (read)
        else if (this.readLocks.containsKey(tp) && !this.readLocks.get(tp).contains(tid)) {
            if (perm.equals(Permissions.READ_ONLY)) {
                this.readLocks.get(tp).add(tid);
            } else if (perm.equals(Permissions.READ_WRITE)) {
                // Abort
                transactionComplete(tid, false);
                return null;
            }
            return addPageToBuffer(tp, hf);
        }
        return null;
    }

    private HeapPage addPageToBuffer(TablePage tp, HeapFile hf) throws Exception {
        if (buffer.size() == size) {
            evictPage();
        }
        // If this page is not in cache, push it into the cache.
        if (!buffer.containsKey(tp))
            buffer.put(tp, hf.readPage(tp.pid));

        return buffer.get(tp);
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableId the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    //done
    public void releasePage(int tid, int tableId, int pid) {
        // your code here
        TablePage tp = new TablePage(tableId, pid);
        if (writeLocks.containsKey(tp)) {
            if (writeLocks.get(tp) == tid) {
                writeLocks.remove(tp);
            }
        }
        if (readLocks.containsKey(tp)) {
            readLocks.get(tp).remove(tid);
            if (readLocks.get(tp).size() == 0) {
                readLocks.remove(tp);
            }
        }

    }

    /** Return true if the specified transaction has a lock on the specified page */
    //done
    public boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
        TablePage tp = new TablePage(tableId, pid);
        if (writeLocks.containsKey(tp)){
            return writeLocks.get(tp) == tid;
        }
        else if (readLocks.containsKey(tp)) {
            return readLocks.get(tp).contains(tid);
        }
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(int tid, boolean commit) throws Exception {
        // your code here
        boolean writeLock = false;
        boolean readLock = false;
        TablePage tp = null;
        for (TablePage key: writeLocks.keySet()) {
            if (writeLocks.get(key) == tid) {
                tp = key;
                writeLock = true;
                break;
            }
        }
        for (TablePage key: readLocks.keySet()) {
            if (readLocks.get(key).contains(tid)) {
                tp = key;
                readLock = true;
                break;
            }
        }
        if (!readLock && !writeLock) {
            throw new Exception("No such transaction");
        }

        // If commit, write to the DB file
        if (commit) {
            flushPage(tp.tableid, tp.pid);
        }
        // If abort, rollback
        else {
            this.buffer.put(tp, Database.getCatalog().getDbFile(tp.tableid).readPage(tp.pid));
        }

        // Release all locks of this transaction
        for (Map.Entry<TablePage, Integer> entry: writeLocks.entrySet()) {
            if (entry.getValue().equals(tid))
                releasePage(tid, entry.getKey().tableid, entry.getKey().pid);
        }
        for (Map.Entry<TablePage, Set<Integer>> entry: readLocks.entrySet()) {
            if (entry.getValue().contains(tid)) {
                readLocks.get(entry.getKey()).remove(tid);
                if (readLocks.get(entry.getKey()).size() == 0) readLocks.remove(entry.getKey());
            }
        }
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
        TablePage tp = null;
        for (Map.Entry<TablePage, Integer> entry: writeLocks.entrySet()) {
            if (entry.getValue().equals(tid)) tp = entry.getKey();
        }
        if (tp == null) throw new Exception("You should require write lock before insertion\n");
        this.buffer.get(tp).addTuple(t);
        // Set this page in the buffer is dirty (modified).
        this.buffer.setDirtyPage(tp, true);
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
        TablePage tp = new TablePage(tableId, t.getPid());
        if (!writeLocks.containsKey(tp) || !writeLocks.get(tp).equals(tid))
            throw new Exception("You should require write lock before deletion.\n");
        this.buffer.get(tp).deleteTuple(t);
        this.buffer.setDirtyPage(tp, true);

    }

    private synchronized void flushPage(int tableId, int pid) {
        // your code here
        HeapFile hf = Database.getCatalog().getDbFile(tableId);
        hf.writePage(this.buffer.get(new TablePage(tableId, pid)));
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized void evictPage() throws Exception {
        // your code here
        if (this.buffer.remove() == null)
            throw new Exception("No non-dirty page in the buffer.\n");
    }

}
