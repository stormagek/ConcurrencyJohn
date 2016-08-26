package lec10_ReentrantLock;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
Condition newCondition()

Returns a new Condition instance that is bound to this Lock instance. 
Before waiting on the condition the lock must be held by the current thread. 
A call to Condition.await() will atomically release the lock before waiting and re-acquire 
the lock before the wait returns.
It's an equivalent mechanism to the low-level wait() and notify(). 

Implementation Considerations 

The exact operation of the Condition instance depends on the Lock implementation and must be 
documented by that implementation.
Returns:A new Condition instance for this Lock instance
Throws:UnsupportedOperationException - if this Lock implementation does not support conditions 
*/

public class Runner2
{
    private int       count = 0;
    private Lock      lock  = new ReentrantLock();
    private Condition cond  = lock.newCondition();

    private void increment ()
    {
        for (int i = 0; i < 10000; i++)
        {
            count++;
        }
    }

    public void firstThread () throws InterruptedException
    {
        lock.lock();
        System.out.println("Waiting...");
        cond.await(); //Causes the current thread to wait until it is signaled or interrupted. 
        System.out.println("Woken up!");

        try
        {
            increment();
            System.out.println("count in firstThread() is: " + count);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void secondThread () throws InterruptedException
    {
        Thread.sleep(1000);
        lock.lock();
        System.out.println("Press the Enter key!");
        new Scanner(System.in).nextLine();
        System.out.println("Enter key pressed!");
        cond.signal();
        try
        {
            increment();
            System.out.println("count in secondThread() is: " + count);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void finished ()
    {
        System.out.println("Count is: " + count);
    }
}