/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {

    public static final int PAGE_SIZE = 4096;

    /**
     * Creates a new heap file in the given location that can accept tuples of the given type
     * @param f location of the heap file
     * @param types type of tuples contained in the file
     */

    private File f;
    private TupleDesc type;
    private int tableId;

    public HeapFile(File f, TupleDesc type) {
        //your code here (v1)
        this.f = f;
        this.type = type;
        this.tableId = f.hashCode();
    }

    public File getFile() {
        //your code here
        return this.f;
    }

    public TupleDesc getTupleDesc() {
        //your code here (v1)
        return this.type;
    }

    /**
     * Creates a HeapPage object representing the page at the given page number.
     * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
     * should be used here.
     * @param id the page number to be retrieved
     * @return a HeapPage at the given page number
     */
    public HeapPage readPage(int id) {
        //your code here
        byte[] pageBytes = new byte[PAGE_SIZE];
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(f, "r");
            long startPos = (long) PAGE_SIZE * id;
            randomAccessFile.seek(startPos);
            randomAccessFile.read(pageBytes);
            randomAccessFile.close();
            return new HeapPage(id, pageBytes, this.tableId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Something wrong!");
        return null;
    }

    /**
     * Returns a unique id number for this heap file. Consider using
     * the hash of the File itself.
     * @return
     */
    public int getId() {
        //your code here (v1)
        return this.tableId;
    }

    /**
     * Writes the given HeapPage to disk. Because of the need to seek through the file,
     * a RandomAccessFile object should be used in this method.
     * @param p the page to write to disk
     */
    public void writePage(HeapPage p) {
        //your code here (v1)
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw");
            randomAccessFile.seek((long) PAGE_SIZE * p.getId());
            randomAccessFile.write(p.getPageData());
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HeapPage addPage(Tuple t) throws Exception {
        HeapPage newPage = new HeapPage(getNumPages(), new byte[PAGE_SIZE], this.tableId);
        newPage.addTuple(t);
        return newPage;
    }

    /**
     * Adds a tuple. This method must first find a page with an open slot, creating a new page
     * if all others are full. It then passes the tuple to this page to be stored. It then writes
     * the page to disk (see writePage)
     * @param t The tuple to be stored
     * @return The HeapPage that contains the tuple
     */
    public HeapPage addTuple(Tuple t) throws Exception {
        //your code here (v1)
        int i = 0;
        HeapPage page = null;
        for (i = 0; i < this.getNumPages(); ++i) {
            page = readPage(i);
            if (!page.hasSlot()) continue;
            page.addTuple(t);
            this.writePage(page);
            return page;
        }
        page = this.addPage(t);
        this.writePage(page);
        return page;
    }

    /**
     * This method will examine the tuple to find out where it is stored, then delete it
     * from the proper HeapPage. It then writes the modified page to disk.
     * @param t the Tuple to be deleted
     */
    public void deleteTuple(Tuple t) throws Exception {
        //your code here (v1)
        HeapPage page = readPage(t.getPid());
        page.deleteTuple(t);
        this.writePage(page);

    }

    /**
     * Returns an ArrayList containing all of the tuples in this HeapFile. It must
     * access each HeapPage to do this (see iterator() in HeapPage)
     * @return
     */
    public ArrayList<Tuple> getAllTuples() {
        //your code here (v1)
        ArrayList<Tuple> tuples = new ArrayList<>();
        for (int i = 0; i < this.getNumPages(); ++i) {
            tuples.addAll(readPage(i).getTuples());
        }
        return tuples;
    }

    /**
     * Computes and returns the total number of pages contained in this HeapFile
     * @return the number of pages
     */
    public int getNumPages()  {
        //your code here (v1)
        int pageNum = 0;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.f, "r");
            pageNum = (int) randomAccessFile.length() / PAGE_SIZE;
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageNum;
    }
}
