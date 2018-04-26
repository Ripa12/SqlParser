package SqlParser;


import org.apache.commons.lang3.SystemUtils;

import javax.management.relation.Relation;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


/**
 * Created by Richard on 2018-03-27.
 */
public class QueryGenerator {

    final static String COLUMNS[] = {"A", "B", "C"};
    final static long RANGE[] = {0, 100000000L};
    final static int NR_OF_QUERIES = 100000;

    static String generateBatchOfQueries(){
        String sqlPrefix = "SELECT * FROM Customers WHERE ";
        StringBuilder stmts = new StringBuilder();

        Random rand = new Random();

        for(int k = 0; k < NR_OF_QUERIES; k++) {
            int selectedColumn = rand.nextInt(COLUMNS.length);
            stmts.append(sqlPrefix);
            if (rand.nextInt(3) > 0) {
                int start = 0;
                int end = 0;

                int random = rand.nextInt(11);
                if (random <= 9) { //This is 20% more
                    start = rand.nextInt(10000) + 1000;
                    end = rand.nextInt((100000)) + start;
                } else {
                    start = rand.nextInt(100000000);
                    end = rand.nextInt((100000)) + start;
                }

                stmts.append(COLUMNS[selectedColumn]).append(" < ").append(start).append(" AND ").append(end).append(" < ").append(COLUMNS[selectedColumn]).append(";\n");
            } else {
                int start = 0;

                int random = rand.nextInt(11);
                if (random <= 9) { //This is 20% more
                    start = rand.nextInt(10000) + 1000;
                } else {
                    start = rand.nextInt(100000000);
                }
                stmts.append(COLUMNS[selectedColumn]).append(" = ").append(start).append(";\n");
            }
        }

        return stmts.toString();
    }

    private final static int NR_OF_Q = 50000;
    private final static int MAX_DUPLICATES = 50;

    private final static int MAX_UPPER_BOUND = 99999999;

    private final static int FIRST_LOWER_BOUND = 0;
    private final static int FIRST_UPPER_BOUND = 250;

    private final static int SECOND_LOWER_BOUND = 150000;
    private final static int SECOND_UPPER_BOUND = 152000;

    static void generateCSV(String filename){
        String path = String.valueOf(QueryGenerator.class.getClassLoader().getResource(filename).getPath());
        if (SystemUtils.IS_OS_WINDOWS) {
            path = path.replaceFirst("/", "");
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(path)), "UTF-8"));

            Random rand = new Random();

            for(int i = 0; i < NR_OF_Q; i++) {
                if (false) {
                } else {
                    int random;
                    int[] row = new int[3];

                    for (int k = 0; k < 3; k++) {
                        random = rand.nextInt(101);
                        if (random <= 70) { //This is 20% more
                            row[k] = rand.nextInt((FIRST_UPPER_BOUND - FIRST_LOWER_BOUND) + 1) + FIRST_LOWER_BOUND;
                        } else if (random <= 90) {
                            row[k] = rand.nextInt((SECOND_UPPER_BOUND - SECOND_LOWER_BOUND) + 1) + SECOND_LOWER_BOUND;
                        } else {
                            row[k] = rand.nextInt(MAX_UPPER_BOUND);
                        }

                    }

                    int total = 1;
                    if (rand.nextInt(10) > 0)
                        total = rand.nextInt(MAX_DUPLICATES)+1;
                    for (int t = 0; t < total; t++) {
                        out.print(String.format("%d %d %d%n", row[0], row[1], row[2]));
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }

    public static MyRelation<MyVector> csvToRelation(String fname){

        String path = String.valueOf(QueryGenerator.class.getClassLoader().getResource(fname).getPath());
        if (SystemUtils.IS_OS_WINDOWS) {
            path = path.replaceFirst("/", "");
        }

        MyRelation<MyVector> relation = null;

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            relation = new MyRelation<>(3);
            for(String line; (line = br.readLine()) != null; ) {
                String[] strCols = line.split(" ");
                int[] intCols = new int[strCols.length];

                for(int i = 0; i < strCols.length; i++){
                    intCols[i] = Integer.parseInt(strCols[i]);
                }

                relation.insert(new MyPoint(intCols));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return relation;
    }
}
