package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

public class HW1Tests {

	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;
	
	private static final String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
	private Type[] randomTypes(int n) {
		Type[] t = new Type[n];
		for(int i = 0; i < n; i++) {
			if(Math.random() > .5) {
				t[i] = Type.INT;
			}
			else {
				t[i] = Type.STRING;
			}
		}

		return t;
	}

	private String[] randomColumns(int n) {
		String[] c = new String[n];
		for(int i = 0; i < n; i++) {
			int l = (int)(Math.random() * 12 + 2);
			String s = "";
			for (int j = 0; j < l; j++) {
				s += alphabet.charAt((int)(Math.random() * 36));
			}
			c[i] = s;
		}
		return c;
	}

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}
	
	@Test
	public void testGetType() throws Exception{
		for(int i = 0; i < 10; i++) {
			int size = (int)(Math.random() * 15 + 1);
			Type[] t = randomTypes(size);
			String[] c = randomColumns(size);
			TupleDesc td = new TupleDesc(t, c);
			for(int j = 0; j < size; j++) {
				assertTrue("Tuple not recording types properly", td.getType(j) == t[j]);
			}

		}

	}

	@Test
	public void testNameToId() throws Exception{
		for(int i = 0; i < 10; i++) {
			int size = (int)(Math.random() * 15 + 1);
			Type[] t = randomTypes(size);
			String[] c = randomColumns(size);
			TupleDesc td = new TupleDesc(t, c);
			for(int j = 0; j < size; j++) {
				assertTrue("Tuple nameToId not working properly", td.nameToId(c[j]) == j);
			}	
		}
	}
	
	@Test
	public void testNameToId2() throws Exception{	
		try {
			int size = (int)(Math.random() * 15 + 1);
			Type[] t = randomTypes(size);
			String[] c = randomColumns(size);
			TupleDesc td = new TupleDesc(t, c);
			td.nameToId("");
			fail("nameToId should throw an exception for a name that doesn't exist");
		} catch(Exception e) {
			//success
		}
	}

	@Test
	public void testGetSize() throws Exception{
		Type[] t = {Type.INT, Type.INT, Type.INT};
		String[] c = {"", "", ""};
		TupleDesc td = new TupleDesc(t, c);
		assertTrue("TupleDesc getSize() not working", td.getSize() == 12);

		Type[] t2 = {Type.STRING, Type.STRING, Type.STRING};
		String[] c2 = {"", "", ""};
		td = new TupleDesc(t2, c2);

		assertTrue("TupleDesc getSize() not working", td.getSize() == (129 * 3));

	}

	@Test
	public void testTupleToString() throws Exception{
		for(int i = 0; i < 10; i++) {
			int size = (int)(Math.random() * 15 + 1);
			Type[] t = randomTypes(size);
			String[] c = randomColumns(size);
			TupleDesc td = new TupleDesc(t, c);
			for(int j = 0; j < size; j++) {
				assertTrue("TupleDesc toString does not contain all column names", td.toString().contains(c[j]));
			}

		}
	}

	@Test
	public void testTupleGetDesc() throws Exception{
		Type[] t = new Type[] {Type.INT, Type.INT};
		String[] c = new String[] {"a", "bs"};

		TupleDesc td = new TupleDesc(t, c);

		Tuple tup = new Tuple(td);

		assertTrue("Tuple desc from tuple.getDesc() does not match",tup.getDesc().equals(td));
	}

	@Test
	public void testTupleSetField() throws Exception{
		Type[] t = new Type[] {Type.INT, Type.INT};
		String[] c = new String[] {"a", "bs"};

		TupleDesc td = new TupleDesc(t, c);

		Tuple tup = new Tuple(td);

		tup.setField(0, new IntField(new byte[] {1, 1, 1, 1}));

		assertTrue("Tuple setField failed", tup.getField(0).equals(new IntField(new byte[] {1, 1, 1, 1})));
	}

	@Test
	public void testTupleToString2() throws Exception{
		Type[] t = new Type[] {Type.INT, Type.STRING};
		String[] c = new String[] {"a", "bs"};

		TupleDesc td = new TupleDesc(t, c);

		Tuple tup = new Tuple(td);
		byte[] q = new byte[] {1, 1, 1, 1};
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;

		tup.setField(0, new IntField(q));
		tup.setField(1, new StringField(s));
		System.out.println(tup.toString());
		assertTrue("toString() is not properly outputting data", tup.toString().contains("by"));
	}

	@Test
	public void testTableId() throws Exception{

		int tableId = c.getTableId("test");

		assertTrue("Table id not implemented", tableId != 0);
	}


	@Test
	public void testTableName() throws Exception{

		int tableId = c.getTableId("test");

		assertTrue("Catalog does not get table names properly", c.getTableName(tableId).equals("test"));

	}

	@Test
	public void testTableDesc() throws Exception{

		int tableId = c.getTableId("test");

		TupleDesc td = new TupleDesc(new Type[] {Type.INT, Type.STRING}, new String[] {"c1", "c2"});

		assertTrue("Catalog does not retrieve TupleDescs properly", c.getTupleDesc(tableId).equals(td));

	}

	@Test
	public void testhfGetters() throws Exception{

		assertTrue("Unable to get tupleDesc from heap file", hf.getTupleDesc().equals(td));
		
		assertTrue("Heap file reports incorrect number of pages", hf.getNumPages() == 1);
		assertTrue("Heap file cannot read page", hf.readPage(0) != null);
	}

	@Test
	public void testGetAllTuples() throws Exception{
		assertTrue("Heap file contains wrong number of tuples", hf.getAllTuples().size() == 1);
	}

	@Test
	public void testhfMultiPage() throws Exception {
		
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		for(int i = 0; i < 50; i++) {
			hf.addTuple(t);
		}

		assertTrue("HeapFile not reporting the correct number of tuples", hf.getAllTuples().size() == 51);

		assertTrue("HeapFile unable to add page", hf.readPage(1) != null);

		assertTrue("HeapFile unable to add page", hf.getNumPages() == 2);
	}
	
	@Test
	public void testhfWrite() throws Exception {

		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		hf.addTuple(t);

		assertTrue("HeapFile unable to add tuple", hf.getAllTuples().size() == 2);

	}

	@Test
	public void testhfRemove() throws Exception {

		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		//set id and pid of tuple to be deleted
		t.setId(0);
		t.setPid(0);
		hf.deleteTuple(t);

		assertTrue("HeapFile unable to remove tuple", hf.getAllTuples().size() == 0);

	}

	@Test
	public void testHeapPageGetters() throws Exception{
				
		assertTrue("HeapPage Slot Occupied not functioning", hp.slotOccupied(0));

		for(int i = 1; i < 30; i++) {
			assertTrue("HeapPage Slot Occupied not finding empty slot", !hp.slotOccupied(i) );
		}
	}

	@Test
	public void testHPAdd() throws Exception{
				
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		try {
			hp.addTuple(t);
		} catch (Exception e) {
			fail("Exception thrown when adding tuple to heap page");
		}

		Iterator<Tuple> it = hp.iterator();
		assertTrue("HeapPage not finding all tuples", it.hasNext());
		it.next();
		assertTrue("HeapPage not finding all tuples", it.hasNext());
		it.next();
		assertTrue("HeapPage reporting too many tuples", !it.hasNext());

	}

	@Test
	public void testHPRemove() throws Exception{
				
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		//set id and pid of tuple to be deleted
		t.setId(0);
		t.setPid(0);
		try {
			hp.deleteTuple(t);
		} catch (Exception e) {
			fail("Heap Page delete tuple throws unnecessary exception");
		}

		Iterator<Tuple> it = hp.iterator();
		assertTrue("Heap Page does not properly remove tuple", !it.hasNext());

	}

}
