package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEInterval;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUESubspace;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.clique.CLIQUEUnit;
import de.lmu.ifi.dbs.elki.algorithm.clustering.trivial.ByLabelClustering;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.filter.FixedDBIDsFilter;
import de.lmu.ifi.dbs.elki.evaluation.clustering.ClusterContingencyTable;
import de.lmu.ifi.dbs.elki.evaluation.outlier.OutlierROCCurve;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CliqueTest {
    /**
     * Run CLIQUE with fixed parameters and compare the result to a golden
     * standard.
     *
     * @throws ParameterException
     */
    @Test
    public void testCLIQUEResults() {
//        Database db = makeSimpleDatabase(UNITTEST + "subspace-simple.csv", 600);
        Database db = makeSimpleDatabase(UNITTEST + "test_data.csv", 18);

        ListParameterization params = new ListParameterization();
        params.addParameter(CLIQUE.TAU_ID, "0.05");
        params.addParameter(CLIQUE.XSI_ID, 20);

        // setup algorithm
        CLIQUE<DoubleVector> clique = ClassGenericsUtil.parameterizeOrAbort(CLIQUE.class, params);
        testParameterizationOk(params);

        // run CLIQUE on database
        Clustering<SubspaceModel> result = clique.run(db);

        List<Integer> sizes = new java.util.ArrayList<>();
        for (Cluster<SubspaceModel> cl : result.getAllClusters()) {
            sizes.add(cl.size());

            if(cl.getModel().getSubspace() instanceof CLIQUESubspace) {
                CLIQUESubspace cliqueSubspace = (CLIQUESubspace) cl.getModel().getSubspace();
                System.out.print(" -- Subspace -- \n");
                List<CLIQUEUnit> units = cliqueSubspace.getDenseUnits();
                System.out.println("Coverage: " + cliqueSubspace.getCoverage());

                System.out.println("Dimension: " + cliqueSubspace.dimensonsToString());
                System.out.println("Dimension: " + cliqueSubspace.dimensionality());

                double dimensions[][] = new double[cliqueSubspace.dimensionality()][2];
                for (double[] row: dimensions){
                    row[0] = Double.MAX_VALUE;
                    row[1] = Double.MIN_VALUE;
                }

                for (CLIQUEUnit unit : units) {

                    System.out.print("\t -- Unit -- \n");
                    System.out.println("\tFeature vectors: " + unit.numberOfFeatureVectors());
                    System.out.println("\tSelectivity: " + unit.selectivity(unit.numberOfFeatureVectors()));
                    ArrayList<CLIQUEInterval> intervals = unit.getIntervals();

                    int k = 0;
                    for (CLIQUEInterval interval : intervals) {
                        System.out.print("\t\t -- Interval -- \n");
                        System.out.print("\t\tDimension: " + interval.getDimension() + ", ");
                        System.out.print("\t\tMax: " + interval.getMax() + ", ");
                        System.out.println("\t\tMin: " + interval.getMin());

                        dimensions[k][0] = Math.min(dimensions[k][0], interval.getMin());
                        dimensions[k][1] = Math.max(dimensions[k][1], interval.getMax());
                        k++;
                    }
                }
                for (double[] dimension : dimensions) {
                    System.out.print("\t\t -- Combined Interval -- \n");
                    System.out.print("\t\t\t Min: " + dimension[0]);
                    System.out.println("\t\t\t Max: " + dimension[1]);
                }

            }

        }


        // Sort both
        Collections.sort(sizes);
        // Report
        // if(logger.isVerbose()) {
        StringBuilder buf = new StringBuilder();
        buf.append("Cluster sizes: [");
        for (int i = 0; i < sizes.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(sizes.get(i));
        }
        buf.append("]");

        System.out.println(buf.toString());

        List<Clustering<? extends Model>> clusterresults = ResultUtil.getClusteringResults(result);
        assertTrue("No unique clustering found in result.", clusterresults.size() == 1);
        Clustering<? extends Model> clustering = clusterresults.get(0);

        // PairCounting is not appropriate here: overlapping clusterings!
        // testFMeasure(db, result, 0.9882);
//        testClusterSizes(result, new int[] { 200, 200, 216, 400 });
    }

    /**
     * Run CLIQUE with fixed parameters and compare the result to a golden
     * standard.
     *
     * @throws ParameterException
     */
    @Test
    public void testCLIQUESubspaceOverlapping() {
        Database db = makeSimpleDatabase(UNITTEST + "subspace-overlapping-3-4d.ascii", 850);

        // Setup algorithm
        ListParameterization params = new ListParameterization();
        params.addParameter(CLIQUE.TAU_ID, 0.2);
        params.addParameter(CLIQUE.XSI_ID, 6);
        CLIQUE<DoubleVector> clique = ClassGenericsUtil.parameterizeOrAbort(CLIQUE.class, params);
        testParameterizationOk(params);

        // run CLIQUE on database
        Clustering<SubspaceModel> result = clique.run(db);
        // PairCounting is not appropriate here: overlapping clusterings!
        // testFMeasure(db, result, 0.433661);
        testClusterSizes(result, new int[] { 255, 409, 458, 458, 480 });

        List<Integer> sizes = new java.util.ArrayList<>();
        for (Cluster<?> cl : result.getAllClusters()) {
            sizes.add(cl.size());
        }
        // Sort both
        Collections.sort(sizes);
        // Report
        // if(logger.isVerbose()) {
        StringBuilder buf = new StringBuilder();
        buf.append("Cluster sizes: [");
        for (int i = 0; i < sizes.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(sizes.get(i));
        }
        buf.append("]");

//        for (Cluster<SubspaceModel> cluster : result.getAllClusters()) {
//            System.out.println(cluster.getModel().);
//        }

        System.out.println(result.getAllClusters().size());
    }

    /**
     * Base path for unit test files.
     */
    public final static String UNITTEST = "data/testdata/unittests/";

    /**
     * Notice: this is okay for tests - don't use this for frequently used
     * objects, use a static instance instead!
     */
    protected Logging logger = Logging.getLogger(this.getClass());

    /**
     * Validate that parameterization succeeded: no parameters left, no
     * parameterization errors.
     *
     * @param config Parameterization to test
     */
    protected void testParameterizationOk(ListParameterization config) {
        if (config.hasUnusedParameters()) {
            fail("Unused parameters: " + config.getRemainingParameters());
        }
        if (config.hasErrors()) {
            config.logAndClearReportedErrors();
            fail("Parameterization errors.");
        }
    }

    /**
     * Generate a simple DoubleVector database from a file.
     *
     * @param filename File to load
     * @param expectedSize Expected size in records
     * @param params Extra parameters
     * @return Database
     */
    protected <T> Database makeSimpleDatabase(String filename, int expectedSize, ListParameterization params, Class<?>[] filters) {
        assertTrue("Test data set not found: " + filename, (new File(filename)).exists());
        params.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, filename);

        List<Class<?>> filterlist = new ArrayList<>();
        filterlist.add(FixedDBIDsFilter.class);
        if (filters != null) {
            for (Class<?> filter : filters) {
                filterlist.add(filter);
            }
        }
        params.addParameter(FileBasedDatabaseConnection.Parameterizer.FILTERS_ID, filterlist);
        params.addParameter(FixedDBIDsFilter.Parameterizer.IDSTART_ID, 1);
        Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);

        testParameterizationOk(params);

        db.initialize();
        Relation<?> rel = db.getRelation(TypeUtil.ANY);
