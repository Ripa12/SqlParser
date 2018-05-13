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
            if(interval.getMin() > upperBound[interval.getDimension()] || vector[interval.getDimension()] >= interval.getMax()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isContained(double min, double max, int dim) {
        if(min > upperBound[dim] || vector[dim] >= max) {
            return false;
        }
        return true;
    }

    @Override
    public double getMax(int i) {
        return upperBound[i];
    }

    @Override
    public double getMin(int i) {
        return super.getMin(i);
    }
}
