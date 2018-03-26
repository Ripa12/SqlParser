package SqlParser;

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;

class AndExpressionVisitor extends GenericExpressionVisitor{

    public AndExpressionVisitor(MyIntervalTree tree){
        this.intervalTree = tree;
    }

    public void process(AndExpression exp){
        int start, end = 0;
        super.process(exp);
        start = getExtractedValue();
        super.process(exp);
        end = getExtractedValue();

        this.intervalTree.insert(new MyIntervalTree.Interval(start, end));
    }

//        @Override
//        public void visit(LongValue longValue) {
//            super.visit(longValue);
//        }
//
//        @Override
//        public void visit(DoubleValue doubleValue) {
//            super.visit(doubleValue);
//        }

    @Override
    public void visit(EqualsTo equalsTo) {
        super.visit(equalsTo);
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
    }
}