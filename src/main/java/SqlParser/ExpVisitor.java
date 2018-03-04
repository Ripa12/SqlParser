package SqlParser;

import javafx.beans.binding.IntegerExpression;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Created by Richard on 2018-03-04.
 */
public class ExpVisitor implements ExpressionVisitor {

    public void doWork(Expression exp){
        if (exp instanceof AndExpression) {
            this.visit((AndExpression)exp);
        } else if (exp instanceof OrExpression) {
            visit((OrExpression)exp);
        }
        else if (exp instanceof EqualsTo) {
            visit((EqualsTo)exp);
        }
        else if (exp instanceof GreaterThan) {
            visit((GreaterThan)exp);
        }
        else if (exp instanceof GreaterThanEquals) {
            visit((GreaterThanEquals)exp);
        }
        else if (exp instanceof MinorThan) {
            visit((MinorThan)exp);
        }
        else if (exp instanceof MinorThanEquals) {
            visit((MinorThanEquals)exp);
        }
        else if (exp instanceof NotEqualsTo) {
            visit((NotEqualsTo)exp);
        }
        else if (exp instanceof DoubleValue) {
            this.visit((DoubleValue)exp);
        } else if (exp instanceof StringValue) {
            visit((StringValue)exp);
        }
        else if (exp instanceof JdbcNamedParameter) {
            visit((JdbcNamedParameter)exp);
        }
        else if (exp instanceof JdbcParameter) {
            visit((JdbcParameter)exp);
        }
        else if (exp instanceof Column) {
            visit((Column)exp);
        }
        else {
            // Do naught
        }
    }

    public void visit(NullValue nullValue) {

    }

    public void visit(Function function) {

    }

    public void visit(SignedExpression signedExpression) {

    }

    public void visit(JdbcParameter jdbcParameter) {

    }

    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    public void visit(DoubleValue doubleValue) {
        System.out.println("Value: " + doubleValue.getValue());
    }

    public void visit(LongValue longValue) {

    }

    public void visit(HexValue hexValue) {

    }

    public void visit(DateValue dateValue) {

    }

    public void visit(TimeValue timeValue) {

    }

    public void visit(TimestampValue timestampValue) {

    }

    public void visit(Parenthesis parenthesis) {

    }

    public void visit(StringValue stringValue) {
        System.out.println("String: " + stringValue.getValue());
    }

    public void visit(Addition addition) {

    }

    public void visit(Division division) {

    }

    public void visit(Multiplication multiplication) {

    }

    public void visit(Subtraction subtraction) {

    }

    public void visit(AndExpression andExpression) {
        doWork(andExpression.getLeftExpression());
        doWork(andExpression.getRightExpression());
    }

//    public void visit(AndExpression andExpression) {
//        if(andExpression != null) {
//            if (andExpression.getLeftExpression() instanceof AndExpression) {
//                andExpression.getLeftExpression().accept(this);
//            } else {
//                //System.out.println(andExpression.getLeftExpression());
//            }
//
//            andExpression.getLeftExpression().accept(this);
//            //System.out.println(andExpression.getRightExpression());
//        }
//    }

    public void visit(OrExpression orExpression) {
//        doWork(OrExpression);
//        doWork(OrExpression.getRightExpression());
    }

    public void visit(Between between) {

    }

    public void visit(EqualsTo equalsTo) {
        doWork(equalsTo.getLeftExpression());
        doWork(equalsTo.getRightExpression());
    }

    public void visit(GreaterThan greaterThan) {
        doWork(greaterThan.getLeftExpression());
        doWork(greaterThan.getRightExpression());
    }

    public void visit(GreaterThanEquals greaterThanEquals) {
        doWork(greaterThanEquals.getLeftExpression());
        doWork(greaterThanEquals.getRightExpression());
    }

    public void visit(InExpression inExpression) {

    }

    public void visit(IsNullExpression isNullExpression) {

    }

    public void visit(LikeExpression likeExpression) {

    }

    public void visit(MinorThan minorThan) {
        doWork(minorThan.getLeftExpression());
        doWork(minorThan.getRightExpression());
    }

    public void visit(MinorThanEquals minorThanEquals) {
        doWork(minorThanEquals.getLeftExpression());
        doWork(minorThanEquals.getRightExpression());
    }

    public void visit(NotEqualsTo notEqualsTo) {
        doWork(notEqualsTo.getLeftExpression());
        doWork(notEqualsTo.getRightExpression());
    }

    public void visit(Column column) {
        System.out.println("Attribute: " + column.getColumnName());
    }

    public void visit(SubSelect subSelect) {

    }

    public void visit(CaseExpression caseExpression) {

    }

    public void visit(WhenClause whenClause) {

    }

    public void visit(ExistsExpression existsExpression) {

    }

    public void visit(AllComparisonExpression allComparisonExpression) {

    }

    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    public void visit(Concat concat) {

    }

    public void visit(Matches matches) {

    }

    public void visit(BitwiseAnd bitwiseAnd) {

    }

    public void visit(BitwiseOr bitwiseOr) {

    }

    public void visit(BitwiseXor bitwiseXor) {

    }

    public void visit(CastExpression castExpression) {

    }

    public void visit(Modulo modulo) {

    }

    public void visit(AnalyticExpression analyticExpression) {

    }

    public void visit(WithinGroupExpression withinGroupExpression) {

    }

    public void visit(ExtractExpression extractExpression) {

    }

    public void visit(IntervalExpression intervalExpression) {

    }

    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

    }

    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    public void visit(JsonExpression jsonExpression) {

    }

    public void visit(JsonOperator jsonOperator) {

    }

    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

    }

    public void visit(UserVariable userVariable) {

    }

    public void visit(NumericBind numericBind) {

    }

    public void visit(KeepExpression keepExpression) {

    }

    public void visit(MySQLGroupConcat mySQLGroupConcat) {

    }

    public void visit(RowConstructor rowConstructor) {

    }

    public void visit(OracleHint oracleHint) {

    }

    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    public void visit(NotExpression notExpression) {

    }
}
