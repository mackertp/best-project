
public class DataArray{

    private int[][] theArray;

    public DataArray(int rows, int cols){
        theArray = new int[rows][cols];
    }

    public void setCategory(int user, int category, int val){
        theArray[user][category] = val;
    }

    public int getCategory(int user, int category){
        return theArray[user][category];
    }

    /**
     * @return The amount of users stored
     */

    public int getUsers() {
        return theArray.length;
    }

    /**
     * @return The amount of categories stored
     */

    public  int getCategories() {
        return theArray[0].length;
    }
}

