package lec3_1_ThreadInterferenceAndInterleavingIntrinsicLockSynchronized;

/*
Thread interference, Threads interleaving, Intrinsic Lock/Monitor Lock, synchronized,
The join() method, AtomicInteger class.

Normally we'd expect count to be 20000, but it's not always the case.
count++ is equivalent to count = count+1; so it's not an atomic operation.
We're reading (AKA fetching) the value of count, incrementing it,
and storing it back (Adds up to 3 steps by the JVM).
In computer terms, after fetching the count value, other thread/process
might gets CPU time and sometime later our thread gets CPU time back, and
increments and stores its value...
It's possible that it'd be an atomic operation in runtime, but just
possible, we should not count on it. 
We should find a way to make count++ an atomic operation (Non-native atomic operation), 
or in other words to make sure that when count is accessed by 1 thread, 
no other thread can access it until the other thread is done with it.
  First solution for this:
  --------------------------------
The simplest way in this case of type int, is to use AtomicInteger, which 
is a specialized class that has a method allowing an int to be
incremented atomically (volatile won't fix this because it guarantees memory visibility,
while it doesn't guarantee atomicity) - This class is allowed to be shared among multiple
threads safely (Thread-safe class).
  Second solution for this:
  --------------------------------
One more way would be the use of the synchronized keyword at the increment() method.
The increment method has one line of code which is: count++;, so just
having this method without synchronization won't fix the problem since still,
count++ is not a native atomic operation, and the problem of threads interference still
persists. We should declare the increment method as synchronized to fix the threads
interleaving problem - threads interleaving elaborated below this line:

Interference happens when two operations (non atomic operations),
running in different threads, but acting on the same data, interleave.
This means that the two operations consist of multiple steps, 
and the sequences of steps overlap.

More on interleaving at the bottom of the code lines - from Javadoc.
Every object in Java has an intrinsic lock/monitor/mutex.
When a synchronized method on an object (In this case an instance
of App class) is invoked, the current thread must acquire that object's lock 
before invoking the method, and the key point is that only 1 thread can acquire
that lock at a time, so if 1 thread invoked a synchronized method, 
and other thread tries also to invoke it, that other thread would just
have to wait until the first thread releases the lock.
This solution has made the increment operation atomic.

The use of volatile won't fix this problem, because if the field is accessed
in a synchronized statement, then it's guaranteed that the most up to date value of the
field is visible for all threads that can access the current state of the
field, provided that this field is accessed using synchronized statements that lock on the
same object (same lock).  - Pages 18.1-18.4 in AP explain that well.
 
 */
/* 
public void start()
Causes this thread to begin execution; the Java Virtual Machine calls the run method of this
thread. The result is that two threads are running concurrently: 
the current thread (which returns from the call to the start method - 
in our example it's the main thread that returns) and the other thread
(which executes its run method). 
It is never legal to start a thread more than once. 
In particular, a thread may not be restarted once it has completed execution.
Throws:IllegalThreadStateException - if the thread was already started.
*/
/*public final void join() throws InterruptedException
Waits for this thread to die (In this case for the t1 thread and t2 thread). 
An invocation of this method behaves in exactly the same way as the invocation join(0)
*/
/*
 Thread Interference Example from Javadoc:
 From https://docs.oracle.com/javase/tutorial/essential/concurrency/interfere.html:
Consider a simple class called Counter:

class Counter {
    private int c = 0;

    public void increment() {
        c++;
    }

    public void decrement() {
        c--;
    }

    public int value() {
        return c;
    }
}

Counter is designed so that each invocation of increment will add 1 to c, 
and each invocation of decrement will subtract 1 from c. 
However, if a Counter object is referenced from multiple threads, 
interference of threads may prevent this from happening as expected.
In other words, if a Counter instance is to be shared among multiple threads, and as we can see,
this Counter instance is a "shared mutable state", then interference of threads may prevent this
from happening as expected.

!!!!!!!!!!!
Interference happens when two operations (Non atomic operations), running in different threads, 
but acting on the same data, interleave. This means that the two operations consist 
of multiple steps, and the sequences of steps overlap.
!!!!!!!!!!!

It might not seem possible for operations on instances of Counter to interleave, 
since both operations on c are single, simple statements (c++ and c--). 
However, even simple statements can translate to multiple steps by the virtual machine. 
We won't examine the specific steps the virtual machine takes — it is enough to know 
that the single expression c++ can be decomposed into three steps:

Retrieve the current value of c.
Increment the retrieved value by 1.
Store the incremented value back in c.
The expression c-- can be decomposed the same way, except that the second step decrements
instead of increments.

Suppose Thread A invokes increment at about the same time Thread B invokes decrement, 
and both threads work with the same object of Counter (Look at my comment at the lowest 
section of page 17.4 in AP).
If the initial value of c is 0, their interleaved actions might follow this sequence:

Thread A: Retrieve c.
Thread B: Retrieve c.
Thread A: Increment retrieved value; result is 1.
Thread B: Decrement retrieved value; result is -1.
Thread A: Store result in c; c is now 1.
Thread B: Store result in c; c is now -1.
Thread A's result is lost, overwritten by Thread B.
This particular interleaving is only one possibility.

Under different circumstances it might be Thread B's result that gets lost, and here is
another possibility illustrating this:
Thread A: Retrieve c.
Thread B: Retrieve c.
Thread A: Increment retrieved value; result is 1.
Thread B: Decrement retrieved value; result is -1.
Thread B: Store result in c; c is now -1.
Thread B: Retrieve c.
Thread B: Decrement retrieved value; result is -2.
Thread B: Store result in c; c is now -2.
Thread B: Retrieve c.
Thread B: Decrement retrieved value; result is -3.
Thread B: Store result in c; c is now -3.
Thread A: Store result in c; c is now 1.
Thread B's result is lost, overwritten by Thread A.

 
Under different circumstances different possibilities are possible, or there could be
no bugs at all. 
Because they are unpredictable, thread interference bugs can be difficult to detect and fix.
 */

public class App
{
    private int count = 0;

    public synchronized void increment ()
    {
        count++;
    }

    public void doWork ()
    {
        Thread t1 = new Thread(new Runnable() //Using anonymous class
        {
            @Override
            public void run ()
            {
                for (int i = 0; i < 10000; i++)
                {
                    count++;//Buggy
                    //increment();//Not Buggy
                }
            }
        });

        Thread t2 = new Thread(new Runnable() //Using anonymous class
        {
            @Override
            public void run ()
            {
                for (int i = 0; i < 10000; i++)
                {
                    count++;//Buggy
                    //increment();//Not Buggy
                }
            }
        });
        t1.start();//Start both threads, so both loops run concurrently and try to increase
        t2.start();//count 10000 times each.
        /* After the invocation of start() a new thread is created and started,
         but the current thread (main thread) is back and active immediately after the
         invocation, prints the count value which is 0, then the main thread terminates and both
         other threads (t1 and t2) are still running, but the new value of count following the
         execution of both threads is never printed, hence the bug.
         See below for elaboration of the start() method.
         To fix this we'll use the join method - 
         See below for elaboration of the join() method.
        */
        try
        {
            //The main thread invokes the t1.join() and t2.join() methods!!!
            t1.join();//Pauses execution of the thread it's called in, which is the main thread
            t2.join();//Pauses execution of the thread it's called in, which is the main thread
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("Count is: " + count);
    }//doWork() method.

    public static void main (String[] args)
    {
        App app = new App();
        app.doWork();
    }
}//class App
