import org.junit.Before;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by bofinger on 3/16/2016.
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