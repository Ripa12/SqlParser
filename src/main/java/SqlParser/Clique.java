package SqlParser;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2015
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.SubspaceClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.Subspace;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.database.relation.RelationUtil;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Centroid;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.utilities.FormatUtil;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Flag;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;

/**
 * <p/>
 * Implementation of the CLIQUE algorithm, a grid-based algorithm to identify
 * dense clusters in subspaces of maximum dimensionality.
 * </p>
 * <p/>
 * The implementation consists of two steps: <br>
 * 1. Identification of subspaces that contain clusters <br>
 * 2. Identification of clusters
 * </p>
 * <p/>
 * The third step of the original algorithm (Generation of minimal description
 * for the clusters) is not (yet) implemented.
 * </p>
 * <p>
 * Reference: <br>
 * R. Agrawal, J. Gehrke, D. Gunopulos, P. Raghavan: Automatic Subspace
 * Clustering of High Dimensional Data for Data Mining Applications. <br>
 * In Proc. ACM SIGMOD Int. Conf. on Management of Data, Seattle, WA, 1998.
 * </p>
 *
 * @param <V> the type of NumberVector handled by this Algorithm
 * @author Elke Achtert
 * @apiviz.has SubspaceModel
 * @apiviz.has ExtendedCLIQUESubspace
 * @apiviz.uses ExtendedCliqueUnit
 * @since 0.2
 */
@Title("CLIQUE: Automatic Subspace Clustering of High Dimensional Data for Data Mining Applications")
@Description("Grid-based algorithm to identify dense clusters in subspaces of maximum dimensionality.")
@Reference(authors = "R. Agrawal, J. Gehrke, D. Gunopulos, P. Raghavan", title = "Automatic Subspace Clustering of High Dimensional Data for Data Mining Applications", booktitle = "Proc. SIGMOD Conference, Seattle, WA, 1998", url = "http://dx.doi.org/10.1145/276304.276314")
public class Clique<V extends MyVector> extends AbstractAlgorithm<Clustering<SubspaceModel>> implements SubspaceClusteringAlgorithm<SubspaceModel> {
    /**
     * The logger for this class.
     */
    private static final Logging LOG = Logging.getLogger(CLIQUE.class);

    /**
     * Parameter to specify the number of intervals (units) in each dimension,
     * must be an integer greater than 0.
     * <p>
     * Key: {@code -clique.xsi}
     * </p>
     */
    public static final OptionID XSI_ID = new OptionID("clique.xsi", "The number of intervals (units) in each dimension.");

    /**
     * Parameter to specify the density threshold for the selectivity of a unit,
     * where the selectivity is the fraction of total feature vectors contained in
     * this unit, must be a double greater than 0 and less than 1.
     * <p>
     * Key: {@code -clique.tau}
     * </p>
     */
    public static final OptionID TAU_ID = new OptionID("clique.tau", "The density threshold for the selectivity of a unit, where the selectivity is" + "the fraction of total feature vectors contained in this unit.");

    /**
     * Flag to indicate that only subspaces with large coverage (i.e. the fraction
     * of the database that is covered by the dense units) are selected, the rest
     * will be pruned.
     * <p>
     * Key: {@code -clique.prune}
     * </p>
     */
    public static final OptionID PRUNE_ID = new OptionID("clique.prune", "Flag to indicate that only subspaces with large coverage " + "(i.e. the fraction of the database that is covered by the dense units) " + "are selected, the rest will be pruned.");

    /**
     * Holds the value of {@link #XSI_ID}.
     */
    private int xsi;

    /**
     * Holds the value of {@link #TAU_ID}.
     */
    private double tau;

    /**
     * Holds the value of {@link #PRUNE_ID}.
     */
    private boolean prune;

    /**
     * Constructor.
     *
     * @param xsi   Xsi value
     * @param tau   Tau value
     * @param prune Prune flag
     */
    public Clique(int xsi, double tau, boolean prune) {
        super();
        this.xsi = xsi;
        this.tau = tau;
        this.prune = prune;
    }

