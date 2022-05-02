package test;

import hw1.*;
import hw4.BufferPool;
import hw4.Permissions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import static org.junit.Assert.*;

public class HW4Tests {

	private Catalog c;
	private BufferPool bp;
	private HeapFile hf;
	private TupleDesc td;
	private int tid;
	private int tid2;
	
	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		Database.reset();
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		c.loadSchema("testfiles/test2.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		
		Database.resetBufferPool(BufferPool.DEFAULT_PAGES);

		bp = Database.getBufferPool();
		
		
		tid = c.getTableId("test");
		tid2 = c.getTableId("test2");
	}
	@Test
	public void testReleaseLocks() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
	    bp.transactionComplete(0, true);

	    bp.getPage(1, tid, 0, Permissions.READ_WRITE);
	    bp.transactionComplete(1, true);
	    assertTrue(true);
	}
	
	@Test
	public void testEvict() throws Exception {
		for(int i = 0; i < 50; i++) {
			bp.getPage(0, tid2, i, Permissions.READ_WRITE);
			Tuple t = new Tuple(td);
			t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
			byte[] s = new byte[129];
			s[0] = 2;
			s[1] = 98;
			s[2] = 121;
			t.setPid(i);
			t.setId(0);
			bp.deleteTuple(0, tid2, t);
		}
		try {
			bp.getPage(0, tid2, 50, Permissions.READ_WRITE);
		} catch (Exception e) {
			assertTrue(true);
			return;
		}
		fail("Should have thrown an exception");

	}
	
	@Test
	public void testEvict2() throws Exception {
		for(int i = 0; i < 50; i++) {
			bp.getPage(0, tid2, i, Permissions.READ_WRITE);
		}
		try {
			bp.getPage(0, tid2, 50, Permissions.READ_WRITE);
		} catch (Exception e) {
			fail("Should have evicted a page");
		}
		assertTrue(true);

	}
	
	@Test
	public void testReadLocks() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		if(!bp.holdsLock(0, tid, 0) && !bp.holdsLock(1, tid, 0)) {
			fail("Should be able to acquire multiple read locks");
		}
		assertTrue(true);
	}
	
	@Test
	public void testLockUpgrade() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		if(!bp.holdsLock(0, tid, 0)) {
			fail("Should be able to upgrade locks");
		}
		assertTrue(true);
	}
	
	@Test
	public void testLockUpgrade2() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		if(!bp.holdsLock(0, tid, 0)) {
			fail("Should be able to upgrade locks");
		}
		assertTrue(true);
	}
	
	
	
	@Test
	public void testWriteLocks() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		try {
		bp.getPage(1, tid, 0, Permissions.READ_WRITE);
		} catch(Exception e) {
			
		}
		if(!bp.holdsLock(0, tid, 0) && !bp.holdsLock(1, tid, 0)) {
			fail("Deadlock - should not grant both locks");
		}
		
		if(bp.holdsLock(1, tid, 0)&& bp.holdsLock(0, tid, 0)) {
			fail("Deadlock - one transaction should survive");
		}
		assertTrue(true);
	}
	
	@Test
	public void testReadThenWrite() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		try {
		bp.getPage(1, tid, 0, Permissions.READ_WRITE);
		} catch(Exception e) {
			
		}
		if(!bp.holdsLock(0, tid, 0) && !bp.holdsLock(1, tid, 0)) {
			fail("Deadlock - should not grant both locks");
		}
		
		if(bp.holdsLock(1, tid, 0)&& bp.holdsLock(0, tid, 0)) {
			fail("Deadlock - one transaction should survive");
		}
		assertTrue(true);
	}
	
	@Test
	public void testWriteThenRead() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		try {
		bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		} catch(Exception e) {
			
		}
		if(!bp.holdsLock(0, tid, 0) && !bp.holdsLock(1, tid, 0)) {
			fail("Deadlock - should not grant both locks");
		}
		
		if(bp.holdsLock(1, tid, 0)&& bp.holdsLock(0, tid, 0)) {
			fail("Deadlock - one transaction should survive");
		}
		assertTrue(true);
	}
	
	@Test
	public void testCommit() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		bp.getPage(0, tid, 0, Permissions.READ_WRITE); //acquire lock for the page
		bp.insertTuple(0, tid, t); //insert the tuple into the page
		bp.transactionComplete(0, true); //should flush the modified page
		
		//reset the buffer pool, get the page again, make sure data is there
		bp = Database.resetBufferPool(BufferPool.DEFAULT_PAGES);
		HeapPage hp = bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		Iterator<Tuple> it = hp.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testAbort() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		bp.getPage(0, tid, 0, Permissions.READ_WRITE); //acquire lock for the page
		bp.insertTuple(0, tid, t); //insert the tuple into the page
		bp.transactionComplete(0, false); //should abort, discard changes
		
		//reset the buffer pool, get the page again, make sure data is there
		bp = Database.resetBufferPool(BufferPool.DEFAULT_PAGES);
		HeapPage hp = bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		Iterator<Tuple> it = hp.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testRelease() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
	    bp.releasePage(0, tid, 0);

	    //lock has been released so this should work
	    bp.getPage(1, tid, 0, Permissions.READ_WRITE);
	    assertTrue(true);
	}
	
	@Test
	public void testRelease2() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
	    bp.releasePage(0, tid, 0);

	    //lock has been released so this should work
	    bp.getPage(1, tid, 0, Permissions.READ_WRITE);
	    assertTrue(true);
	}
	
	@Test
	public void testDuplicateReads() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		
		//should be ok since it already has the lock
		assertTrue("should hold read lock", bp.holdsLock(0, tid, 0));
	}
	
	@Test
	public void testDuplicateWrites() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		
		//should be ok since it already has the lock
		assertTrue("should hold write lock", bp.holdsLock(0, tid, 0));
	}
	
	@Test
	public void testhfRemove() throws Exception {

		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		t.setId(0);
		t.setPid(0);
		bp.deleteTuple(0, tid, t);
		
		bp.transactionComplete(0, true);
		
		bp = Database.resetBufferPool(BufferPool.DEFAULT_PAGES);
		HeapPage hp = bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		Iterator<Tuple> it = hp.iterator();
		assertFalse("Deletion failed", it.hasNext());


	}
	
	@Test
	public void testWrongPermissions() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		bp.getPage(0, tid, 0, Permissions.READ_ONLY); //acquire lock for the page
		try {
			bp.insertTuple(0, tid, t); //insert the tuple into the page
			assertTrue(false);
		}catch(Exception e){
			//should not allow the user to write to a lock with read only permissions
			assertTrue(true);
		}
		
	}
	
	/**
	 * This test creates deadlock, then checks to see three things, increasing in order of strictness:
	 * 		1. That at least one lock has survived deadlock
	 * 		2. That whichever transaction survived deadlock still has both of its locks
	 * 				(This is to ensure that locks are rolled back by transaction, not individually)
	 * 		3. That the deadlock is truly resolved, and there are no conflicting locks left
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeadlockResolveAndSurvive() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		bp.getPage(1, tid2, 0, Permissions.READ_WRITE);
		try { // Either this transaction fails
			bp.getPage(0, tid2, 0, Permissions.READ_WRITE);
		} catch (Exception e) {

		}
		try { // Or this transaction fails
			bp.getPage(1, tid, 0, Permissions.READ_WRITE);
		} catch (Exception e) {

		}

		// The rest of the checking is the same
		boolean holdsLock = bp.holdsLock(0, tid, 0) || bp.holdsLock(1, tid2, 0) || bp.holdsLock(0, tid2, 0)
				|| bp.holdsLock(1, tid, 0);
		assertTrue("All locks have been released, but some locks must survive the deadlock", holdsLock);

		boolean t0HasLocks = bp.holdsLock(0, tid, 0) && bp.holdsLock(0, tid2, 0);
		boolean t1HasLocks = bp.holdsLock(1, tid, 0) && bp.holdsLock(1, tid2, 0);
		assertTrue("Either transaction 0 or transaction 1 must still hold a lock on all of the requested pages",
				t0HasLocks || t1HasLocks);

		boolean overlappingLocksTable0 = bp.holdsLock(0, tid, 0) && bp.holdsLock(1, tid, 0);
		boolean overlappingLocksTable1 = bp.holdsLock(0, tid2, 0) && bp.holdsLock(1, tid2, 0);
		assertTrue("There are conflicting write-locks on one of the requested pages",
				!overlappingLocksTable0 && !overlappingLocksTable1);
	}

}
