package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;
import hw2.AggregateOperator;
import hw2.Query;
import hw2.Relation;

public class HW2Tests {

	private HeapFile testhf;
	private TupleDesc testtd;
	private HeapFile ahf;
	private TupleDesc atd;
	private Catalog c;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		testtd = c.getTupleDesc(tableId);
		testhf = c.getDbFile(tableId);
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");
		
		tableId = c.getTableId("A");
		atd = c.getTupleDesc(tableId);
		ahf = c.getDbFile(tableId);
		
		c.loadSchema("testfiles/B.txt");
		c.loadSchema("testfiles/S.txt");
	}
	
	@Test
	public void testSelectR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ar = ar.select(0, RelationalOperator.EQ, new IntField(530));
		
		assertTrue("Should be 5 tuples after select operation", ar.getTuples().size() == 5);
		assertTrue("select operation does not change tuple description", ar.getDesc().equals(atd));
	}
	
	@Test
	public void testSelectStringR() {
		Relation testr = new Relation(testhf.getAllTuples(), testtd);
		testr = testr.select(1, RelationalOperator.EQ, new StringField("hi"));
		
		assertTrue("Should be 1 tuple after select operation", testr.getTuples().size() == 1);
		assertTrue("select operation does not change tuple description", testr.getDesc().equals(testtd));
	}
	
	@Test
	public void testProjectR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(1);
		ar = ar.project(c);
		assertTrue("Projection should remove one of the columns, making the size smaller", ar.getDesc().getSize() == 4);
		assertTrue("Projection should not change the number of tuples", ar.getTuples().size() == 8);
		assertTrue("Projection should retain the column names", ar.getDesc().getFieldName(0).equals("a2"));
	}
	
	@Test
	public void testProjectRemoveAllColumnsR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		ar = ar.project(c);
		assertTrue("Projection that removes all columns should have size 0", ar.getDesc().getSize() == 0);
		assertTrue("Projection that removes all columns should have 0 tuples", ar.getTuples().size() == 0);
	
	}
	
	@Test
	public void testProjectWrongColumnR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(2);
		c.add(3);
		try {
			ar = ar.project(c);
			fail("Should throw an exception due to the invalid column number");
		} catch(IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testJoinR() {
		Relation tr = new Relation(testhf.getAllTuples(), testtd);
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		tr = tr.join(ar, 0, 0);
		
		assertTrue("There should be 5 tuples after the join", tr.getTuples().size() == 5);
		assertTrue("The size of the tuples should reflect the additional columns from the join", tr.getDesc().getSize() == 141);
	}
	
	@Test
	public void testRenameR() throws Exception {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		
		ArrayList<Integer> f = new ArrayList<Integer>();
		ArrayList<String> n = new ArrayList<String>();
		
		f.add(0);
		n.add("b1");
		
		ar = ar.rename(f, n);
		
		assertTrue("Rename should not remove any tuples", ar.getTuples().size() == 8);
		assertTrue("Rename did not go through", ar.getDesc().getFieldName(0).equals("b1"));
		assertTrue("Rename changed the wrong column", ar.getDesc().getFieldName(1).equals("a2"));
		assertTrue("Rename should not add or remove any columns", ar.getDesc().getSize() == 8);
		
	}
	
	@Test
	public void testRenameNullR() throws Exception {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		
		ArrayList<Integer> f = new ArrayList<Integer>();
		ArrayList<String> n = new ArrayList<String>();
		
		f.add(0);
		n.add("");
		
		ar = ar.rename(f, n);
		
		assertTrue("Rename should not remove any tuples", ar.getTuples().size() == 8);
		assertFalse("Rename should not allow null input", ar.getDesc().getFieldName(0).equals(""));
		assertFalse("Rename should not allow null input", ar.getDesc().getFieldName(1).equals(""));
		assertTrue("Rename should not add or remove any columns", ar.getDesc().getSize() == 8);
		
	}
	
	@Test
	public void testRenameDuplicateR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		
		ArrayList<Integer> f = new ArrayList<Integer>();
		ArrayList<String> n = new ArrayList<String>();
		
		f.add(0);
		n.add(ar.getDesc().getFieldName(1));
		
		try {
			ar = ar.rename(f, n);
			fail("Column name already exists");
		} catch(Exception e) {
			//test passes, should throw an exception
			assertTrue(true);
		}
	}
	
	@Test
	public void testAggregateR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(1);
		ar = ar.project(c);
		ar = ar.aggregate(AggregateOperator.SUM, false);
		
		assertTrue("The result of an aggregate should be a single tuple", ar.getTuples().size() == 1);
		IntField agg = (IntField) ar.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", agg.getValue() == 36);
	}
	
	@Test
	public void testGroupByR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ar = ar.aggregate(AggregateOperator.SUM, true);
		
		assertTrue("There should be four tuples after the grouping", ar.getTuples().size() == 4);
	}
	
	@Test
	public void testAggregateMaxR()  {
		Relation rel = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> x = new ArrayList<Integer>();
		x.add(0);
		rel = rel.project(x);
		Relation relMax = rel.aggregate(AggregateOperator.MAX, false);
		assertTrue("Aggregator MAX is incorrect (rows)", relMax.getTuples().size() == 1);
		IntField testAgg = (IntField) relMax.getTuples().get(0).getField(0);
		assertTrue("Aggregate MAX is incorrect (value)", testAgg.getValue() == 530);
		
	}
	
	@Test
	public void testAggregateMinR()  {
		Relation rel = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> x = new ArrayList<Integer>();
		x.add(0);
		rel = rel.project(x);
		
		Relation relMin = rel.aggregate(AggregateOperator.MIN, false);
		assertTrue("Aggregator MIN is incorrect (rows)", relMin.getTuples().size() == 1);
		IntField testAgg = (IntField) relMin.getTuples().get(0).getField(0);
		assertTrue("Aggregate MIN is incorrect", testAgg.getValue() == 1);
	}
	
	@Test
	public void testAggregateAVGR() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(1);
		ar = ar.project(c);
		ar = ar.aggregate(AggregateOperator.AVG, false);
		
		assertTrue("Aggregates return one value", ar.getTuples().size() == 1);
		IntField agg = (IntField) ar.getTuples().get(0).getField(0);
		assertTrue("Result of AVG should be an int", agg.getValue() == 4); // Result should be an int		
	}
	
	@Test
	public void testAggregateStringR() {
		Type[] types = {Type.STRING};
		String[] fields = {"stringcol"};
		
		TupleDesc td = new TupleDesc(types, fields);
		
		Tuple t1 = new Tuple(td);
		Tuple t2 = new Tuple(td);
		Tuple t3 = new Tuple(td);
		
		t1.setField(0, new StringField("a"));
		t2.setField(0, new StringField("k"));
		t3.setField(0, new StringField("e"));
		
		ArrayList<Tuple> tups = new ArrayList<Tuple>();
		tups.add(t1);
		tups.add(t2);
		tups.add(t3);
		
		Relation r = new Relation(tups, td);
		
		Relation max = r.aggregate(AggregateOperator.MAX, false);
		assertTrue("Aggregate should contain one tuple", max.getTuples().size() == 1);
		StringField sf = (StringField) max.getTuples().get(0).getField(0);
		assertTrue("String max should return latest string", sf.getValue().equals("k"));
		
		Relation min = r.aggregate(AggregateOperator.MIN, false);
		assertTrue("Aggregate should contain one tuple", max.getTuples().size() == 1);
		sf = (StringField) min.getTuples().get(0).getField(0);
		assertTrue("String max should return earliest string", sf.getValue().equals("a"));
	}
	
	@Test
	public void testRelationToString() {
		Type[] types = {Type.INT, Type.STRING, Type.INT};
		String[] fields = {"a", "b", "c"};
		TupleDesc td = new TupleDesc(types, fields);
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		for (int i = 0; i < 5; i++){
			Tuple t = new Tuple(td);
			t.setField(0, new IntField((int)(i*Math.random())));
			t.setField(1, new StringField("hello" + i));
			t.setField(2, new IntField((int)(i*Math.random())));
			tuples.add(t);
		}
		Relation r = new Relation(tuples, td);
		assertTrue("Relation toString does not contain the tuple desc", r.toString().contains(td.toString()));
		for (Tuple t : tuples){
			assertTrue("Relation toString does not contain all tuples", r.toString().contains(t.toString()));
		}
	}
	
	@Test
	public void testSimpleQ() {
		Query q = new Query("SELECT a1, a2 FROM A");
		Relation r = q.execute();
		
		assertTrue("Select should not change the number of tuples", r.getTuples().size() == 8);
		assertTrue("Query does not add or remove column", r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testSelectQ() {
		Query q = new Query("SELECT a1, a2 FROM A WHERE a1 = 530");
		Relation r = q.execute();
		
		assertTrue("Result of query should contain 5 tuples", r.getTuples().size() == 5);
		assertTrue("Where clause does not add or remove columns", r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testSelectRangeQ() {
		Query q = new Query("SELECT a1, a2 FROM A WHERE a2 > 3");
		Relation r = q.execute();

		assertTrue("There should be five tuples after the selecting", r.getTuples().size() == 5);
		assertTrue("Query does not add or remove column", r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testQuerySelectUnprojectedField() {
		Query q = new Query("SELECT a2 FROM A WHERE a1 = 530");
		Relation r = q.execute();
		
		assertTrue("Projection should remove a column", r.getDesc().getSize() == 4);
		assertTrue("Projection removed the wrong column", r.getDesc().getFieldName(0).equals("a2"));
		assertTrue("Result of query should contain 5 tuples", r.getTuples().size() == 5);
	}
	
	@Test
	public void testProjectQ() {
		Query q = new Query("SELECT a2 FROM A");
		Relation r = q.execute();
		
		assertTrue("Projection should remove a column", r.getDesc().getSize() == 4);
		assertTrue("Projection should not remove tuples", r.getTuples().size() == 8);
		assertTrue("Projection removed the wrong column", r.getDesc().getFieldName(0).equals("a2"));
	}
	
	@Test
	public void testJoinQ() {
		Query q = new Query("SELECT c1, c2, a1, a2 FROM test JOIN A ON test.c1 = a.a1");
		Relation r = q.execute();
		
		assertTrue("Join should return 5 tuples", r.getTuples().size() == 5);
		assertTrue("Tuple size should increase since columns were added with join", r.getDesc().getSize() == 141);
	}
	
	@Test
	public void testJoinOnStringFieldsQ() {
		// This test makes use of the S.txt,dat,dat.bak files in my testFiles folder.
		Query q = new Query("SELECT * FROM test JOIN S ON S.s1 = test.c2");
		Relation r = q.execute();
		// Verify that only one tuple was matched
		assertEquals("Should only be 1 tuple in the resulting relation.", 1, r.getTuples().size());
		String matchString = "hi";
		Tuple t = r.getTuples().get(0);
		Field sField = t.getField(t.getDesc().nameToId("s1"));
		Field testField = t.getField(t.getDesc().nameToId("c2"));
		assertEquals("Column s1 of tuple should be of type string.", sField.getType(), Type.STRING);
		assertEquals("Column c2 of tuple should be of type string.", testField.getType(), Type.STRING);
		assertEquals("Value of column s1 should be match given string.", matchString, ((StringField) sField).getValue());
		assertEquals("Value of column s1 should match value of column c2, that was the WHERE clause of the JOIN.", ((StringField) sField).getValue(), ((StringField) testField).getValue());
	}
	
	
	@Test
	public void testAggregateQ() {
		Query q = new Query("SELECT SUM(a2) FROM A");
		Relation r = q.execute();
		
		assertTrue("Aggregations should result in one tuple",r.getTuples().size() == 1);
		IntField agg = (IntField) r.getTuples().get(0).getField(0);
		assertTrue("Result of sum aggregation is 36", agg.getValue() == 36);
	}
	
	@Test
	public void testGroupByQ() {
		Query q = new Query("SELECT a1, SUM(a2) FROM A GROUP BY a1");
		Relation r = q.execute();
		
		assertTrue("Tuple size should remain unchanged after aggregating", r.getDesc().getSize() == 8);
		assertTrue("Should be 4 groups from this query",r.getTuples().size() == 4);
		ArrayList<Integer> groups = new ArrayList<Integer>();
		ArrayList<Integer> sums = new ArrayList<Integer>();
		
		for(Tuple t : r.getTuples()) {
			groups.add(((IntField)t.getField(0)).getValue());
			sums.add(((IntField)t.getField(1)).getValue());
		}
		
		assertTrue("Missing grouping", groups.contains(1));
		assertTrue("Missing grouping", groups.contains(530));
		assertTrue("Missing grouping", groups.contains(2));
		assertTrue("Missing grouping", groups.contains(3));
		
		assertTrue("Missing sum", sums.contains(2));
		assertTrue("Missing sum", sums.contains(20));
		assertTrue("Missing sum", sums.contains(6));
		assertTrue("Missing sum", sums.contains(8));
	}
	
	@Test
	public void testSelectAllQ() {
		Query q = new Query("SELECT * FROM A");
		Relation r = q.execute();
		
		assertTrue("should return all 8 tuples", r.getTuples().size() == 8);
		assertTrue("number of columns should be unchanged", r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testCountAggregationQ() {
		Query q = new Query("SELECT COUNT(*) FROM A");
		Relation r = q.execute();
		assertTrue("The count is just one number", r.getTuples().size() == 1);
		
		IntField agg = (IntField) r.getTuples().get(0).getField(0);
		assertTrue("The number of tuples should be 8", agg.getValue() == 8);
	}
	
	@Test
	public void testSelectNoneApplicableQ() {
		Query q = new Query("SELECT a1, a2 FROM A WHERE a1 = 233");
		Relation r = q.execute();
		
		assertTrue(r.getTuples().size() == 0);
		assertTrue(r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testSelectGreaterQ() {
		Query q = new Query("SELECT a1, a2 FROM A WHERE a2 > 5");
		Relation r = q.execute();
		
		assertTrue("Wrong number of rows", r.getTuples().size() == 3);
		assertTrue("Wrong columns", r.getDesc().getSize() == 8);
	}
	
	@Test
	public void testJoinEmptySetQ() {
		// This test verifies that a Join between two tables that have nothing in
		// common on their specified fields gives you a relation with nothing except 
		// the properly defined tuple description.
		// First set up the query screen to project all columns from the defined tables,
		// but select to join the two on columns that have nothing in common.
		Query quer = new Query("SELECT a1, a2, c1, c2 FROM A JOIN test ON test.c1 = a.a2");
		// Execute the query.
		Relation rel = quer.execute();
		
		// Verify the results.
		assertTrue("You shouldn't have retrieved any tuples!", rel.getTuples().size() == 0);
		assertTrue("You still should have returned a proper-sized tuple description!", rel.getDesc().getSize() == 141);
	}
	
	@Test
	public void testMultiJoinQuery() {
		Query q = new Query("SELECT c1, c2, a1, a2, b1, b2 FROM test JOIN A ON test.c1 = A.a1 JOIN B ON A.a1 = B.b1");
		Relation r = q.execute();
		
		assertTrue("Multi-join should return 25 tuples", r.getTuples().size() == 25);
		assertTrue("Tuples should have 5 ints and 1 string", r.getDesc().getSize() == 149);
	}

}
