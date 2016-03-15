import java.util.AbstractList;

public class DataArray{
    int[][] theArray;

    public DataArray(int rows, int cols){
        theArray = new int[rows][cols];
    }

    public void setCategory(int user, int category, int val){
        theArray[user][category] = val;
    }

    public int getCategory(int user, int category){
        return theArray[user][category];
    }
}

