/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw1;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc {

	private Type[] types;
	private String[] fields;
	
    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr array specifying the number of and types of fields in
     *        this TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
    	//your code here (v1)
//        if (typeAr.length <= 0) {
//            throw new IllegalArgumentException("Error: Tuple must contain at least one entry.");
//        }
        if (typeAr.length != fieldAr.length) {
            throw new IllegalArgumentException("Error: typeAr and fieldAr must have the same size.");
        }
        // Init types and fields
        int n = typeAr.length;
        types = Arrays.copyOf(typeAr, n);
        fields = Arrays.copyOf(fieldAr, n);
    }

    public Type[] getTypes() {
        return types;
    }

    public void setTypes(Type[] types) {
        this.types = types;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        //your code here (v1)
    	return fields.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        //your code here (v1)
        int n = fields.length;
        if (i >= n || i < 0) {
            throw new NoSuchElementException("Error: i should be greater than 0 and smaller than" + n + ".");
        }
    	return fields[i];
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int nameToId(String name) throws NoSuchElementException {
        //your code here (v1)
        int n = fields.length;
        for (int i = 0; i < n; ++i) {
            if (name.equals(fields[i])) return i;
        }
        // If no matching name is found, then throw an exception
        throw new NoSuchElementException("Error: No field with a matching name is found.");
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getType(int i) throws NoSuchElementException {
        //your code here (v1)
        int n = types.length;
        if (i >= n || i < 0) {
            throw new NoSuchElementException("Error: i should be greater than or equals to 0 and smaller than " + n + ".");
        }
        return types[i];
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
    	//your code here (v1)
        int sizeCount = 0, stringSize = 129, intSize = 4;
        for (Type type: types) {
            if (type == Type.STRING) {
                sizeCount += stringSize;
            } else {
                sizeCount += intSize;
            }
        }
    	return sizeCount;
    }

    /**
     * Compares the specified object with this TupleDesc for equality.
     * Two TupleDescs are considered equal if they are the same size and if the
     * n-th type in this TupleDesc is equal to the n-th type in td.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
    	//your code here (v1)

        // Special cases
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TupleDesc tupleDesc = (TupleDesc) o;
        // Equal if they are the same size and the same types
        int n = this.types.length;
        if (tupleDesc.types.length == n) {
            for (int i = 0; i < n; ++i) {
                if (this.types[i] != tupleDesc.types[i]) return false;
            }
            return true;
        }
    	return false;
    }
    

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
//        throw new UnsupportedOperationException("unimplemented");

        //your code here (v1)
        if (types == null) return 0;
        // base: a random prime number
        long code = 0, base = 31;
        // Calculate hashCode based on elements in the types array
        for (Type type: types) {
            code = (code * base + type.hashCode()) % Integer.MAX_VALUE;
        }
        return (int) code;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * @return String describing this descriptor.
     */
    public String toString() {
        //your code here (v1)
        StringBuilder sb = new StringBuilder();
        int n = types.length;
        for (int i = 0; i < n; ++i) {
            sb.append(types[i]).append("(").append(fields[i]).append(")");
            if (i < n - 1) {
                sb.append(", ");
            } else {
                sb.append("\n");
            }
        }
    	return sb.toString();
    }
}
