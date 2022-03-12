/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw2;

import hw1.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {

    private TupleDesc td;
    private boolean group_by;
    private AggregateOperator aop;
    ArrayList<Tuple> tuples = new ArrayList<Tuple>();

    public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
        //your code here
        this.td = td;
        this.group_by = groupBy;
        this.aop = o;
    }

    /**
     * Merges the given tuple into the current aggregation
     * @param t the tuple to be aggregated
     */
    public void merge(Tuple t) {
        //your code here
        this.tuples.add(t);
    }

    /**
     * Returns the result of the aggregation
     * @return a list containing the tuples after aggregation
     */
    public ArrayList<Tuple> getResults() {
        //your code here
        switch (aop) {
            case COUNT:
                if (!group_by) {
                    return count_wo_groupby();
                }
                else {
                    return count_w_groupby();
                }
            case SUM:
                if (!group_by) {
                    return sum_wo_groupby();
                }
                else {
                    return sum_w_groupby();
                }
            case AVG:
                if (!group_by) {
                    return ave_wo_groupby();
                }
                else {
                    return ave_w_groupby();
                }
            case MIN:
                if (!group_by) {
                    return min_wo_groupby();
                }
                else {
                    return min_w_groupby();
                }
            case MAX:
                if (!group_by) {
                    return max_wo_groupby();
                }
                else {
                    return max_w_groupby();
                }
        }
        return null;
    }
    private ArrayList<Tuple> count_wo_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        //define new td
        Type[] new_type = new Type[]{Type.INT};
        String[] new_fields = new String[]{"COUNT"};
        TupleDesc newTd = new TupleDesc(new_type,new_fields);
        //define new tp
        Tuple newT = new Tuple(newTd);
        IntField field = new IntField(tuples.size());
        newT.setField(newTd.numFields() - 1, field);
        list.add(newT);
        return list;

    }
    private ArrayList<Tuple> count_w_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        //define new td
        Type[] new_type = new Type[]{this.td.getType(0), Type.INT};
        String[] new_field = new String[]{this.td.getFieldName(0), "COUNT"};
        TupleDesc new_td = new TupleDesc(new_type, new_field);
        //iterate through the tuples
        //using map to store fields and counts;
        //map for <Integer, Integer> (fieldName, counts)
        HashMap <Integer, Integer> integerMap = new HashMap<>();
        //map for <String, Integer>
        HashMap <String, Integer> StringMap = new HashMap<>();
        for (Tuple t: tuples) {
            if (t.getDesc().getType(0) == Type.INT) {
                integerMap.put(t.getField(0).hashCode(), integerMap.getOrDefault(t.getField(0).hashCode(),0) + 1);
            }
            else {
                StringMap.put(t.getField(0).toString(), StringMap.getOrDefault(t.getField(0).toString(),0) + 1);
            }
        }
        if (integerMap.size() == 0 && StringMap.size() == 0) return list;
        if (integerMap.size() == 0) {
            for (String name : StringMap.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new StringField(name));
                IntField intField = new IntField(StringMap.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }
        else {
            for (Integer name : integerMap.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new IntField(name));
                IntField intField = new IntField(integerMap.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }


        return list;
    }
    private ArrayList<Tuple> sum_wo_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        if (this.td.getType(0) == Type.STRING) {
            return list;
        }
        //define new td for sum
        Type[] new_type = new Type[]{Type.INT};
        String[] new_fields = new String[]{"SUM"};
        TupleDesc newTd = new TupleDesc(new_type, new_fields);
        //define new tuple for sum
        Tuple new_tuple = new Tuple(newTd);
        if (this.tuples.size() == 0) {
            return list;
        }
        int sum = 0;
        for (Tuple t : tuples) {
            sum += t.getField(0).hashCode();
        }
        IntField new_infield = new IntField(sum);
        new_tuple.setField(0, new_infield);
        list.add(new_tuple);
        return list;
    }
    private ArrayList<Tuple> sum_w_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        Type[] new_type = new Type[]{this.td.getType(0), Type.INT};
        String[] new_field = new String[]{this.td.getFieldName(0), "COUNT"};
        TupleDesc new_td = new TupleDesc(new_type, new_field);
        //iterate through the tuples
        //using map to store fields and counts
        if (this.td.getType(0) == Type.INT && this.td.getType(1) == Type.INT) {
            //map for field, sum;
            HashMap<Integer, Integer> Map = new HashMap<>();
            for (Tuple t : tuples) {
                Map.put(t.getField(0).hashCode(), Map.getOrDefault(t.getField(0).hashCode(),0) + t.getField(1).hashCode());
            }
            for (Integer name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new IntField(name));
                IntField intField = new IntField(Map.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }
        else if (this.td.getType(0) == Type.STRING && this.td.getType(1) == Type.INT) {
            HashMap<String, Integer> Map = new HashMap<>();
            for (Tuple t : tuples) {
                Map.put(t.getField(0).getType().toString(), Map.getOrDefault(t.getField(0).toString(), 0) + t.getField(1).hashCode());
            }
            for (String name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new StringField(name));
                IntField intField = new IntField(Map.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }

        return list;
    }

    private ArrayList<Tuple> ave_wo_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        if (this.td.getType(0) == Type.STRING) {
            return list;
        }
        //define new td for ave
        Type[] new_type = new Type[]{Type.INT};
        String[] new_fields = new String[]{"AVE"};
        TupleDesc newTd = new TupleDesc(new_type, new_fields);
        //define new tuple for sum
        Tuple new_tuple = new Tuple(newTd);
        if (this.tuples.size() == 0) {
            return list;
        }
        int sum = 0;
        for (Tuple t : tuples) {
            sum += t.getField(0).hashCode();
        }
        int ave = sum / tuples.size();
        IntField new_infield = new IntField(ave);
        new_tuple.setField(0, new_infield);
        list.add(new_tuple);
        return list;
    }
    private ArrayList<Tuple> ave_w_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        Type[] new_type = new Type[]{this.td.getType(0), Type.INT};
        String[] new_field = new String[]{this.td.getFieldName(0), "COUNT"};
        TupleDesc new_td = new TupleDesc(new_type, new_field);
        //iterate through the tuples
        //using map to store fields and counts
        if (this.td.getType(0) == Type.INT && this.td.getType(1) == Type.INT) {
            //map for field, sum;
            HashMap<Integer, Integer> Map = new HashMap<>();
            HashMap<Integer, Integer> Size = new HashMap<>();
            for (Tuple t : tuples) {
                Size.put(t.getField(0).hashCode(), Map.getOrDefault(t.getField(0).hashCode(),0) + 1);
                Map.put(t.getField(0).hashCode(), Map.getOrDefault(t.getField(0).hashCode(),0) + t.getField(1).hashCode());
            }
            for (Integer name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new IntField(name));
                IntField intField = new IntField(Map.get(name)/Size.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }
        else if (this.td.getType(0) == Type.STRING && this.td.getType(1) == Type.INT) {
            HashMap<String, Integer> Map = new HashMap<>();
            HashMap<String, Integer> Size = new HashMap<>();
            for (Tuple t : tuples) {
                Size.put(t.getField(0).getType().toString(), Map.getOrDefault(t.getField(0).toString(), 0) + 1);
                Map.put(t.getField(0).getType().toString(), Map.getOrDefault(t.getField(0).toString(), 0) + t.getField(1).hashCode());
            }
            for (String name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new StringField(name));
                IntField intField = new IntField(Map.get(name)/Size.get(name));
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }

        return list;
    }
    private ArrayList<Tuple> min_wo_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        if (tuples.size() <= 0) {
            return list;
        }
        Tuple tmp = tuples.get(0);
