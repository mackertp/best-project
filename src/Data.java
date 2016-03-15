import java.io.*;
import java.util.Scanner;

public class Data {

    public Data(File dataFile) throws FileNotFoundException{

        int[][] dataArray = new int[989818][17];
        int lineNum = 0;

        Scanner file = new Scanner(dataFile);

        while (file.hasNextLine()){

            String line = file.nextLine();
            lineNum+=1;
            
            for (int i=0; i<line.length(); i++){
                int view = (int)line.charAt(i);
                dataArray[lineNum][view] += 1;
            }
        }
    }
}

