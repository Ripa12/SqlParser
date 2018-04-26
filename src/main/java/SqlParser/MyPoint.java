package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;

import java.util.ArrayList;

public class MyPoint extends MyVector{

    public MyPoint(int[] v) {
        super(v);
    }

    @Override
    public boolean isContained(ArrayList<CLIQUEInterval> intervals) {
        for(CLIQUEInterval interval : intervals) {
            final double value = doubleValue(interval.getDimension());
            if(interval.getMin() > value || value >= interval.getMax()) {
                return false;
            }
        }
        return true;
    }
}
