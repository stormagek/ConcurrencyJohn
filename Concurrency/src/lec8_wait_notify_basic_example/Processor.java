package lec8_wait_notify_basic_example;

import java.util.Scanner;

/*
In lecture 7 we saw an implementation of the producer/consumer pattern using 
ArrayBlockingQueue, in the next lecture we'll see the same with low level thread 
synchronization techniques. 
The actual implementation of the producer/consumer pattern will be in lecture 9 as mentioned,
and this lecture will be an introduction of using the methods notify() and wait().
In the App class, within the main method, the consume() and produce() methods are invoked 
by 2 different threads, so consume() runs in 1 thread and produce() in another thread.
Within consume() we'll invoke sleep for 2 seconds in order to ensure that the thread that
invokes produce() runs first.
*/
/*
Within the produce() method we'll create a synchronized statement block, and we'll synchronize
on the Processor object itself, within this block we'll invoke wait().
This is a method available to every Java object since it exists in the Object class.
It's recommended to invoke wait(long timeout), because just invoking wait() may result in
the thread waiting forever if we're not careful.
Refer to the Javadoc of this method, below.
*/
/*
Within the consume() method (In other words the thread that invokes the consume() method)
we'll create a synchronized statement block that will be locked on the same object
(Processor in our example), so both synchronized blocks are locked on the same object,
hence on the same intrinsic lock.
*/
/*
The t1 thread runs first, because t2 thread will sleep for 2 seconds, so t1 thread gets
to invoke the wait() method, which will cause the lock to be released and the t2 thread will 
be able to acquire the lock in order to run its synchronized block.
In t2 thread's synchronized block we'll wait for user to press the Enter key.
After the user entering the Enter key, we invoke notify() - Note that if user never presses the 
Enter key, the waiting thread will wait forever!!! - Serious BBUUGG!!!
Once Enter is pressed, t1 thread stops waiting (it is awakened) and awaits for its
opportunity to acquire the lock and keep on running.
notify() awakens one of the threads that are waiting due to invocation of the
wait() method, where the thread upon which notify was invoked and 
every one of the possible awakened threads, are locked on the same object
which in this case it's the Processor class.
It's a very handy mechanism for synchronizing threads.
*/
/*
 public final void wait() throws InterruptedException - from Javadoc
Causes the current thread (The thread invoking the wait() method) to wait until another thread
invokes the notify() method or the notifyAll() method for this object 
(The object the thread is locked on).
---It can only be invoked within a synchronized code block.--- 
The owner thread of the intrinsic lock AKA monitor, releases that lock, once wait() is invoked on
the object whose intrinsic lock was acquired.
The thread in which wait() was invoked, will resume if 2 things happen:
1. Another thread that is locked on the same object (Processor in our example) must invoke
   either the method notify() or notifyAll().
2. It's must be possible for the waiting thread to regain the monitor - In the code below, we show
   how the waiting thread can be awakened by invocation of notify(), but cannot re-acquire the
   lock (The infinite loop example).
   
In other words, this method behaves exactly as if it simply performs the call wait(0).

The current thread (The thread invoking wait()) must invoke wait only on object 
whose intrinsic lock had been acquired by it.
(The object it is locked on).
 For example:
 1.
         synchronized (this)//we'll synchronize on the Processor object 
        {
            wait();//Equivalent to this.wait();
        }
2.
            synchronized (lock)//Synchronizing on the lock object
            {
                    lock.wait();
            }


When the wait() method is invoked by a thread, the thread releases ownership of the acquired
monitor and waits until another thread notifies it with notify(), or possibly notify 
additional threads (with notifyAll) that are waiting on the same object's monitor,
the notify()/notifyAll() invocations cause the thread/threads to wake up.

The thread then waits until it can re-obtain ownership of the monitor and resumes execution.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
This method should always be used in a loop - elaboration for this in lecture 9 - my comments.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
This method should only be invoked on an object whose intrinsic lock is owned by the 
current thread.

Throws:
IllegalMonitorStateException - if the current thread is not the owner of the object's monitor.
If the current thread invokes wait on an object, without owning its intrinsic lock.
In other words, invocation of wait() is possible only from within a synchronized block,
where the current thread owns an intrinsic lock of some object, upon which it may invoke the
wait() method.

InterruptedException - if any thread interrupted the current thread before or while the current
thread was waiting for a notification. The interrupted status of the current thread is cleared
when this exception is thrown.
 */
/*
public final void notify() - from Javadoc
Wakes up a single thread that is waiting on this object's monitor. 
If multiple are waiting on this object, one of them is chosen to be awakened.
The invocation of the method alone doesn't cause the thread that invoked it, to release the 
lock.
The notifyAll() method awakens all of the threads that are waiting on the same lock(same object).
In our case notify() will be more efficient because we have only 1 thread waiting on the same
lock.
The choice is arbitrary and occurs at the discretion of the implementation. 
A thread waits on an object's monitor by calling one of the wait methods.
The awakened thread will not be able to proceed until the current thread relinquishes 
the lock on this object.
notify() can only be invoked within a synchronized code block. 
The awakened thread will compete in the usual manner with any other threads that might be
actively competing to synchronize on this object; for example, the awakened thread enjoys 
no reliable privilege or disadvantage in being the next thread to lock this object.
This method should only be called by a thread that is the owner of this object's monitor. 
A thread becomes the owner of the object's monitor in one of three ways:
1. By executing a synchronized instance method of that object.
2. By executing the body of a synchronized statement that synchronizes on the object.
3. For objects of type Class, by executing a synchronized static method of that class
   (As can be seen in the SingletonMyThreads class in the DesignPatterns project).
   Only one thread at a time can own an object's monitor.
   */

public class Processor
{
    public void produce () throws InterruptedException
    {
        synchronized (this)//we'll synchronize on the Processor object 
        {
            System.out.println("Producer Thread Running...");
            wait();//Equivalent to this.wait();
            System.out.println("Producer Thread Resumed...");
        }
    }

    public void consume () throws InterruptedException
    {
        Scanner scanner = new Scanner(System.in);
        Thread.sleep(2000);

        synchronized (this)//we'll synchronize on the Processor object
        {
            System.out.println("Waiting for return key...");
            scanner.nextLine();//Program halts until we press Enter
            notify();//Equivalent to this.notify();

            //This print is here just to prove that an invocation of notify() doesn't cause
            //the current thread to release the lock, so the print will be executed and only 
            //then the other thread that is locked on the same object, can re-acquire the 
            //lock and print Resumed...
            System.out.println("consume() has finished running");
            //We should note that if I were to do the following here:
            //while(true){syso("f");}, then the thread that invoked notify() will never release
            //the lock, and the program will hang, and that's an obvious bug.
            //Obviously, an infinite loop will cause the consumer thread to hang forever, 
            //because it's infinite (Dahh), but it'll also cause the producer thread
            //to wait forever, because the consumer thread will never release the lock
            //which is the same lock the producer thread needs to acquire.
        }
    }
}//End of public class Processor