import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by bofinger on 3/16/2016.
 */
public class DataTest {

    @org.junit.Test
    public void testCountQuery() throws Exception {
        File file = new File("testdatafile.txt");
        Data data = new Data(62, 17);
        data.loadData(file);
        assertEquals(true, data.countQuery(3, 1));
    }
}