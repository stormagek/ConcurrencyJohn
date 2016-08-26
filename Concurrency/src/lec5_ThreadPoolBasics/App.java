package lec5_ThreadPoolBasics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
Thread pools is a way of managing lots of threads at the same time.
The advantage of using thread pool is that there's a lot of overhead with starting threads,
and by recycling the threads in a thread pool we avoid that overhead.
A thread pool is like having workers in a factory (In this code example we have 2 workers),
and we have some tasks we want them to process.
For example if we have 5 tasks, and we want each of these workers AKA threads to 
process these tasks, and when a thread finishes processing a task, I want it to start a new
task. We give our workers AKA threads numerous tasks, and ask them to work on these tasks
one at a time, so when a thread finishes 1 task, start a new one.
*/
/*
In our example we have 2 workers/threads, but typically it'd be a bigger number.
In order to allot tasks, we'll submit tasks to an instance of ExecutorService class
called executor, this instance has its own managerial thread that will handle passing
out these tasks I want processed - In this example it's 5 tasks.
This is simulated using a simple for-loop with 5 iterations representing 5 tasks.
As mentioned earlier, ExecutorService class has its own managerial thread, and we want to 
instruct that thread to stop accepting new tasks after the for-loop and to shut itself down
after the tasks are finished, this is done using the shutdown() method.
If we want all the tasks to complete (5 tasks in our case) before the main thread gets 
CPU time, we'll use the method executor.awaitTermination which blocks.
*/
/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
What actually happens in this program:
------------------------------------------
An object of ExecutorService class is created with 2 as the number of threads to be managed
in its pool. Then this executor is submitted tasks, each of these tasks is a Runnable instance,
in our example we submit 5 tasks, all of which are instances of the Processor class
(which implements the Runnable interface), and the actual task is the implementation of the
run method in each of the instances of the Processor class, and again in our example here,
it's the same run method (of the Processor class).
The implementation of the run method in our example is just invocation of the sleep method 
for 5 seconds of sleeping.
Then the 2 threads from the pool that is created by the Executors.newFixedThreadPool(2) 
method start executing the tasks, when 1 finishes a task, it's given the next task...
Refer to bottom of file for elaboration on this method.
ExecutorService class has its own managerial thread, and we want to 
instruct that thread to stop accepting new tasks after the for-loop and to shut itself down
after the tasks have finished, this is done using the shutdown() method.
So we invoke  executor.shutdown() method.
Refer to bottom of file for elaboration on this method.
After reading the elaboration on the shutdown method, he says he wants all the tasks
to finish before the rest of the main thread keeps running, so he invokes the
executor.awaitTermination method, which blocks for a given time that it accepts as argument.
Refer to bottom of file for elaboration on this method.
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
*/
/*
public static ExecutorService newFixedThreadPool(int nThreads)
Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded
queue (queue of tasks). At any point, at most nThreads threads will be active processing tasks. 
If additional tasks are submitted when all threads are active, they will wait in the queue 
until a thread is available. If any thread terminates due to a failure during execution prior
to shutdown, a new one will take its place if needed to execute subsequent tasks. 
The threads in the pool will exist until it is explicitly shutdown.
Parameters:nThreads - the number of threads in the pool
Returns:the newly created thread pool
 */
/*
public void shutdown()
Initiates an orderly shutdown (of the pool) in which previously submitted tasks are executed,
but no new tasks will be accepted. 
Invocation has no additional effect if already shut down.
This method does not wait for previously submitted tasks to complete execution, for example
in our code, rest of the code in the main thread will keep executing concurrently with the
running threads from the pool that keep processing the remaining tasks, this is what it means
that "This method does not wait for previously submitted tasks to complete execution".
Use awaitTermination instead, to do just that, namely to block.
 */
/*
boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
Blocks until all tasks given to the pool have completed execution after a shutdown request
(to the pool), or the timeout occurs, or the current thread is interrupted, whichever happens
first.
Parameters:
timeout - the maximum time to wait
unit - the time unit of the timeout argument
Returns:
true if this executor terminated and false if the timeout elapsed before termination
 */
/*
Future<?> submit(Runnable task)
Submits a Runnable task for execution and returns a Future representing that task. 
The Future's get method will return null upon successful completion.
Parameters:task - the task to submit
Returns:a Future representing pending completion of the task
Throws:RejectedExecutionException - if the task cannot be scheduled for execution
executionNullPointerException - if the task is null
*/
class Processor implements Runnable //We'll start many threads of Processor
{
    private int id;

    public Processor (int id) //Helps identifying each thread
    {
        this.id = id;
        System.out.println("Processor CTOR just called");
    }

    /*
    Between the "Starting" and the "Completed" prints, we'll simulate some useful work
    such as handling requests if this were a server, or some files processing etc.
    For this we'll use sleep of 5 seconds.
     */
    @Override
    public void run ()
    {

        System.out.println("Starting: " + id + " " + System.identityHashCode(Thread.currentThread()));

        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("Completed: " + id + " " + System.identityHashCode(Thread.currentThread()));
    }

}//class Processor

public class App
{
    public static void main (String[] args)
    {
        //So far, if we wanted to create a thread, we used the Thread class, but now,
        //we'll use the ExecutorService class.
        ExecutorService executor = Executors.newFixedThreadPool(2);//2 represents 2 threads

        //Submit 5 tasks to the thread pool created by the invocation of newFixedThreadPool(2).
        //We "send" a task to the pool, the task has to be a Runnable instance.
        //Note that we do not start a running thread, what we do is submitting a task to
        //the pool, so it can be executed by one of the pool's 2 threads.
        //Recall that without invoking the start() method on a Runnable object,
        //no new thread is started by the JVM.
        System.out.println("Starts assigning tasks to the pool");
        for (int i = 0; i < 5; i++)
        {
            executor.submit(new Processor(i));
        }

        //Shuts down the pool,won't shutdown immediately, rather it'd wait for all threads 
        //of the pool to finish their tasks, and then the pool managerial thread will be
        //terminated.
        executor.shutdown();

        System.out.println("All tasks submitted. ");
        try
        {
            //Wait for 1 day, if we were to invoke the method like so:
            //executor.awaitTermination(10, TimeUnit.SECONDS), and if the 5 tasks didn't
            //finish in 10 seconds, the method might return before the tasks are finished.
            //We wait for 1 day, and for our program, that'd be enough.
            executor.awaitTermination(1, TimeUnit.DAYS);//This method blocks for 1 day.
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("All tasks completed ");
    }
}//class App