//        System.out.println(tuples);

        for (int i = 1; i < tuples.size(); i++) {

            if (tuples.get(i).getDesc().getType(0) == Type.INT) {
//                System.out.println(((IntField) tuples.get(i).getField(0)).getValue() + " " + ((IntField) tmp.getField(0)).getValue());
                if (((IntField) tuples.get(i).getField(0)).getValue() < ((IntField) tmp.getField(0)).getValue()) {
                    tmp = tuples.get(i);
                    System.out.println("tmp = " + tmp);
                }
            }
            else if (tuples.get(i).getDesc().getType(0) == Type.STRING) {
                if (tuples.get(i).getField(0).toString().compareTo(tmp.getField(0).toString()) < 0) {
                    tmp = tuples.get(i);
                }
            }
        }
        list.add(tmp);
        return list;
    }
    private ArrayList<Tuple> min_w_groupby() {
        //unedited
        ArrayList<Tuple> list = new ArrayList<>();
        Type[] new_type = new Type[]{this.td.getType(0), Type.INT};
        String[] new_field = new String[]{this.td.getFieldName(0), "COUNT"};
        TupleDesc new_td = new TupleDesc(new_type, new_field);
        //iterate through the tuples
        //using map to store fields and counts
        if (this.td.getType(0) == Type.INT) {
            //map for field, index;
            HashMap<Integer, Integer> Map = new HashMap<>();
            for (int i = 0; i < tuples.size(); i++) {
                if (Map.containsKey(tuples.get(i).getField(0).hashCode())) {
                    if (this.td.getType(1) == Type.INT) {
                        if (tuples.get(i).getField(1).hashCode() < tuples.get(Map.get(tuples.get(i).getField(0).hashCode())).getField(1).hashCode()) {
                            Map.put(tuples.get(i).getField(0).hashCode(),i);
                        }
                    }
                    else {
                        if (tuples.get(i).getField(1).toString().compareTo(tuples.get(Map.get(tuples.get(i).getField(0).hashCode())).getField(1).toString()) < 0) {
                            Map.put(tuples.get(i).getField(0).hashCode(),i);
                        }
                    }
                }
                else {
                    Map.put(tuples.get(i).getField(0).hashCode(), i);
                }
            }
            for (Integer name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new IntField(name));
                IntField intField = new IntField(tuples.get(Map.get(name)).getField(1).hashCode());
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }
        else if (this.td.getType(0) == Type.STRING) {
            HashMap<String, Integer> Map = new HashMap<>();
            for (int i = 0; i < tuples.size(); i++) {
                if (Map.containsKey(tuples.get(i).getField(0).toString())) {
                    if (this.td.getType(1) == Type.INT) {
                        if (tuples.get(i).getField(1).hashCode() < tuples.get(Map.get(tuples.get(i).getField(0).toString())).getField(1).hashCode()) {
                            Map.put(tuples.get(i).getField(0).toString(),i);
                        }
                    }
                    else {
                        if (tuples.get(i).getField(1).toString().compareTo(tuples.get(Map.get(tuples.get(i).getField(0).toString())).getField(1).toString()) < 0) {
                            Map.put(tuples.get(i).getField(0).toString(),i);
                        }
                    }
                }
                else {
                    Map.put(tuples.get(i).getField(0).toString(), i);
                }
            }
            for (String name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new StringField(name));
                IntField intField = new IntField(tuples.get(Map.get(name)).getField(1).hashCode());
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }

        return list;

    }

    private ArrayList<Tuple> max_wo_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        if (tuples.size() <= 0) {
            return list;
        }
        Tuple tmp = tuples.get(0);
        for (int i = 1; i < tuples.size(); i++) {
            if (tuples.get(i).getDesc().getType(0) == Type.INT) {
                if (tuples.get(i).getField(0).hashCode() > tmp.getField(0).hashCode()) {
                    tmp = tuples.get(i);
                }
            }
            else if (tuples.get(i).getDesc().getType(0) == Type.STRING) {
                if (tuples.get(i).getField(0).toString().compareTo(tmp.getField(0).toString()) > 0) {
                    tmp = tuples.get(i);
                }
            }
        }
        list.add(tmp);
        return list;

    }

    private ArrayList<Tuple> max_w_groupby() {
        ArrayList<Tuple> list = new ArrayList<>();
        Type[] new_type = new Type[]{this.td.getType(0), Type.INT};
        String[] new_field = new String[]{this.td.getFieldName(0), "MAX"};
        TupleDesc new_td = new TupleDesc(new_type, new_field);
        //iterate through the tuples
        //using map to store fields and counts
        if (this.td.getType(0) == Type.INT) {
            //map for field, index;
            HashMap<Integer, Integer> Map = new HashMap<>();
            for (int i = 0; i < tuples.size(); i++) {
                if (Map.containsKey(tuples.get(i).getField(0).hashCode())) {
                    if (this.td.getType(1) == Type.INT) {
                        if (tuples.get(i).getField(1).hashCode() > tuples.get(Map.get(tuples.get(i).getField(0).hashCode())).getField(1).hashCode()) {
                            Map.put(tuples.get(i).getField(0).hashCode(),i);
                        }
                    }
                    else {
                        if (tuples.get(i).getField(1).toString().compareTo(tuples.get(Map.get(tuples.get(i).getField(0).hashCode())).getField(1).toString()) > 0) {
                            Map.put(tuples.get(i).getField(0).hashCode(),i);
                        }
                    }
                }
                else {
                    Map.put(tuples.get(i).getField(0).hashCode(), i);
                }
            }
            for (Integer name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new IntField(name));
                IntField intField = new IntField(tuples.get(Map.get(name)).getField(1).hashCode());
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }
        else if (this.td.getType(0) == Type.STRING) {
            HashMap<String, Integer> Map = new HashMap<>();
            for (int i = 0; i < tuples.size(); i++) {
                if (Map.containsKey(tuples.get(i).getField(0).toString())) {
                    if (this.td.getType(1) == Type.INT) {
                        if (tuples.get(i).getField(1).hashCode() > tuples.get(Map.get(tuples.get(i).getField(0).toString())).getField(1).hashCode()) {
                            Map.put(tuples.get(i).getField(0).toString(),i);
                        }
                    }
                    else {
                        if (tuples.get(i).getField(1).toString().compareTo(tuples.get(Map.get(tuples.get(i).getField(0).toString())).getField(1).toString()) > 0) {
                            Map.put(tuples.get(i).getField(0).toString(),i);
                        }
                    }
                }
                else {
                    Map.put(tuples.get(i).getField(0).toString(), i);
                }
            }
            for (String name : Map.keySet()) {
                Tuple newTp = new Tuple(new_td);
                newTp.setField(0, new StringField(name));
                IntField intField = new IntField(tuples.get(Map.get(name)).getField(1).hashCode());
                newTp.setField(1, intField);
                list.add(newTp);
            }
        }

        return list;
    }
}
