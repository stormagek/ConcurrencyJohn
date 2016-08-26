package lec6_countDownLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Countdown Latches.
We use the CountDownLatch class to impose a high level synchronization, where we block
one thread with an instance of CountDownLatch, until a thread or some threads reduces 
that latch value to zero.
The goal here is to make 1 or more threads wait (the main thread in this example),
until other thread or threads finish their operations, in this case it's 3 threads that
process 100 or 3 tasks/operations one at a time (100 or 3 or any other number for that matter).
In this example the main thread waits on a latch which effectively causing it to wait for 
some other threads to finish their operations, each of these other threads invoke
the countDown() method on that latch when its operation is finished, when the count value
reaches 0, the thread that is waiting (In other words locked by the latch) is released and
keeps on running (In our case it's the main thread).
*/
/*
There are many more high level classes that are very useful in the java.util.concurrent
package, and using them allowing us to avoid synchronization issues.
*/
/*
As we saw in lectures 3 and 4, we can get lots of problems with threads interference, when
more than 1 thread accesses the same shared mutable data at the same time.
We can use synchronization techniques or use classes that are thread safe, 
namely they can be referenced and accessed by more than 1 thread at the same time safely
without worrying about manual threads synchronization.
CountDownLatch is such a class. This class allows us to count down from a specified number
(given to its CTOR), to zero.
*/
/*
Any thread that has an invocation of the await() method upon some latch object, within the
thread code, will wait until that latch reaches zero, effectively releasing it to keep 
running. 
Other threads that also wait on the same latch can then continue.
The countDown method will typically be invoked at the end of a "task", or in other words
at the end of thread's code (AKA the run method), so that the next available thread from
the pool will pick up the next task to process it and it too, will invoke the countDown method,
and so on...
Since the CountDownLatch class is thread safe, we don't use the synchronized keyword.
*/
/*
Rather than create threads using the Thread class, we'll use a thread pool as can be seen
in lecture 5.
We'll crate a pool of 3 threads and that pool will be submitted 3 tasks 
(a Runnable object which in our case it's a Processor object).
Basically, the pool has 3 threads, and it is submitted 3 tasks 
(Each task is a Runnable object which in our case it's a Processor object).
*/
/*
It seems to be an overkill for a thread pool with 3 threads,
but the range of possibilities is greater, i.e. we can have a pool with 3 threads,
but the for-loop will run for 100 iterations, effectively submitting 100 tasks,
(Runnable objects which in our case it's a Processor object), and the count value would 
be 100 (rather than 3), and that's a more appropriate use of a thread pool,
in which we have 3 threads in the pool, these same 3 threads will execute 100 tasks,
one at a time, each thread will invoke the countDown() method when the "task" is finished,
this method is an implicitly synchronized method 
(because the CountDownLatch class is thread safe), so the count value will be decremented
exactly 100 times - from 100 to 0 - what we get is that the block created by the 
await method will be released (in other words the await method will return) exactly 
after the 100 tasks have been processed and that'll happen when the latch count value is 0.
There are no worries of threads interference over the count value.
*/
/*
The goal in our program here is to make 1 thread wait (the main thread) until other
threads have finished their operations, in this case it's 3 threads that process 100 tasks 
one at a time or 3 tasks one at a time.
*/
/*
It's worth mentioning that across the entire program code, we have only 1 CountDownLatch object,
it's created in the main method, and called latch.
One more thing worth mentioning is that if the pool has 3 threads in it, then there will
be at most 3 threads (managed by the pool) running at any given time, 
and the tasks' (Runnable objects which in our case it's a Processor object) run method
has invocation of the sleep method like so: sleep(3000), so first thread waits 3 seconds,
second thread waits 3 seconds and the third thread waits for 3 seconds, 
so after 3 seconds approximately, all three threads finish, as can be seen down in the code,
where I've computed the runtime which is a bit more than 3 seconds 
(Look in main method for the computation).
*/
/*
The countDown() method will typically be invoked at the end of a "task", or in other words
the end of thread's code or in other words the end of the run method.
*/
/*
 public void countDown()
Decrements the count of the latch, releasing all waiting threads if the count reaches zero. 
Any thread that has an invocation of the await() method upon some latch object, will wait
until that latch reaches zero, effectively releasing it to keep running.
If the current count is greater than zero then it is decremented. If the new count is zero then
all waiting threads are re-enabled for thread scheduling purposes. 
If the current count equals zero then nothing happens.
 */
/*
public void await() throws InterruptedException
Causes the current thread to wait until the latch has counted down to zero, unless the 
thread is interrupted.
If the current count is zero then this method returns immediately.
If the current count is greater than zero then the current thread becomes disabled for 
thread scheduling purposes and lies dormant until one of two things happen:
1. The count reaches zero due to invocations of the countDown() method; or
2. Some other thread interrupts the current thread.
This method is not limited of being invoked in the main thread as in our code, it can also
be invoked in any other thread, so we can have the inverse situation where we have some threads
that are waiting, by invoking the await() method in them, and in the main thread we'll 
count down from 3 to 0 (by invoking the latch.countDown() method within the main thread 
rather than from within the other threads) - In our code we invoke the 
latch.countDown() method in the Processor thread and the await method in the main thread.
 */
/*
public class CountDownLatch extends Object
A synchronization aid that allows one or more threads to wait until a set of operations 
being performed in other threads completes.
A CountDownLatch is initialized with a given count. The await methods block until the 
current count reaches zero due to invocations of the countDown() method, after which all 
waiting threads are released and any subsequent invocations of await return immediately.
This is a one-shot phenomenon -- the count cannot be reset. If you need a version that resets
the count, consider using a CyclicBarrier.

A CountDownLatch is a versatile synchronization tool and can be used for a number of purposes:
1. A CountDownLatch initialized with a count of one serves as a simple on/off latch, or gate:
   all threads invoking await wait at the gate until it is opened by a thread invoking
   countDown().
2. A CountDownLatch initialized to N can be used to make one thread wait until N threads have 
   completed some action, or some action has been completed N times - In our example 1 thread
   which is the main thread waits until an action has been completed N times
   (In our code it's 3 actions AKA operations AKA tasks), so 1 thread waits until an action
   has been completed  3 times, and the action is the run method of Processor class.
   When the count value reaches 0, the await method returns or in other words the gate opens,
   and the waiting thread or threads can continue).

A useful property of a CountDownLatch is that it doesn't require that threads calling countDown
wait for the count to reach zero before proceeding, it simply prevents any thread from 
proceeding past an await until all threads could pass.
 */

class Processor implements Runnable
{
    private CountDownLatch latch;

    //CTOR
    public Processor (CountDownLatch latch)
    {
        this.latch = latch;
        System.out.println("Processor CTOR just called");
    }

    @Override
    public void run ()
    {
        System.out.println("Started...");
        try
        {
            Thread.sleep(3000);//Simulate useful work like wait for data etc.
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("Thread running is: " + System.identityHashCode(Thread.currentThread()));
        //System.out.println("Before decrement: " + latch.getCount());
        latch.countDown();//This is the end of the "task", so we invoke the countDown method.
        //System.out.println("FuckIt " + latch.hashCode());//Just for debugging
        //System.out.println("After decrement: " + latch.getCount());
    }

}//End of class Processor

public class App
{
    public static void main (String[] args)
    {
        CountDownLatch latch = new CountDownLatch(3);
        //CountDownLatch latch = new CountDownLatch(100);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) //for (int i = 0; i < 100; i++)
        {
            executor.submit(new Processor(latch));
            //System.out.println("FuckIt " + latch.hashCode());//Just for debugging
        }
        try
        { //Refer to bottom of file for elaboration on this method.
            latch.await();//main thread waits until the latch count is 0
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("Completed...");
        long end = System.currentTimeMillis();
        //prints a bit more than 3000, such as 3011 or 3025.
        System.out.println("Runtime is: " + (end - start));
        executor.shutdown();//Shutting down the thread pool.
    }
}//class App