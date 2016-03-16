import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Data {

    private DataArray msnbcData;
    private int usersProcessed; // represents how many lines are loaded
    private int totalUsers;
    private int categories;
    private WorkerThread[] threadPool;
    LinkedBlockingQueue<Runnable> taskQueue;
    private CountDownLatch countLatch; // used to block in queries untill all tasks are complete

    /**
     * This class defines a task that will count how many users in its sublist that have visited category at least once.
     */
    private class CountTask implements Runnable {

        private int result;
        private int start;
        private int end;
        private int category;

        public CountTask(int start, int end, int category) {
            this.start = start;
            this.end = end;
            this.category = category;
        }

        private int getResult() {
            return result;
        }

        public void run() {
            int count = 0;
            for(int i=start; i<end; i++) {
                if(msnbcData.getCategory(i, category) > 0) {
                    count ++;
                }
            }
            result = count;
            countLatch.countDown();
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

    /**
     * Multithreaded query to tell if more than userThreshold users visited category
     *
     * @param userThreshold how many users must have visited category
     * @param category the category to consider
     * @return True if amount of visitors to category is >= to userThreshold. False otherwise.
     */
    public boolean countQuery(int userThreshold, int category) {
        int threadCount = threadPool.length;
        int dataSubsize = msnbcData.theArray.length / threadCount;
        CountTask[] tasks = new CountTask[threadCount]; // array of created tasks
        try {
            countLatch = new CountDownLatch(threadCount);
            // create the tasks and place them in the pool of tasks
            for (int i = 0; i < threadCount; i++){
                CountTask newTask = new CountTask(i * dataSubsize, (i + 1) * dataSubsize, category);
                tasks[i] = newTask;
                taskQueue.put(newTask);
            }
            countLatch.await(); // waits for all tasks to finish

            int sum = 0;
            // sum the results of the tasks
            for(CountTask task : tasks) {
                sum += task.getResult();
            }

            return sum > userThreshold;

        } catch (InterruptedException e) {
            return false;
        }
    }
}

