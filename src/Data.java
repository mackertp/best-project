import java.io.*;
import java.util.Scanner;

public class Data {

    DataArray msnbcData;
    
    public Data(File dataFile) throws FileNotFoundException{

        msnbcData = new DataArray(989818, 17);
        int lineNum = 0;

        Scanner file = new Scanner(dataFile);

        while (file.hasNextLine()){
            Scanner line = new Scanner(file.nextLine());
            lineNum+=1;
            
            while (line.hasNextInt()){
                int category = line.nextInt() - 1; // views are stored in file as 1-17 but stored in program as 0-16
                msnbcData.setCategory(lineNum, category, msnbcData.getCategory(lineNum, category) + 1); // increment category
            }
        }


    }
}

