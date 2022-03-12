/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw2;

import hw1.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

    private ArrayList<Tuple> tuples;
    private TupleDesc td;

    public Relation(ArrayList<Tuple> l, TupleDesc td) {
        //your code here
        this.tuples = l;
        this.td = td;
//        System.out.println("lololol" + l);
    }

    /**
     * This method performs a select operation on a relation
     * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
     * @param op the comparison operator
     * @param operand a constant to be compared against the given column
     * @return
     */
    public Relation select(int field, RelationalOperator op, Field operand) {
        //your code here
        if (op == null || field < 0 || field >= this.td.numFields() || operand == null)
            throw new InvalidParameterException("Invalid Parameters!");
        ArrayList<Tuple> tuples = new ArrayList<>();
        for (Tuple tuple: this.tuples) {
            if (tuple.getField(field).compare(op, operand)) tuples.add(tuple);
        }
        return new Relation(tuples, this.td);
    }

    /**
     * This method performs a rename operation on a relation
     * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
     * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
     * @return
     */
    public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) throws Exception {
        //your code here
        String[] newNames = this.td.getFields();
        // {Field names}
        Set<String> nameSet = new HashSet<>(Arrays.asList(this.getDesc().getFields()));
        for (Integer field: fields) {
            if (field == null || field < 0 || field >= this.td.numFields()
                    || names.get(field) == null || names.get(field).equals("")) continue;
            if (nameSet.contains(names.get(field))) throw new Exception("Duplicated field name");
            // update nameSet with the updated field name
            nameSet.remove(this.getDesc().getFieldName(field));
            nameSet.add(names.get(field));
            newNames[field] = names.get(field);
        }
        TupleDesc td = this.td;
        td.setFields(newNames);
        return new Relation(this.tuples, td);
    }

    /**
     * This method performs a project operation on a relation
     * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
     * @return
     */
    public Relation project(ArrayList<Integer> fields) {
        //your code here
        if (fields == null || fields.size() == 0)
            return new Relation(new ArrayList<Tuple>(), new TupleDesc(new Type[0], new String[0]));
        int rowsCount = this.tuples.size();
        int newFieldsCount = fields.size();
        // 1. Create new tuple desc
        Type[] newTypes = new Type[newFieldsCount];
        String[] newFields = new String[newFieldsCount];
        for (int i = 0; i < newFieldsCount; ++i) {
            if (fields.get(i) >= this.getDesc().numFields()) throw new IllegalArgumentException("invalid column number.");
            newTypes[i] = this.td.getType(fields.get(i));
            newFields[i] = this.td.getFieldName(fields.get(i));
        }
        TupleDesc newTd = new TupleDesc(newTypes, newFields);

        // 2. Create new tuples by new td
        ArrayList<Tuple> newList = new ArrayList<>();
        for (Tuple oriTuple : this.tuples) {
            Tuple tuple = new Tuple(newTd);
//            tuple.setId(value.getId());
//            tuple.setPid(value.getPid());
            for (int j = 0; j < newFieldsCount; ++j) {
                tuple.setField(j, oriTuple.getField(fields.get(j)));
            }

            tuple.setDesc(newTd);
            newList.add(tuple);
        }
        return new Relation(newList, newTd);
    }

    /**
     * This method performs a join between this relation and a second relation.
     * The resulting relation will contain all of the columns from both of the given relations,
     * joined using the equality operator (=)
     * @param other the relation to be joined
     * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
     * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
     * @return
     */
    public Relation join(Relation other, int field1, int field2) {
        //your code here
        if (this.tuples == null || this.tuples.size() == 0 || other.tuples == null || other.tuples.size() == 0)
            return null;
        String[] fields1 = this.td.getFields();
        String[] fields2 = other.td.getFields();
        Type[] types1 = this.td.getTypes();
        Type[] types2 = other.td.getTypes();
        String[] new_fields = new String[fields1.length + fields2.length];
        Type[] new_type = new Type[types1.length + types2.length];

        for (int i = 0; i < fields1.length; i++) {
            new_fields[i] = fields1[i];
            new_type[i] = types1[i];
        }
        for (int i = 0, j = fields1.length; i < fields2.length; ++i, ++j) {
            new_fields[j] = fields2[i];
            new_type[j] = types2[i];
        }
        // 1. Create new Tuple Description
        TupleDesc new_td = new TupleDesc(new_type, new_fields);
        // 2. Filter Cartesian Product of this relation and other relation by the condition
        ArrayList<Tuple> new_tuples = new ArrayList<>();
        for (int i = 0; i < this.tuples.size(); ++i) {
            for (int j = 0; j < other.tuples.size(); ++j) {
                if (this.tuples.get(i).getField(field1).equals(other.tuples.get(j).getField(field2))){
                    Tuple new_tuple = new Tuple(new_td);
                    // 2.1. setField (concat fields of two Tuple Desc)
                    for (int k = 0; k < fields1.length; ++k)
                        new_tuple.setField(k, this.tuples.get(i).getField(k));
                    for (int k = fields1.length, l = 0; l < fields2.length; ++l)
                        new_tuple.setField(k + l, other.tuples.get(j).getField(l));
                    // 2.2. pid, id is not required
                    // 2.3. Add new tuple to the list
                    new_tuples.add(new_tuple);
                }
            }
        }
        return new Relation(new_tuples, new_td);
    }

    /**
     * Performs an aggregation operation on a relation. See the lab write up for details.
     * @param op the aggregation operation to be performed
     * @param groupBy whether or not a grouping should be performed
     * @return
     */
    public Relation aggregate(AggregateOperator op, boolean groupBy) {
        //your code here
        Aggregator aggregator = new Aggregator(op, groupBy, this.td);
        // 1. merge tuples into the aggregator
        for (Tuple tuple: this.tuples) {
            aggregator.merge(tuple);
        }

        ArrayList<Tuple> result = aggregator.getResults();
        if (result == null || result.size() == 0) return null;
        return new Relation(result, result.get(0).getDesc());
    }

    public TupleDesc getDesc() {
        //your code here
        return this.td;
    }

    public ArrayList<Tuple> getTuples() {
        //your code here
        return this.tuples;
    }

    /**
     * Returns a string representation of this relation. The string representation should
     * first contain the TupleDesc, followed by each of the tuples in this relation
     */
    public String toString() {
        //your code here
        StringBuilder sb = new StringBuilder();
        sb.append("Tuple description: ").append(getDesc().toString());
        sb.append("Tuples: \n");
        for (Tuple tuple: this.tuples) sb.append(tuple.toString()).append("\n");
        return sb.toString();
    }
}