    /**
     * Performs the CLIQUE algorithm on the given database.
     *
     * @param relation Data relation to process
     * @return Clustering result
     */
    public Clustering<SubspaceModel> run(Relation<V> relation) {
        // 1. Identification of subspaces that contain clusters
        // TODO: use step logging.
        if (LOG.isVerbose()) {
            LOG.verbose("*** 1. Identification of subspaces that contain clusters ***");
        }
        SortedMap<Integer, List<ExtendedCLIQUESubspace<V>>> dimensionToDenseSubspaces = new TreeMap<>();
        List<ExtendedCLIQUESubspace<V>> denseSubspaces = findOneDimensionalDenseSubspaces(relation);
        dimensionToDenseSubspaces.put(Integer.valueOf(0), denseSubspaces);
        if (LOG.isVerbose()) {
            LOG.verbose("    1-dimensional dense subspaces: " + denseSubspaces.size());
        }
        if (LOG.isDebugging()) {
            for (ExtendedCLIQUESubspace<V> s : denseSubspaces) {
                LOG.debug(s.toString("      "));
            }
        }

        long startTime = System.nanoTime();
        int dimensionality = RelationUtil.dimensionality(relation);
        for (int k = 2; k <= dimensionality && !denseSubspaces.isEmpty(); k++) {
            denseSubspaces = findDenseSubspaces(relation, denseSubspaces);
            dimensionToDenseSubspaces.put(Integer.valueOf(k - 1), denseSubspaces);
            if (LOG.isVerbose()) {
                LOG.verbose("    " + k + "-dimensional dense subspaces: " + denseSubspaces.size());
            }
            if (LOG.isDebugging()) {
                for (ExtendedCLIQUESubspace<V> s : denseSubspaces) {
                    LOG.debug(s.toString("      "));
                }
            }
        }
        System.out.println("Find multi-dimensionality: " + (System.nanoTime() - startTime) / 1000000000.0);

        // 2. Identification of clusters
        if (LOG.isVerbose()) {
            LOG.verbose("*** 2. Identification of clusters ***");
        }
        // build result
        int numClusters = 1;
        Clustering<SubspaceModel> result = new Clustering<>("CLIQUE clustering", "clique-clustering");


        // ToDo: Only consider maximum dimensionality, so no need for a loop here
//        for (Integer dim : dimensionToDenseSubspaces.keySet()) {
        Integer dim = dimensionToDenseSubspaces.lastKey();
        List<ExtendedCLIQUESubspace<V>> subspaces = dimensionToDenseSubspaces.get(dim);
        List<Pair<Subspace, ModifiableDBIDs>> modelsAndClusters = determineClusters(subspaces);

        if (LOG.isVerbose()) {
            LOG.verbose("    " + (dim + 1) + "-dimensional clusters: " + modelsAndClusters.size());
        }

        startTime = System.nanoTime();
        // ToDo: No need to loop over all vectors if model's dimensionality is below maximum
        for (Pair<Subspace, ModifiableDBIDs> modelAndCluster : modelsAndClusters) {
            ((ExtendedCLIQUESubspace) modelAndCluster.getFirst()).resetCoverage();
        }
        for (DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
            V featureVector = relation.get(it);
            for (Pair<Subspace, ModifiableDBIDs> modelAndCluster : modelsAndClusters) {
                ((ExtendedCLIQUESubspace) modelAndCluster.getFirst()).isContained(featureVector);
            }
        }
        for (Pair<Subspace, ModifiableDBIDs> modelAndCluster : modelsAndClusters) {
            // ToDo: Are too many candidate clusters created before being pruned?
            if (((ExtendedCLIQUESubspace) modelAndCluster.getFirst()).getCoverage() / ((double) relation.size()) >= tau) {
//                if(true){
                Cluster<SubspaceModel> newCluster = new Cluster<>(modelAndCluster.second);
                newCluster.setModel(new SubspaceModel(modelAndCluster.first, Centroid.make(relation, modelAndCluster.second)));
                newCluster.setName("cluster_" + numClusters++);
                result.addToplevelCluster(newCluster);
            }
        }
        System.out.println("Prune clusters: " + (System.nanoTime() - startTime) / 1000000000.0);
//        }

        return result;
    }