//        org.junit.Assert.assertEquals("Database size does not match.", expectedSize, rel.size());
        return db;
    }

    /**
     * Generate a simple DoubleVector database from a file.
     *
     * @param filename File to load
     * @param expectedSize Expected size in records
     * @return Database
     */
    protected <T> Database makeSimpleDatabase(String filename, int expectedSize) {
        return makeSimpleDatabase(filename, expectedSize, new ListParameterization(), null);
    }

    /**
     * Find a clustering result, fail if there is more than one or none.
     *
     * @param result Base result
     * @return Clustering
     */
    protected Clustering<?> findSingleClustering(Result result) {
        List<Clustering<? extends Model>> clusterresults = ResultUtil.getClusteringResults(result);
        assertTrue("No unique clustering found in result.", clusterresults.size() == 1);
        Clustering<? extends Model> clustering = clusterresults.get(0);
        return clustering;
    }

    /**
     * Test the clustering result by comparing the score with an expected value.
     *
     * @param database Database to test
     * @param clustering Clustering result
     * @param expected Expected score
     */
    protected <O> void testFMeasure(Database database, Clustering<?> clustering, double expected) {
        // Run by-label as reference
        ByLabelClustering bylabel = new ByLabelClustering();
        Clustering<Model> rbl = bylabel.run(database);

        ClusterContingencyTable ct = new ClusterContingencyTable(true, false);
        ct.process(clustering, rbl);
        double score = ct.getPaircount().f1Measure();
        if (logger.isVerbose()) {
            logger.verbose(this.getClass().getSimpleName() + " score: " + score + " expect: " + expected);
        }
        org.junit.Assert.assertEquals(this.getClass().getSimpleName() + ": Score does not match.", expected, score, 0.0001);
    }

    /**
     * Validate the cluster sizes with an expected result.
     *
     * @param clustering Clustering to test
     * @param expected Expected cluster sizes
     */
    protected void testClusterSizes(Clustering<?> clustering, int[] expected) {
        List<Integer> sizes = new java.util.ArrayList<>();
        for (Cluster<?> cl : clustering.getAllClusters()) {
            sizes.add(cl.size());
        }
        // Sort both
        Collections.sort(sizes);
        Arrays.sort(expected);
        // Report
        // if(logger.isVerbose()) {
        StringBuilder buf = new StringBuilder();
        buf.append("Cluster sizes: [");
        for (int i = 0; i < sizes.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(sizes.get(i));
        }
        buf.append("]");
        // }
        // Test
        org.junit.Assert.assertEquals("Number of clusters does not match expectations. " + buf.toString(), expected.length, sizes.size());
        for (int i = 0; i < expected.length; i++) {
            org.junit.Assert.assertEquals("Cluster size does not match at position " + i + " in " + buf.toString(), expected[i], (int) sizes.get(i));
        }
    }

