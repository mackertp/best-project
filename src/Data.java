import java.io.*;
import java.util.Scanner;

public class Data {

    private DataArray msnbcData;
    private int usersProcessed; // represents how many lines are loaded
    private int totalUsers;
    private int categories;
    
    public Data(int totalUsers, int categories) {
        msnbcData = new DataArray(totalUsers, categories);
        this.totalUsers = totalUsers;
        this.categories = categories;
    }

    public void loadData(File dataFile) throws FileNotFoundException{
        int lineNum = 0;

        Scanner file = new Scanner(dataFile);

        while (file.hasNextLine()){
            Scanner line = new Scanner(file.nextLine());
            
            while (line.hasNextInt()){
                int category = line.nextInt() - 1; // views are stored in file as 1-17 but stored in program as 0-16
                msnbcData.setCategory(lineNum, category, msnbcData.getCategory(lineNum, category) + 1); // increment category
            }

            usersProcessed++;
            lineNum++;
        }

    }

    public int getUsersProcessed(){
        return usersProcessed;
    }

    public int getTotalUsers(){
        return totalUsers;
    }
}