    /**
     * Determines the clusters in the specified dense subspaces.
     *
     * @param denseSubspaces the dense subspaces in reverse order by their
     *                       coverage
     * @return the clusters in the specified dense subspaces and the corresponding
     * cluster models
     */
    private List<Pair<Subspace, ModifiableDBIDs>> determineClusters(List<ExtendedCLIQUESubspace<V>> denseSubspaces) {
        List<Pair<Subspace, ModifiableDBIDs>> clusters = new ArrayList<>();

        for (ExtendedCLIQUESubspace<V> subspace : denseSubspaces) {
            List<Pair<Subspace, ModifiableDBIDs>> clustersInSubspace = subspace.determineClusters();
            if (LOG.isDebugging()) {
                LOG.debugFine("Subspace " + subspace + " clusters " + clustersInSubspace.size());
            }
            clusters.addAll(clustersInSubspace);
        }
        return clusters;
    }

    /**
     * Determines the one dimensional dense subspaces and performs a pruning if
     * this option is chosen.
     *
     * @param database the database to run the algorithm on
     * @return the one dimensional dense subspaces reverse ordered by their
     * coverage
     */
    private List<ExtendedCLIQUESubspace<V>> findOneDimensionalDenseSubspaces(Relation<V> database) {
        List<ExtendedCLIQUESubspace<V>> denseSubspaceCandidates = findOneDimensionalDenseSubspaceCandidates(database);

        if (prune) {
            return pruneDenseSubspaces(denseSubspaceCandidates);
        }

        return denseSubspaceCandidates;
    }

    /**
     * Determines the {@code k}-dimensional dense subspaces and performs a pruning
     * if this option is chosen.
     *
     * @param database       the database to run the algorithm on
     * @param denseSubspaces the {@code (k-1)}-dimensional dense subspaces
     * @return a list of the {@code k}-dimensional dense subspaces sorted in
     * reverse order by their coverage
     */
    private List<ExtendedCLIQUESubspace<V>> findDenseSubspaces(Relation<V> database, List<ExtendedCLIQUESubspace<V>> denseSubspaces) {
        List<ExtendedCLIQUESubspace<V>> denseSubspaceCandidates = findDenseSubspaceCandidates(database, denseSubspaces);

        if (prune) {
            return pruneDenseSubspaces(denseSubspaceCandidates);
        }

        return denseSubspaceCandidates;
    }

