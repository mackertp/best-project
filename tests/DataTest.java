import org.junit.Before;

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
        assertEquals(true, data.countQuery(3, 1));
    }

    @org.junit.Test
    public void testPercentageCountQuery() throws Exception {
        assertEquals(12f / 62f, data.percentageCountQuery(0), 0.0f);
    }

}