package lec12_Semaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
Semaphore is an object that maintains a count value, in our example we've initialized 
the count value to be 1.
We refer to the count value as a value of available permits of the semaphore.
This class has a method called release() that increments the number of permits by 1.
This class has a method called acquire() that decrements the number of permits by 1.
The method acquire() will wait if there is no permit available, until a permit is 
released somewhere.
For a semaphore with count value of 1, acquire() and release() behave like a lock,
because it's like doing lock and unlock on a lock, and indeed we can use this as a lock.
*/
/*
The advantage of using semaphores is that we can release from different threads than the one
we invoked acquire() - With the built-in intrinsic lock, we have to unlock from the same
thread we locked the lock, however, this requirement does not apply for semaphores.
What semaphores are usually used for is to control access to some resource.
*/
/*
We'll create a new class called Connection, that would be a Singleton.
The idea would be to create connections to this Connection class.
We'll ever have only 1 instance of the Connection class, it'll manage how many connections
(Probably connections to some server to do some work) can be created, using a simple dumb
counter of type int.
What we want to accomplish is to restrict the number of connections to that server, if we
set the maximum number of connections to x, then we have to make sure the there will never
be more than x connections active.
*/
/*
The Connection class has a field called connections, that represents the number of 
connections it has active, at any given time.
Within this class we'll have the connect method, in which we'll have 2 synchronized 
statements, in one we'll increment the connections field by 1, and in the second we'll
decrement the connections field by 1.
*/
/*
In our main method, we'll create a cached thread pool, using the method
Executors.newCachedThreadPool() - Look at bottom of file for method's Javadoc.
So this method creates a thread pool that creates new threads as needed, but will reuse 
previously constructed threads when they are available.
In lecture 7 we saw the method Executors.newFixedThreadPool(int num) that created a pool
with fixed number of threads in it, this is how this pool differs from cached pool.
*/
/*
Hence, potentially we could have 200 threads created (because the tasks are rather short - 
The run method implementation) using the submit method-
Look at bottom of file for method's Javadoc.
So each task submitted, would be to invoke the connect() method.
The implementation so far just prints "Current connections" 200 times.
*/
/*
Now we'll see how we can limit the number of connections at any given time.
We'll add a field called sem of type Semaphore to class Connections, with 10 to its CTOR.
10 represents 10 connections at most, at any given time.
In order to get a connection, we must acquire one of these 10 permits, and after a connection
is no longer needed, it releases that permit.
*/
/*
Now, indeed, at most 10 connections are possible at any given time.
This is where we see the plus of semaphores.
Let's say that some thread acquired the 10th permit, so at the moment no permits available, 
namely no thread can acquire a permit to execute the connect() method.
Now the thread that acquired the 1st/2nd/3rd/4th/5th/6th/7th/8th/9th permit, could
release it so other threads can acquire a permit, so we see that it's similar to a "lock"
that the thread that acquired the 10th permit locked on, but other thread could release
that "lock" by invoking release(), and then other waiting threads can acquire the "lock", 
by invoking the acquire() method.
In other words, thread x acquires the 10th permit, so no permits available, and thread 
y wishes to get a permit, but cannot.
one of the threads that acquired one of the other 9 permits, could then invoke release(),
effectively returning a permit to the semaphore 
(Similar to unlock the lock that y is waiting for),
and now a permit is available, so threads that invoked acquire() and are waiting (like thread y)
could now compete and try to get a permit in order to proceed with execution.
*/
/*
Now he makes another change, the original connect() is not safe in the sense that if
an exception were to be thrown after acquire() was invoked, then the release() method might
never get called.
The meaning of "after acquire() was invoked", is that after this method was invoked, a
permission was acquired so the current thread can keep on executing its code, this code
could be complex in nature, and might throw various kinds of exceptions, this is where we 
should play safe in order to make sure the release() method always gets called after the
acquire() method.
*/
/*
The connect method is renamed to doConnect(), and we move some code from it to the method
connect() - The code we moved is the code in charge for acquire and release, I've left
that code as it was originally, and commented it out in the doConnect() method.
This way we made sure that even if an exception is thrown, after acquire was invoked,
release() will be called any way, because it's placed in the finally block.
*/
/*
In the doConnect method, the code in charge for acquire
a permit, is not safe (I've commented out that code), because if that code were to
throw an exception, then the release() method might never get called.
So this code has been moved to the connect() method, as well as the code of the release()
method, so we can make sure that even if an exception is thrown, release() will be called,
any way because it's placed in the finally block.
*/
/*
One last thing about Semaphore, this class has a CTOR with a Fairness parameter.
We could create a semaphore like so: private Semaphore sem = new Semaphore(10,true);
This means that whichever thread calls acquire() first will be the first one to get
a permit when a permit becomes available.
If many threads call acquire() but no permits are available, then they all have to wait
until a permit becomes available, in the mean time they all compete fair and square for
CPU time, and when a permit becomes available there is no telling as to which thread
runs and invokes the acquire method, BBUUTT, with the true parameter, it's guaranteed
that whichever thread calls acquire() first will be the first one to get
a permit when a permit becomes available.
Typically we'd want a "fair semaphore" because we don't want it to leave some threads in
the background while it serves other threads, and we don't want threads to wait for too long
to get a permit when other threads that might get permit could have waited for a shorter
time.
*/
/*
public static ExecutorService newCachedThreadPool()
Creates a thread pool that creates new threads as needed, but will reuse previously 
constructed threads when they are available. These pools will typically improve the 
performance of programs that execute many short-lived asynchronous tasks. Calls to execute 
will reuse previously constructed threads if available. If no existing thread is available,
a new thread will be created and added to the pool. Threads that have not been used for sixty
seconds are terminated and removed from the cache. Thus, a pool that remains idle for long 
enough will not consume any resources.
Returns:
the newly created thread pool

Future<?> submit(Runnable task)
Submits a Runnable task for execution and returns a Future representing that task. 
The Future's get method will return null upon successful completion.
Parameters:
task - the task to submit
Returns:
a Future representing pending completion of the task
Throws:
RejectedExecutionException - if the task cannot be scheduled for execution
NullPointerException - if the task is null
*/

public class App
{
    public static void main (String[] args) throws Exception
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 200; i++)
        {
            executor.submit(new Runnable()
            {
                public void run ()
                {
                    //This print is just for debugging
                    System.out.println(System.identityHashCode(Thread.currentThread()));
                    Connection.getInstance().connect();
                }
            });
        }
        executor.shutdown();//Shutting down the managerial thread of the pool
        executor.awaitTermination(1, TimeUnit.DAYS);
        /*
        This commented out code just shows basic methods of semaphore and print the result.
                Semaphore sem = new Semaphore(1);
                System.out.println("Available permits: " + sem.availablePermits());
                sem.release();
                System.out.println("Available permits: " + sem.availablePermits());
                sem.acquire();
                sem.release();
                System.out.println("Available permits: " + sem.availablePermits());
        */
    }
}//End of class App