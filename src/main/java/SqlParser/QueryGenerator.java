package SqlParser;


import java.util.Random;

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
}
