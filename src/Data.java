import java.io.*;
import java.util.Scanner;

public class Data {

    private DataArray msnbcData;
    private int linesProcessed; // represents how many lines are loaded
    private int totalLines = 989818;
    private int categories = 17;
    
    public Data(File dataFile) throws FileNotFoundException{

        msnbcData = new DataArray(totalLines, categories);
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

    public int getLinesProcessed(){
        return linesProcessed;
    }

    public int getTotalLines(){
        return totalLines;
    }
}

