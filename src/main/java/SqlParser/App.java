package SqlParser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.List;

/**
 * Hello world!
 */

// https://medium.com/@ankane/introducing-dexter-the-automatic-indexer-for-postgres-5f8fa8b28f27
public class App {
    public static void main(String[] args) {

// TODO Auto-generated method stub
        String statements = "SELECT * FROM Customers WHERE Country='Mexico';\n" +
                "SELECT * FROM mytable WHERE b=5 AND c>3;";
//statement = SampleSQL1.sql;

        CCJSqlParserManager parserManager = new CCJSqlParserManager();

        Select select = null;
        try {
            //select = (Select) parserManager.parse(new StringReader(statements));
            Statements stats = CCJSqlParserUtil.parseStatements(statements);
            for(Statement statement : stats.getStatements()){
                select = (Select) statement;
                PlainSelect ps = (PlainSelect) select.getSelectBody();

                FromItem tableName = ps.getFromItem();
                System.out.println("Tablename: " + tableName.toString());

                List<SelectItem> selectItems = ps.getSelectItems();
                for(SelectItem item : selectItems){

                    //System.out.println("Selected Item: " + item.toString());
                }

                ExpressionVisitor visitor = new GenericExpressionVisitor();
                Expression exp = ps.getWhere();

                exp.accept(visitor);

                //System.out.println(ps.getSelectItems().get(1).toString());

                // here you have to check what kind of expression it is and execute your actions individualy for every expression implementation
                //AndExpression e = (AndExpression) ps.getWhere();

                //AndExpression andExpression = null;
//                ExpressionVisitor visitor = new ExpVisitor();
//
//                ps.getWhere().accept(visitor);
//
//                visitor.visit((AndExpression)ps.getWhere());

                //System.out.println(andExpression.getLeftExpression());


                //System.out.println(e.getLeftExpression());  // whitch is another expression you can drill down
                System.out.println();
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        System.out.println("Hello World!");
    }
}
