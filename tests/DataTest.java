import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * JUnit test for the data class, it tests all of the queries on a small testing data file so that we can make sure it
 * is returning the values that we know.
 *
 * @author Robert Bofinger
 * @author Preston Mackert
 *
 */

public class DataTest {

    Data data;

    @Before
    public void setUp() throws Exception {
        File file = new File("testdatafile.txt");
        data = new Data(62, 17);
        data.loadData(file);
    }

    @org.junit.Test
    public void testCountQuery() throws Exception {
        // Are there more than ____ users who looked at X
        assertEquals(true, data.countQuery(3, 1));
    }

    @org.junit.Test
    public void testPercentageCountQuery() throws Exception {
        // What percent of users looked at X
        assertEquals(12f / 62f, data.percentageCountQuery(0), 0.0f);
    }

    @org.junit.Test
    public void testComparisonQuery() throws Exception {
        // Are there more users who looked at X than Y
        assertEquals(true, data.compairisonQuery(0, 3));
    }

}