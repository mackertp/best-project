import java.io.File;
import java.io.FileNotFoundException;

public class App {
    private static Data data;
    private static int users = 989818;
    private static int categories = 17;

    public static void main(String[] args) {
        Thread loadThread = new Thread() { // anonymous thread to load data
            public void run() {
                try {
                    File dataFile = new File("datafile.txt");
                    data.loadData(dataFile);
                }
                catch (FileNotFoundException e){
                    // do nothing
                }
            }
        };
        data = new Data(users, categories);

        loadThread.start();

        while(loadThread.isAlive()){
            System.out.println(data.getUsersProcessed() + " out of " + data.getTotalUsers());
        }
    }
}
