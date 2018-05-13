/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package SqlParser;

import de.lmu.ifi.dbs.elki.algorithm.clustering.GriDBSCAN;
import de.lmu.ifi.dbs.elki.algorithm.clustering.correlation.LMCLUS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.gdbscan.LSDBC;
import de.lmu.ifi.dbs.elki.algorithm.clustering.onedimensional.KNNKernelDensityMinimaClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.DOC;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.PROCLUS;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.model.ClusterModel;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.filter.FixedDBIDsFilter;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import org.junit.Test;


import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.database.Database;
import smile.clustering.BIRCH;
import smile.clustering.DENCLUE;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
/**
 * Performs a full SUBCLU run, and compares the result with a clustering derived
 * from the data set labels. This test ensures that SUBCLU performance doesn't
 * unexpectedly drop on this data set (and also ensures that the algorithms
 * work, as a side effect).
 *
 * @author Elke Achtert
 * @author Katharina Rausch
 * @author Erich Schubert
 * @since 0.3
 */
public class SUBCLUTest {

    /**
     * Base path for unit test files.
     */
    public final static String UNITTEST = "data/testdata/unittests/";


    /**
     * Run SUBCLU with fixed parameters and compare the result to a golden
     * standard.
     */
    @Test
    public void testSUBCLUResults() {
//        Database db = makeSimpleDatabase(UNITTEST + "3clusters-and-noise-2d.csv", 6070);

        Database db = makeSimpleDatabase(UNITTEST + "test_data.csv", 0);

//        PROCLUS<DoubleVector> pclus = new PROCLUS(3, 3, 1,1, new RandomFactory(12));
//        Clustering<?> result =  pclus.run(db);


        Clustering<?> result = new DOC<DoubleVector>(.3, .2, .1, true, 0, new RandomFactory(22))
                .run(db);

//        Clustering<Model> result = new LSDBC<MyVector>(EuclideanDistanceFunction.STATIC, 20, .4).run(db);

//        Clustering<Model> result = new LMCLUS(3, 100, 2, 0.3, new RandomFactory(6))
//                .run(db);

//        Clustering<Model> result = new GriDBSCAN<DoubleVector>(EuclideanDistanceFunction.STATIC, .1,
//                10, 100).run(db);

//        Clustering<?> result = new ELKIBuilder<DOC<DoubleVector>>(DOC.class) //
//                .with(DOC.Parameterizer.RANDOM_ID, 0) //
//                .with(DOC.Parameterizer.ALPHA_ID, 0.4) //
//                .with(DOC.Parameterizer.BETA_ID, 0.85) //
//                .build().run(db);

        for (Cluster<?> cl : result.getAllClusters()) {
            System.out.println(((SubspaceModel)cl.getModel()).getMean());
            System.out.println(cl.getModel().getClass().getName());
            System.out.println(cl.getIDs().size());
//            cl.getModel()
//
//
////            if(cl.getModel().getSubspace() instanceof ExtendedCLIQUESubspace) {
////                ExtendedCLIQUESubspace cliqueSubspace = (ExtendedCLIQUESubspace) cl.getModel().getSubspace();
////                System.out.print(" -- Subspace -- \n");
////                List<ExtendedCliqueUnit> units = cliqueSubspace.getDenseUnits();
////                System.out.println("Coverage: " + cliqueSubspace.getCoverage());
////
////                System.out.println("Dimension: " + cliqueSubspace.dimensonsToString());
////                System.out.println("Dimension: " + cliqueSubspace.dimensionality());
////
////                double dimensions[][] = new double[cliqueSubspace.dimensionality()][2];
////                for (double[] row: dimensions){
////                    row[0] = Double.MAX_VALUE;
////                    row[1] = Double.MIN_VALUE;
////                }
////
////                for (ExtendedCliqueUnit unit : units) {
////
////                    System.out.print("\t -- Unit -- \n");
////                    System.out.println("\tFeature vectors: " + unit.numberOfFeatureVectors());
////                    System.out.println("\tSelectivity: " + unit.selectivity(unit.numberOfFeatureVectors()));
////                    ArrayList<CLIQUEInterval> intervals = unit.getIntervals();
////
////                    int k = 0;
////                    for (CLIQUEInterval interval : intervals) {
////                        System.out.print("\t\t -- Interval -- \n");
////                        System.out.print("\t\tDimension: " + interval.getDimension() + ", ");
////                        System.out.print("\t\tMax: " + interval.getMax() + ", ");
////                        System.out.println("\t\tMin: " + interval.getMin());
////
////                        dimensions[k][0] = Math.min(dimensions[k][0], interval.getMin());
////                        dimensions[k][1] = Math.max(dimensions[k][1], interval.getMax());
////                        k++;
////                    }
////                }
////                for (double[] dimension : dimensions) {
////                    System.out.print("\t\t -- Combined Interval -- \n");
////                    System.out.print("\t\t\t Min: " + dimension[0]);
////                    System.out.println("\t\t\t Max: " + dimension[1]);
////                }
////
////            }

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

//        testParameterizationOk(params);

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
}