//    /**
//     * Test the AUC value for an outlier result.
//     *
//     * @param db Database
//     * @param positive Positive class name
//     * @param result Outlier result to process
//     * @param expected Expected AUC value
//     */
//    protected void testAUC(Database db, String positive, OutlierResult result, double expected) {
//        ListParameterization params = new ListParameterization();
//        params.addParameter(OutlierROCCurve.POSITIVE_CLASS_NAME_ID, positive);
//        OutlierROCCurve rocCurve = ClassGenericsUtil.parameterizeOrAbort(OutlierROCCurve.class, params);
//
//        // Ensure the result has been added to the hierarchy:
//        if (db.getHierarchy().numParents(result) < 1) {
//            db.getHierarchy().add(db, result);
//        }
//
//        // Compute ROC and AUC:
//        rocCurve.processNewResult(db, result);
//        // Find the ROC results
//        Collection<OutlierROCCurve.ROCResult> rocs = ResultUtil.filterResults(result, OutlierROCCurve.ROCResult.class);
//        assertTrue("No ROC result found.", !rocs.isEmpty());
//        double auc = rocs.iterator().next().getAUC();
//        org.junit.Assert.assertFalse("More than one ROC result found.", rocs.size() > 1);
//        org.junit.Assert.assertEquals("ROC value does not match.", expected, auc, 0.0001);
//    }

    /**
     * Test the outlier score of a single object.
     *
     * @param result Result object to use
     * @param id Object ID
     * @param expected expected value
     */
    protected void testSingleScore(OutlierResult result, int id, double expected) {
        org.junit.Assert.assertNotNull("No outlier result", result);
        org.junit.Assert.assertNotNull("No score result.", result.getScores());
        final DBID dbid = DBIDUtil.importInteger(id);
        org.junit.Assert.assertNotNull("No result for ID " + id, result.getScores().get(dbid));
        double actual = result.getScores().get(dbid);
        org.junit.Assert.assertEquals("Outlier score of object " + id + " doesn't match.", expected, actual, 0.0001);
    }
}
