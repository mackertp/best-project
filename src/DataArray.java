/**
 * This class provides a way to store the dataset efficiently. It is effectively a wrapper for a 2d array
 */
public class DataArray{

    private int[][] theArray;

    /**
     * Creates a new DataArray with the given amount of rows and columns
     * @param rows how many rows or users in the dataset
     * @param cols how many columns or categories are in the dataset
     */
    public DataArray(int rows, int cols){theArray = new int[rows][cols];}

    /**
     * Sets the count of the category to a given value for a given user
     * @param user user whose category to change
     * @param category category to change
     * @param val new value of category
     */
    public void setCategory(int user, int category, int val){theArray[user][category] = val;}

    /**
     * gets the count of a users category
     * @param user user whose category to get
     * @param category category to get
     * @return value stored for the users category
     */
    public int getCategory(int user, int category){return theArray[user][category];}

    /**
     * @return The amount of users stored
     */

    public int getUsers() {return theArray.length;}

    /**
     * @return The amount of categories stored
     */

    public  int getCategories() {return theArray[0].length;}
}

