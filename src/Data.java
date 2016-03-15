import java.io.*;
import java.util.Scanner;

public class Data {

    public Data(File dataFile) throws FileNotFoundException{
        Scanner file = new Scanner(dataFile);

        while (file.hasNextLine()){
            String line = file.nextLine();

        }

    }
}

