package hw2;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;

public class AggregateExpressionVisitor extends ExpressionVisitorAdapter {

	private AggregateOperator op;
	private String column;
	private boolean isAggregate;
	
	public AggregateExpressionVisitor() {
		column = "";
		isAggregate = false;
		op = null;
	}
	
	@Override
	public void visit(AllColumns allColumns) {
		column = "*";
	}
	
	@Override
	public void visit(Column column) {
		this.column = column.getColumnName();
	}
	
	@Override
	public void visit(Function function) {
		String f = function.getName();
		f = f.toLowerCase();
		switch (f) {
		case "avg":
			op = AggregateOperator.AVG;
			break;
		case "count":
			op = AggregateOperator.COUNT;
			break;
		case "min":
			op = AggregateOperator.MIN;
			break;
		case "max":
			op = AggregateOperator.MAX;
			break;
		case "sum":
			op = AggregateOperator.SUM;
			break;
		default:
			throw new UnsupportedOperationException("Aggregate Functions only");
		
		}
		
		if(op != AggregateOperator.COUNT) {
			List<Expression> el = function.getParameters().getExpressions();
			if(el.size() > 1) {
				throw new UnsupportedOperationException("Aggregate Functions only");
			} else {
				column = el.get(0).toString();
			}
		} else {
			column = "*";
		}
		this.isAggregate = true;
	}
	
	public String getColumn() {
		return this.column;
	}
	
	public AggregateOperator getOp() {
		return this.op;
	}
	
	public boolean isAggregate() {
		return this.isAggregate;
	}
}
