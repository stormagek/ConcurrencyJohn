package lec14_ThreadsInterruption;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/*
From Future class
-------------------
boolean cancel(boolean mayInterruptIfRunning)

Attempts to cancel execution of this task. This attempt will fail if the task has already 
completed, has already been cancelled, or could not be cancelled for some other reason. 
If successful, and this task has not started when cancel is called, this task should never run.
If the task has already started, then the mayInterruptIfRunning parameter determines whether 
the thread executing this task should be interrupted in an attempt to stop the task. 
After this method returns, subsequent calls to isDone() will always return true. 
Subsequent calls to isCancelled() will always return true if this method returned true.
Parameters:mayInterruptIfRunning - true if the thread executing this task should be 
interrupted;otherwise, in-progress tasks are allowed to complete
Returns:false if the task  could not be cancelled, typically because it has already completed
normally; true otherwise
*/

public class App2
{
    public static void main (String[] args) throws InterruptedException
    {
        System.out.println("Starting...");

        ExecutorService exec = Executors.newCachedThreadPool();
        //We don't need to return anything, so the parameterized type is Void
        Future<?> future = exec.submit(new Callable<Void>()
        {
            @Override
            public Void call () throws Exception
            {
                Random random = new Random();
                for (int i = 0; i < 1E8; i++) //1E6=1*10^6
                {
                    //If the interrupted flag is set, then we terminate the thread 
                    //gracefully, and the main thread gets CPU time.
                    if (Thread.currentThread().isInterrupted())
                    {
                        System.out.println("Interrupted!");
                        break; //Or we could just use return;
                    }

                    System.out.println(i);

                    Math.sin(random.nextDouble());
                } //End of for-loop
                return null;
            }
        });

        //Shuts down the pool's managerial thread, after previous tasks have finished
        exec.shutdown();

        Thread.sleep(500);

        exec.shutdownNow();

        //future.cancel(true);

        exec.awaitTermination(1, TimeUnit.DAYS);

        System.out.println("Finished...");
    }//main method
}//class App2