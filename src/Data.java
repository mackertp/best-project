import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * The Data class is responsible for reading the data file into its
 * compressed format, and for querying the dataset.
 *
 * The queries are as follows:
 * The user is able to perform the following 5 distinct queries:
 * Are there more than _____ users who looked at X (countQuery)
 * What percent of users looked at X (percentageCountQuery)
 * Are there more users who looked at X than Y (comparisonQuery)
 * How many users viewed X _____ number of times (countThresholdQuery)
 * What percent of users looked at X more than Y (percentageComparisonQuery)
 *
 * @author Robert Bofinger, Preston Mackert
 *
 */

public class Data {

    final private int taskCount = 24; // How many tasks to create per query.

    private DataArray msnbcData;
    private int usersProcessed; // represents how many lines are loaded
    private int totalUsers;
    private int categories;
    private WorkerThread[] threadPool;
    LinkedBlockingQueue<Runnable> taskQueue;

    /**
     * This class defines a task that will count how many users in its subarray that have visited category by atleast
     * a certain threshold.
     */
    private class CountTask implements Runnable {

        private int result;
        private int start;
        private int end;
        private int category;
        private int threshold;
        private CountDownLatch latch; // used to keep track of how many tasks are still working/queued.

        /**
         * Creates a new task to count how many users visited a category in a given sublist
         * @param start starting index (inclusive)
         * @param end ending index (exclusive)
         * @param category category to count
         * @param latch latch to signal when task is done
         */
        public CountTask(int start, int end, int category, CountDownLatch latch) {
            this(start, end, category, 1, latch);
        }

        /**
         * Creates a new task to count how many users visited a category in a given sublist
         * @param start starting index (inclusive)
         * @param end ending index (exclusive)
         * @param category category to count
         * @param threshold minimum amount of visits to a page needed for it to be counted
         * @param latch latch to signal when task is done
         */
        public CountTask(int start, int end, int category, int threshold, CountDownLatch latch) {
            this.start = start;
            this.end = end;
            this.category = category;
            this.latch = latch;
            this.threshold = threshold;

            if (this.start > msnbcData.getUsers()) {
                // if is out of range we should fix it.
                this.start = msnbcData.getUsers() -1 ;
            }

            if (this.end > msnbcData.getUsers()) {
                // if end is out of range we should fix it.
                // this case can easily happen during the division step of the query algorithm
                this.end = msnbcData.getUsers();
            }
        }

        /**
         * @return the result of the task
         */
        public int getResult() {
            return result;
        }

        public void run() {
            int count = 0;
            for(int i=start; i<end; i++) {

                if(msnbcData.getCategory(i, category) >= threshold) {
                    count ++;
                }
            }
            result = count;
            latch.countDown();
        }
    }

    /**
     * This class defines a task that counts how many users in a subarray that have visited one category more than
     * another category.
     */
    private class CompareTask implements Runnable{
        private int result;
        private int start;
        private int end;
        private int category1;
        private int category2;
        private CountDownLatch latch;

        /**
         * Creates a new task to count how many users have visited category1 more than category2
         * @param start starting index of subarray
         * @param end ending index of subarray
         * @param category1 category to check for more visits
         * @param category2 category to check visits against
         * @param latch latch to signal when task is done
         */
        public CompareTask (int start, int end, int category1, int category2, CountDownLatch latch) {
            this.start = start;
            this.end = end;
            this.category1 = category1;
            this.category2 = category2;
            this.latch = latch;

            if (this.start > msnbcData.getUsers()) {
                this.start = msnbcData.getUsers() - 1;
            }

            if (this.end > msnbcData.getUsers()) {
                this.end = msnbcData.getUsers();
            }
        }

        /**
         * @return The result of the task
         */
        public int getResult(){
            return result;
        }

        /**
         * runs the task
         */
        public void run(){
            int count = 0;
            for(int i=start; i<end; i++){
                if(msnbcData.getCategory(i, category1) > msnbcData.getCategory(i, category2)){
                    count++;
                }
            }
            result = count;
            latch.countDown();
        }
    }