    /**
     * Initializes and returns the one dimensional units.
     *
     * @param database the database to run the algorithm on
     * @return the created one dimensional units
     */
    private ArrayList<ExtendedCliqueUnit<V>> initOneDimensionalUnits(Relation<V> database) {
        int dimensionality = RelationUtil.dimensionality(database);
        // initialize minima and maxima
        double[] minima = new double[dimensionality];
        double[] maxima = new double[dimensionality];
        for (int d = 0; d < dimensionality; d++) {
            maxima[d] = -Double.MAX_VALUE;
            minima[d] = Double.MAX_VALUE;
        }
        // update minima and maxima
        for (DBIDIter it = database.iterDBIDs(); it.valid(); it.advance()) {
            V featureVector = database.get(it);
            updateMinMax(featureVector, minima, maxima);
        }
        for (int i = 0; i < maxima.length; i++) {
            maxima[i] += 0.0001;
        }

        // determine the unit length in each dimension
        double[] unit_lengths = new double[dimensionality];
        for (int d = 0; d < dimensionality; d++) {
            unit_lengths[d] = (maxima[d] - minima[d]) / xsi;
        }

        if (LOG.isDebuggingFiner()) {
            StringBuilder msg = new StringBuilder();
            msg.append("   minima: ").append(FormatUtil.format(minima, ", ", FormatUtil.NF2));
            msg.append("\n   maxima: ").append(FormatUtil.format(maxima, ", ", FormatUtil.NF2));
            msg.append("\n   unit lengths: ").append(FormatUtil.format(unit_lengths, ", ", FormatUtil.NF2));
            LOG.debugFiner(msg.toString());
        }

        // determine the boundaries of the units
        double[][] unit_bounds = new double[xsi + 1][dimensionality];
        for (int x = 0; x <= xsi; x++) {
            for (int d = 0; d < dimensionality; d++) {
                if (x < xsi) {
                    unit_bounds[x][d] = minima[d] + x * unit_lengths[d];
                } else {
                    unit_bounds[x][d] = maxima[d];
                }
            }
        }
        if (LOG.isDebuggingFiner()) {
            StringBuilder msg = new StringBuilder();
            msg.append("   unit bounds ").append(FormatUtil.format(new Matrix(unit_bounds), "   "));
            LOG.debugFiner(msg.toString());
        }

        // build the 1 dimensional units
        ArrayList<ExtendedCliqueUnit<V>> units = new ArrayList<>((xsi * dimensionality));
        for (int d = 0; d < dimensionality; d++) {
            for (int x = 0; x < xsi; x++) {
//            for (int d = 0; d < dimensionality; d++) {
                units.add(new ExtendedCliqueUnit<V>(new CLIQUEInterval(d, unit_bounds[x][d], unit_bounds[x + 1][d])));
            }
        }

        if (LOG.isDebuggingFiner()) {
            StringBuilder msg = new StringBuilder();
            msg.append("   total number of 1-dim units: ").append(units.size());
            LOG.debugFiner(msg.toString());
        }

        return units;
    }

    /**
     * Updates the minima and maxima array according to the specified feature
     * vector.
     *
     * @param featureVector the feature vector
     * @param minima        the array of minima
     * @param maxima        the array of maxima
     */
    private void updateMinMax(V featureVector, double[] minima, double[] maxima) {
        if (minima.length != featureVector.getDimensionality()) {
            throw new IllegalArgumentException("FeatureVectors differ in length.");
        }
        for (int d = 0; d < featureVector.getDimensionality(); d++) {
            if ((featureVector.getMax(d)) > maxima[d]) {
                maxima[d] = (featureVector.getMax(d));
            }
            if ((featureVector.getMin(d)) < minima[d]) {
                minima[d] = (featureVector.getMin(d));
            }
//            if((featureVector.doubleValue(d)) > maxima[d]) {
//                maxima[d] = (featureVector.doubleValue(d));
//            }
//            if((featureVector.doubleValue(d)) < minima[d]) {
//                minima[d] = (featureVector.doubleValue(d));
//            }


        }
    }

    // Code from GeeksForGeeks
    private void binaryInsert(ArrayList<ExtendedCliqueUnit<V>> units, int l, int u, V vector) {
        assert u >= l;

        int j = 0;
        int lower = l;
        int upper = u;
        int curIn = 0;

        double value = 0;
        CLIQUEInterval a = null;
        while (lower <= upper) {

            curIn = (lower + upper) / 2;

            a = units.get(curIn).getIntervals().get(0);
            value = vector.getMin(a.getDimension());

            if (a.getMin() < value)
                lower = curIn + 1;
            else if (a.getMin() > value)
                upper = curIn - 1;
            else if (a.getMin() == value)
                break;
        }
//        if (a != null && a.getMin() <= value) {
//            j = curIn + 1;
//        } else {
//            j = curIn;
//        }
        j = curIn;

        units.get(j).addFeatureVector(null, vector);

        boolean terminate = false;
        while (!terminate) {
            j++;
            if (j > u || !units.get(j).addFeatureVector(null, vector)) {
                terminate = true;
            }
        }
    }

