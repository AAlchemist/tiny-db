/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw1;

import java.util.NoSuchElementException;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {

    /**
     * Creates a new tuple with the given description
     * @param t the schema for this tuple
     */

    //your code here (v1)
    private TupleDesc tupleDesc;
    private Field[] fields;
    private int pid;
    private int id;

    public Tuple(TupleDesc t) {
        //your code here (v1)
        this.tupleDesc = t;
        this.fields = new Field[t.numFields()];
    }

    public TupleDesc getDesc() {
        //your code here (v1)
        return tupleDesc;
    }

    /**
     * retrieves the page id where this tuple is stored
     * @return the page id of this tuple
     */
    public int getPid() {
        //your code here (v1)
        return this.pid;
    }

    public void setPid(int pid) {
        //your code here (v1)
        this.pid = pid;
    }

    /**
     * retrieves the tuple (slot) id of this tuple
     * @return the slot where this tuple is stored
     */
    public int getId() {
        //your code here (v1)
        return this.id;
    }

    public void setId(int id) {
        //your code here (v1)
        this.id = id;
    }

    public void setDesc(TupleDesc td) {
        //your code here (v1)
        this.tupleDesc = td;
    }



    /**
     * Stores the given data at the i-th field
     * @param i the field number to store the data
     * @param v the data
     */
    public void setField(int i, Field v) {
        //your code here (v1)
        int n = fields.length;
        if (i >= n || i < 0) {
            throw new NoSuchElementException("Error: i should be greater than 0 and smaller than" + n + ".");
        }
        fields[i] = v;
    }

    public Field getField(int i) {
        //your code here (v1)
        int n = fields.length;
        if (i >= n || i < 0) {
            throw new NoSuchElementException("Error: i should be greater than 0 and smaller than" + n + ".");
        }
        return fields[i];
    }

    /**
     * Creates a string representation of this tuple that displays its contents.
     * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
     * the String columns to readable text).
     */
    public String toString() {
        //your code here (v1)
        StringBuilder sb = new StringBuilder();
        sb.append("[id=").append(id);
        // Both IntField and StringField override toString()
        for (int i = 0; i < fields.length; ++i) {
            sb.append(", ").append(tupleDesc.getFieldName(i))
                    .append("=").append(fields[i].toString());
        }
        sb.append("]\n");
        return sb.toString();
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof Tuple)) return false;
//        Tuple other = (Tuple) obj;
//        return other.toString().equals(this.toString());
//    }
}