    /**
     * This class defines the worker threads that make up the thread pool.
     * A WorkerThread runs in a loop in which it retrieves a task from the
     * taskQueue and calls the run() method in that task.  Note that if
     * the queue is empty, the thread blocks until a task becomes available
     * in the queue.  The constructor starts the thread, so there is no
     * need for the main program to do so.  The thread will run at a priority
     * that is one less than the priority of the thread that calls the
     * constructor.
     *
     * A WorkerThread is designed to run in an infinite loop.  It will
     * end only when the Java virtual machine exits. (This assumes that
     * the tasks that are executed don't throw exceptions, which is true
     * in this program.)  The constructor sets the thread to run as
     * a daemon thread; the Java virtual machine will exit automatically when
     * the only threads are daemon threads.  (In this program, this is not
     * necessary since the virtual machine is set to exit when the
     * window is closed.  In a multi-window program, however, we can't
     * simply end the program when a window is closed.)
     *
     * @author David Eck
     *
     */

    private class WorkerThread extends Thread {
        WorkerThread() {
            try {
                setPriority( Thread.currentThread().getPriority() - 1);
            }
            catch (Exception e) {
            }
            try {
                setDaemon(true);
            }
            catch (Exception e) {
            }
            start(); // Thread starts as soon as it is constructed.
        }
        public void run() {
            while (true) {
                try {
                    Runnable task = taskQueue.take(); // wait for task if necessary
                    task.run();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Constructs a new Data class
     * @param totalUsers the total amount of users in the data file
     * @param categories the total amount of categories in the data file
     */
    public Data(int totalUsers, int categories) {
        msnbcData = new DataArray(totalUsers, categories);
        this.totalUsers = totalUsers;
        this.categories = categories;
        taskQueue = new LinkedBlockingQueue<Runnable>();

        // Create the worker thread pool
        int processors = Runtime.getRuntime().availableProcessors();
        threadPool = new WorkerThread[processors];
        for (int i = 0; i < processors; i++) {
            threadPool[i] = new WorkerThread();
        }
    }

    /**
     * Loads data from a file into the class. Use getUsersProcessed() to get the progress of loading.
     * @param dataFile a file containing formatted data
     * @throws IOException
     */
    public void loadData(File dataFile) throws IOException{
        int lineNum = 0;

        BufferedReader br = new BufferedReader(new FileReader(dataFile));
        String line;
        Pattern splitSpace = Pattern.compile(" ");

        while ((line = br.readLine()) != null){
            String[] numbers = splitSpace.split(line);

            for (String number : numbers){
                int category = Integer.parseInt(number) - 1; // views are stored in file as 1-17 but stored in program as 0-16
                msnbcData.setCategory(lineNum, category, msnbcData.getCategory(lineNum, category) + 1); // increment category
            }

            usersProcessed++;
            lineNum++;
        }

        br.close();

    }

    /**
     * Used to check on the progress of loading the file
     * @return the amount of users who have been processed from the file so far
     */
    public int getUsersProcessed(){
        return usersProcessed;
    }

    /**
     * @return the total amount of users in the dataset
     */
    public int getTotalUsers(){
        return totalUsers;
    }

    /**
     * Using multithreading, count the amount of users who have visited a given category at least once.
     *
     * @param category the category to consider
     * @return the count of uses who have visited category.
     */

    private int countUsersByCategory(int category) throws InterruptedException {
        return countUsersByCategory(1, category);
    }

    /**
     * Using multithreading, count the amount of users who have visited a given category by atleast a given threshold.
     *
     * @param threshold how many visits to a category must be made for the user to be counted
     * @param category the category to consider
     * @return the count of uses who have visited category.
     */
    private int countUsersByCategory(int threshold, int category) throws InterruptedException {
        // dataSubsize is the size of each sublist. We use float division and round up to make sure we don't come up short.
        int dataSubsize = (int) Math.ceil((double) msnbcData.getUsers() / (double) taskCount);
        CountTask[] tasks = new CountTask[taskCount]; // array of created tasks
        CountDownLatch countLatch = new CountDownLatch(taskCount); // lets us block until all tasks finish.
        // create the tasks and place them in the pool of tasks
        for (int i = 0; i < taskCount; i++) {
            CountTask newTask = new CountTask(i * dataSubsize, (i + 1) * dataSubsize, category, threshold, countLatch);
            tasks[i] = newTask;
            taskQueue.put(newTask);
        }
        countLatch.await(); // waits for all tasks to finish
        int sum = 0;
        // sum the results of the tasks
        for (CountTask task : tasks) {
            sum += task.getResult();
        }
        return sum;
    }

    /**
     * Using multithreading, count the amount of users who a visited a given category more than another category.
     *
     * @param category1 category to consider
     * @param category2 category to check again
     * @return the amount of users who have visited category1 more than category2
     * @throws InterruptedException
     */
    private int countUsersByComparison(int category1, int category2) throws  InterruptedException {
        // dataSubsize is the size of each sublist. We use float division and round up to make sure we don't come up short.
        int dataSubsize = (int) Math.ceil((double) msnbcData.getUsers() / (double) taskCount);
        CompareTask[] tasks = new CompareTask[taskCount]; // array of created tasks
        CountDownLatch countLatch = new CountDownLatch(taskCount); // lets us block until all tasks finish.
        // create the tasks and place them in the pool of tasks
        for (int i = 0; i < taskCount; i++) {
            CompareTask newTask = new CompareTask(i * dataSubsize, (i + 1) * dataSubsize, category1, category2, countLatch);
            tasks[i] = newTask;
            taskQueue.put(newTask);
        }
        countLatch.await(); // waits for all tasks to finish
        int sum = 0;
        // sum the results of the tasks
        for (CompareTask task : tasks) {
            sum += task.getResult();
        }
        return sum;
    }

    /**
     * Multithreaded query to tell if more than userThreshold users visited category.
     * Relates to: Are there more than ____ users who looked at X?
     *
     * @param userThreshold how many users must have visited category
     * @param category the category to consider
     * @return True if amount of visitors to category is greater than or equal to userThreshold. False otherwise.
     */

    public boolean countQuery(int userThreshold, int category) {
        try {
            return countUsersByCategory(category) > userThreshold;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Multithreaded query to calculate the percentage of users who visited a category.
     * Relates to: What percent of users looked at X?
     *
     * @param category the category to consider
     * @return a float from 0 to 100 inclusive representing the percent of users who visited category. -1 if error.
     */

    public float percentageCountQuery(int category) {
        try {
            return ((float)countUsersByCategory(category) / (float)msnbcData.getUsers())*100;
        } catch (InterruptedException e) {
            return -1;
        }
    }

    /**
     * Multithreaded query to calculate if more users looked at category1 than category 2.
     * Relates to: Are there more users who looked at X than Y?
     *
     * @param category1 first category
     * @param category2 second category
     * @return true if more users visited category1 than category2
     */
    public boolean comparisonQuery(int category1, int category2){
        try{
            return countUsersByCategory(category1) > countUsersByCategory(category2);
        }
        catch (InterruptedException e){
            return false;
        }
    }

    /**
     * Multithreaded query to calculate the amount of users who have visited a category at least a certain amount of times.
     * Relates to: How many users viewed X ___ (or more) number of times?
     *
     * @param threshold how many visits to a category a user must have made to be counted
     * @param category category to consider
     * @return number of users who visited category at least threshold times
     */
    public int countThresholdQuery(int threshold, int category){
        try {
            return countUsersByCategory(threshold, category);
        }
        catch (InterruptedException e){
            return -1;
        }
    }

    /**
     * Multithreaded query to calculate the percent of users that viewed one category more than another category.
     * Relates to: What percent of users looked at X more than Y?
     *
     * @param category1 the first category to consider
     * @param category2 the second category to consider
     * @return percentage of users who visited which category more.
    */
    public float comparePercentageQuery(int category1, int category2){
        try {
            return (float)countUsersByComparison(category1, category2)/(float)msnbcData.getUsers();
        } catch (InterruptedException e){
            return -1;
        }
    }
}

