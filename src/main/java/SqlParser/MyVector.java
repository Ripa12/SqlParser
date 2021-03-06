package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

import java.util.ArrayList;

/**
 * Created by Richard on 2018-04-22.
 */
public abstract class MyVector implements NumberVector {

    protected int[] vector;

    public MyVector(int[] v){
        this.vector = v;
    }

    public abstract boolean isContained(ArrayList<CLIQUEInterval> intervals);
    public abstract boolean isContained(double min, double max, int dim);

    @Override
    public int getDimensionality() {
        return vector.length;
    }

    @Override
    public double getMin(int i) {
        return vector[i];
    }

    @Override
    public double getMax(int i) {
        return vector[i];
    }

    @Override
    public Number getValue(int i) {
        return vector[i];
    }

    @Override
    public double doubleValue(int i) {
        return (double)vector[i];
    }

    @Override
    public float floatValue(int i) {
        return (float)vector[i];
    }

    @Override
    public int intValue(int i) {
        return vector[i];
    }

    @Override
    public long longValue(int i) {
        return (long)vector[i];
    }

    @Override
    public short shortValue(int i) {
        return (short)vector[i];
    }

    @Override
    public byte byteValue(int i) {
        return (byte)vector[i];
    }

    @Override
    public Vector getColumnVector() {
        return new Vector();
    }
}
