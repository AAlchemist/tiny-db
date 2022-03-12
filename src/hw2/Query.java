/*
 * @author Chengjiang Xia
 * @author Jushen Wang
 */

package hw2;

import hw1.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

public class Query {

    private String q;

    public Query(String q) {
        this.q = q;
    }

    public Relation execute() {
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(q);
        } catch (JSQLParserException e) {
            System.out.println("Unable to parse query");
            e.printStackTrace();
        }
        Select selectStatement = (Select) statement;
        PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
        //your code here

        // 1. FROM clause
        // SELECT * FROM leftTable JOIN rightTable ON leftTable.leftCol=rightTable.rightCol JOIN ...
        Table leftTable = (Table) sb.getFromItem();
        Relation leftRelation = table2Relation(leftTable);
        Catalog catalog = new Catalog();
        // 1.1. JOIN clause (cascade joins)
        List<Join> joinList = sb.getJoins();
        if (joinList != null) {
            for (Join join: joinList) {
                Table rightTable = (Table) join.getRightItem();
                // assume that all join conditions will only use equals
                EqualsTo onExpression = (EqualsTo) join.getOnExpression();
                Relation rightRelation = table2Relation(rightTable);

                Column leftExpression = (Column) onExpression.getLeftExpression();
                Column rightExpression = (Column) onExpression.getRightExpression();


//                System.out.println(leftTable.getName() + onExpression.getRightExpression().toString().split("\\.")[0]);
                // SELECT * FROM test JOIN S ON S.s1 = test.c2 (swap leftExpression and rightExpression)
                if (!leftExpression.getTable().getName().equals(leftTable.getName())) {
                    int leftColId = leftRelation.getDesc().nameToId(rightExpression.getColumnName());
                    int rightColId = rightRelation.getDesc().nameToId(leftExpression.getColumnName());
                    leftRelation = leftRelation.join(rightRelation, leftColId, rightColId);
                } else {
                    int leftColId = leftRelation.getDesc().nameToId(leftExpression.getColumnName());
                    int rightColId = rightRelation.getDesc().nameToId(rightExpression.getColumnName());
                    leftRelation = leftRelation.join(rightRelation, leftColId, rightColId);
                }
                // update leftTable to the rightTable from the last join
                leftTable = rightTable;
            }
        }

        // 2. WHERE clause
        // WHERE col <op> operand
        Expression where = sb.getWhere();
        if (where != null) {
            WhereExpressionVisitor wv = new WhereExpressionVisitor();
            where.accept(wv);
            String col = wv.getLeft();
            Field operand = wv.getRight();
            int colId = leftRelation.getDesc().nameToId(col);
            leftRelation = leftRelation.select(colId, wv.getOp(), operand);
        }

        // 3. SELECT clause
        // SELECT field1 AS userId, field2, field3 ...
        List<SelectItem> selectItemList = sb.getSelectItems();
        // Considering AS clause, we need to cast SelectItems to SelectExpressionItems (using stream)
//        List<SelectExpressionItem> selectExpressionList = selectItemList.stream()
//                .map(item -> (SelectExpressionItem) item)
//                .collect(Collectors.toList());
        // AllColumns indicates SELECT *, no need to project
        ColumnVisitor cv = new ColumnVisitor();
        ArrayList<Integer> fieldList = new ArrayList<>();
        TupleDesc desc = leftRelation.getDesc();
        for (SelectItem item: selectItemList) {
            item.accept(cv);
            String colName = cv.getColumn();
            if (!colName.equals("*")) {
//                System.out.println(colName);
                fieldList.add(desc.nameToId(colName));
            } else {
                // * indicates projecting all fields (columns)
                for (String field : desc.getFields())
                    fieldList.add(desc.nameToId(field));
            }
        }
//        System.out.println(fieldList);
        leftRelation = leftRelation.project(fieldList);

        // 4. Aggregation
        List<Expression> groupByList = sb.getGroupByColumnReferences();
//        System.out.println(cv);
        if (cv.isAggregate()) leftRelation = leftRelation.aggregate(cv.getOp(), groupByList != null);
        return leftRelation;

    }

    private Relation table2Relation(Table table) {
        Catalog catalog = Database.getCatalog();
//        System.out.println(catalog.getTableByName());
        HeapFile heapFile = catalog.getDbFile(catalog.getTableId(table.getName()));
        return new Relation(heapFile.getAllTuples(), heapFile.getTupleDesc());
    }
}