    /**
     * Determines the one-dimensional dense subspace candidates by making a pass
     * over the database.
     *
     * @param database the database to run the algorithm on
     * @return the one-dimensional dense subspace candidates reverse ordered by
     * their coverage
     */
    private List<ExtendedCLIQUESubspace<V>> findOneDimensionalDenseSubspaceCandidates(Relation<V> database) {
        ArrayList<ExtendedCliqueUnit<V>> units = initOneDimensionalUnits(database);
        // identify dense units
        double total = database.size();
        for (DBIDIter it = database.iterDBIDs(); it.valid(); it.advance()) {
            V featureVector = database.get(it);

            for (int i = 0; i < featureVector.getDimensionality(); i++) {
                binaryInsert(units, i * xsi, ((i + 1) * xsi) - 1, featureVector);
            }

//            for (ExtendedCliqueUnit<V> unit : units) {
//                unit.addFeatureVector(it, featureVector);
//            }
        }

        ArrayList<ExtendedCliqueUnit<V>> denseUnits = new ArrayList<>();
        Map<Integer, ExtendedCLIQUESubspace<V>> denseSubspaces = new HashMap<>();
        for (ExtendedCliqueUnit<V> unit : units) {
            // unit is a dense unit
            if (unit.selectivity(total) >= tau) {
                denseUnits.add(unit);
                // add the dense unit to its subspace
                int dim = unit.getIntervals().iterator().next().getDimension();
                ExtendedCLIQUESubspace<V> subspace_d = denseSubspaces.get(Integer.valueOf(dim));
                if (subspace_d == null) {
                    subspace_d = new ExtendedCLIQUESubspace<>(dim);
                    denseSubspaces.put(Integer.valueOf(dim), subspace_d);
                }

                subspace_d.addDenseUnit(unit);
            }
        }

        // Prune // ToDo: Make this code nicer
        for (ExtendedCLIQUESubspace<V> denseSubspace : denseSubspaces.values()) {
            ArrayList<ExtendedCliqueUnit<V>> prunedDenseUnits = new ArrayList<>();
            int i_ = 0;
            for (int i = 0; i < denseSubspace.getDenseUnits().size() - 1; i++) {
                if (!denseSubspace.getDenseUnits().get(i).containsLeftNeighbor(denseSubspace.getDenseUnits().get(i + 1).getIntervals().get(0))) {
                    if (i != i_) {
                        CLIQUEInterval firstInterval = denseSubspace.getDenseUnits().get(i_).getIntervals().get(0);
                        CLIQUEInterval secondInterval = denseSubspace.getDenseUnits().get(i).getIntervals().get(0);

                        prunedDenseUnits.add(new ExtendedCliqueUnit<>(new CLIQUEInterval(firstInterval.getDimension(),
                                firstInterval.getMin(),
                                secondInterval.getMax())));
                    } else {
                        prunedDenseUnits.add(denseSubspace.getDenseUnits().get(i));
                    }
                    i_ = i + 1;
                }
            }
            if (i_ < denseSubspace.getDenseUnits().size() - 1) {
                CLIQUEInterval firstInterval = denseSubspace.getDenseUnits().get(i_).getIntervals().get(0);
                CLIQUEInterval secondInterval = denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 1).getIntervals().get(0);

                prunedDenseUnits.add(new ExtendedCliqueUnit<>(new CLIQUEInterval(firstInterval.getDimension(),
                        firstInterval.getMin(),
                        secondInterval.getMax())));
            } else {
                if (!denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 2).containsRightNeighbor(denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 1).getIntervals().get(0))) {
                    CLIQUEInterval firstInterval = denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 2).getIntervals().get(0);
                    CLIQUEInterval secondInterval = denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 1).getIntervals().get(0);
                    prunedDenseUnits.remove(prunedDenseUnits.size() - 1);

                    prunedDenseUnits.add(new ExtendedCliqueUnit<>(new CLIQUEInterval(firstInterval.getDimension(),
                            firstInterval.getMin(),
                            secondInterval.getMax())));
                } else {
                    prunedDenseUnits.add(denseSubspace.getDenseUnits().get(denseSubspace.getDenseUnits().size() - 1));
                }
            }
            denseSubspace.setDenseUnits(prunedDenseUnits);
        }

        if (LOG.isDebugging()) {
            StringBuilder msg = new StringBuilder();
            msg.append("   number of 1-dim dense units: ").append(denseUnits.size());
            msg.append("\n   number of 1-dim dense subspace candidates: ").append(denseSubspaces.size());
            LOG.debugFine(msg.toString());
        }

        List<ExtendedCLIQUESubspace<V>> subspaceCandidates = new ArrayList<>(denseSubspaces.values());
        Collections.sort(subspaceCandidates, new ExtendedCLIQUESubspace.CoverageComparator());
        return subspaceCandidates;
    }

    /**
     * Determines the {@code k}-dimensional dense subspace candidates from the
     * specified {@code (k-1)}-dimensional dense subspaces.
     *
     * @param database       the database to run the algorithm on
     * @param denseSubspaces the {@code (k-1)}-dimensional dense subspaces
     * @return a list of the {@code k}-dimensional dense subspace candidates
     * reverse ordered by their coverage
     */
    private List<ExtendedCLIQUESubspace<V>> findDenseSubspaceCandidates(Relation<V> database, List<ExtendedCLIQUESubspace<V>> denseSubspaces) {
        // sort (k-1)-dimensional dense subspace according to their dimensions
        List<ExtendedCLIQUESubspace<V>> denseSubspacesByDimensions = new ArrayList<>(denseSubspaces);
        Collections.sort(denseSubspacesByDimensions, new Subspace.DimensionComparator());

        // determine k-dimensional dense subspace candidates
        double all = database.size();
        List<ExtendedCLIQUESubspace<V>> denseSubspaceCandidates = new ArrayList<>();

        while (!denseSubspacesByDimensions.isEmpty()) {
            ExtendedCLIQUESubspace<V> s1 = denseSubspacesByDimensions.remove(0);
            for (ExtendedCLIQUESubspace<V> s2 : denseSubspacesByDimensions) {
                ExtendedCLIQUESubspace<V> s = s1.join(s2, all, tau);
                if (s != null) {
                    denseSubspaceCandidates.add(s);

                    for (DBIDIter it = database.iterDBIDs(); it.valid(); it.advance()) {
                        V featureVector = database.get(it);
                        for (ExtendedCliqueUnit<V> unit : s.getDenseUnits()) {
                            unit.addFeatureVector(it, featureVector);
                        }
                    }

                }
            }
        }

        // sort reverse by coverage
        Collections.sort(denseSubspaceCandidates, new ExtendedCLIQUESubspace.CoverageComparator());
        return denseSubspaceCandidates;
    }

    /**
     * Performs a MDL-based pruning of the specified dense subspaces as described
     * in the CLIQUE algorithm.
     *
     * @param denseSubspaces the subspaces to be pruned sorted in reverse order by
     *                       their coverage
     * @return the subspaces which are not pruned reverse ordered by their
     * coverage
     */
    private List<ExtendedCLIQUESubspace<V>> pruneDenseSubspaces(List<ExtendedCLIQUESubspace<V>> denseSubspaces) {
        int[][] means = computeMeans(denseSubspaces);
        double[][] diffs = computeDiffs(denseSubspaces, means[0], means[1]);
        double[] codeLength = new double[denseSubspaces.size()];
        double minCL = Double.MAX_VALUE;
        int min_i = -1;

        for (int i = 0; i < denseSubspaces.size(); i++) {
            int mi = means[0][i];
            int mp = means[1][i];
            double log_mi = mi == 0 ? 0 : StrictMath.log(mi) / StrictMath.log(2);
            double log_mp = mp == 0 ? 0 : StrictMath.log(mp) / StrictMath.log(2);
            double diff_mi = diffs[0][i];
            double diff_mp = diffs[1][i];
            codeLength[i] = log_mi + diff_mi + log_mp + diff_mp;

            if (codeLength[i] <= minCL) {
                minCL = codeLength[i];
                min_i = i;
            }
        }

        return denseSubspaces.subList(0, min_i + 1);
    }

    /**
     * The specified sorted list of dense subspaces is divided into the selected
     * set I and the pruned set P. For each set the mean of the cover fractions is
     * computed.
     *
     * @param denseSubspaces the dense subspaces in reverse order by their
     *                       coverage
     * @return the mean of the cover fractions, the first value is the mean of the
     * selected set I, the second value is the mean of the pruned set P.
     */
    private int[][] computeMeans(List<ExtendedCLIQUESubspace<V>> denseSubspaces) {
        int n = denseSubspaces.size() - 1;

        int[] mi = new int[n + 1];
        int[] mp = new int[n + 1];

        double resultMI = 0;
        double resultMP = 0;

        for (int i = 0; i < denseSubspaces.size(); i++) {
            resultMI += denseSubspaces.get(i).getCoverage();
            resultMP += denseSubspaces.get(n - i).getCoverage();
            mi[i] = (int) Math.ceil(resultMI / (i + 1));
            if (i != n) {
                mp[n - 1 - i] = (int) Math.ceil(resultMP / (i + 1));
            }
        }

        int[][] result = new int[2][];
        result[0] = mi;
        result[1] = mp;

        return result;
    }

    /**
     * The specified sorted list of dense subspaces is divided into the selected
     * set I and the pruned set P. For each set the difference from the specified
     * mean values is computed.
     *
     * @param denseSubspaces denseSubspaces the dense subspaces in reverse order
     *                       by their coverage
     * @param mi             the mean of the selected sets I
     * @param mp             the mean of the pruned sets P
     * @return the difference from the specified mean values, the first value is
     * the difference from the mean of the selected set I, the second
     * value is the difference from the mean of the pruned set P.
     */
    private double[][] computeDiffs(List<ExtendedCLIQUESubspace<V>> denseSubspaces, int[] mi, int[] mp) {
        int n = denseSubspaces.size() - 1;

        double[] diff_mi = new double[n + 1];
        double[] diff_mp = new double[n + 1];

        double resultMI = 0;
        double resultMP = 0;

        for (int i = 0; i < denseSubspaces.size(); i++) {
            double diffMI = Math.abs(denseSubspaces.get(i).getCoverage() - mi[i]);
            resultMI += diffMI == 0.0 ? 0 : StrictMath.log(diffMI) / StrictMath.log(2);
            double diffMP = (i != n) ? Math.abs(denseSubspaces.get(n - i).getCoverage() - mp[n - 1 - i]) : 0;
            resultMP += diffMP == 0.0 ? 0 : StrictMath.log(diffMP) / StrictMath.log(2);
            diff_mi[i] = resultMI;
            if (i != n) {
                diff_mp[n - 1 - i] = resultMP;
            }
        }
        double[][] result = new double[2][];
        result[0] = diff_mi;
        result[1] = diff_mp;

        return result;
    }

    @Override
    public TypeInformation[] getInputTypeRestriction() {
        return TypeUtil.array(TypeUtil.NUMBER_VECTOR_FIELD);
    }

    @Override
    protected Logging getLogger() {
        return LOG;
    }

    /**
     * Parameterization class.
     *
     * @author Erich Schubert
     * @apiviz.exclude
     */
    public static class Parameterizer<V extends NumberVector> extends AbstractParameterizer {
        protected int xsi;

        protected double tau;

        protected boolean prune;

        @Override
        protected void makeOptions(Parameterization config) {
            super.makeOptions(config);
            IntParameter xsiP = new IntParameter(XSI_ID);
            xsiP.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
            if (config.grab(xsiP)) {
                xsi = xsiP.intValue();
            }

            DoubleParameter tauP = new DoubleParameter(TAU_ID);
            tauP.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
            tauP.addConstraint(CommonConstraints.LESS_THAN_ONE_DOUBLE);
            if (config.grab(tauP)) {
                tau = tauP.doubleValue();
            }

            Flag pruneF = new Flag(PRUNE_ID);
            if (config.grab(pruneF)) {
                prune = pruneF.isTrue();
            }
        }

        @Override
        protected CLIQUE<V> makeInstance() {
            return new CLIQUE<>(xsi, tau, prune);
        }
    }
}