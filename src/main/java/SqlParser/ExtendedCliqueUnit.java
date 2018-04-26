package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEUnit;
import de.lmu.ifi.dbs.elki.data.NumberVector;

public class ExtendedCliqueUnit<V extends MyVector> extends CLIQUEUnit<V> {
    /**
     * Creates a new one-dimensional unit for the given interval.
     *
     * @param interval the interval belonging to this unit
     */
    public ExtendedCliqueUnit(CLIQUEInterval interval) {
        super(interval);
    }

    /**
     * Returns true, if the intervals of this unit contain the specified feature
     * vector.
     *
     * @param vector the feature vector to be tested for containment
     * @return true, if the intervals of this unit contain the specified feature
     *         vector, false otherwise
     */
    @Override
    public boolean contains(V vector) {
        return vector.isContained(getIntervals());
    }
}
