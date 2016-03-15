
public class DataArray {
    int[][] theArray;

    public DataArray(int rows, int cols){
        theArray = new int[rows][cols];
    }

    public void setData(int[][] dataArray){
        theArray = dataArray;
    }
}

