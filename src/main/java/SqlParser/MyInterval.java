package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;

import java.util.ArrayList;

public class MyInterval extends MyVector {

    private int[] upperBound;

    public MyInterval(int[] lowerBound, int[] upperBound) {
        super(lowerBound);
        this.upperBound = upperBound;
    }

    @Override
    public boolean isContained(ArrayList<CLIQUEInterval> intervals) {
        for(CLIQUEInterval interval : intervals) {
            if(interval.getMin() > vector[interval.getDimension()] || upperBound[interval.getDimension()] >= interval.getMax()) {
                return false;
            }
        }
        return true;
    }
}
