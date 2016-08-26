package lec11_Deadlock;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/*
Deadlock  - How to avoid or solve deadlock situation.
Similarly to lecture 10, in the main method, we have t1 thread invoking the firstThread()
method, and t2 thread invoking the secondThread() method, at the same time, and the
finished() method is invoked in the main thread when both threads are done.

Initially what we'd do is having the methods firstThread() and secondThread() running a
for-loop of 10000 iterations each, and in each iteration a random amount (in the range [0,99])
is transferred from one account to another account.

Since both accounts have an initial balance of 10000, the expected joint balances at the end
of execution is 20000, but that's not always the case, because we have the potential of
threads interference that is caused by the fact that the operation
balance=(balance+amount) is not an atomic operation, it consists of 3 steps.
*/
/*
To fix that, we could use a nested synchronized block (Nested within both methods) that is
synchronized on the Account objects acc1 and acc2, so locks for acc1 and acc2 can be acquired
by threads, however, rather than that, he uses the ReentrantLock class.
Before executing the transfer method, we need to lock on the locks associated with both
account objects acc1 and acc2 (Using the statements lock1.lock(); lock2.lock(); ) 
and use the try/finally structure to make sure that the unlock statements get to be called
(as demonstrated and elaborated in previous lecture) so the locks can be explicitly released.
*/
/*
Running the program now will show that the problem had been fixed, because we've established
a synchronization mechanism, according to which only 1 thread is allowed to invoke the
transfer method.
*/
/*
In a more "real" system, it'd make more sense (in the secondThread method) to lock on acc2
first and then on acc1 like so: lock2.lock(); lock1.lock();-->That might cause a deadlock.
The deadlock might occur in the following scenario:
t1 thread acquires lock1, and then t2 thread is given CPU time so it acquires lock2,
now when t1 tries to acquire lock2 it can't do it because t2 acquired that lock already,
similarly t2 thread would try to acquire lock1 and it can't because
t1 thread had already acquired it.
Both threads are stuck.
It's important to note that sometimes it'd work fine if one of the threads acquire both locks
so the second thread will run after the former thread releases the locks, however, we cannot
count on that, and the scenario described earlier that leads to a deadlock must be avoided.
As we've seen, deadlock can occur if we lock on the locks in different order.
Deadlock might occur not only with ReentrantLock, but also with synchronized blocks.
*/
/*
Two solutions to avoid deadlock are:
1. Always lock your locks in the same order.
2. Create a method that can acquire the locks in an order that would never create a deadlock.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
*/
/*
The method will be called acquireLocks(Lock firstLock, Lock secondLock),
and its job would be to acquire the firstLock and the secondLock in a way that avoids 
deadlock.
The invocation acquireLocks(lock1, lock2); replaces the following 2 statements:
lock1.lock();lock2.lock(); in both the firstThread and secondThread methods.
Within the acquireLocks method, we'll use a ReentrantLock class method called tryLock,
This method returns immediately, it returns true if it acquired the lock and false if it
didn't.
*/
/*
We'll run the tryLock method in a "while(true)" loop in order to make sure that the method
acquireLocks indeed acquire the locks, so we'll make this method return only when it had
acquired the locks - In reality we might want to consider a timeout--> tryLock method also
has the following signature: public boolean tryLock(long timeout, TimeUnit unit).
If the locks have not been acquired in a certain iteration, executed by t1 thread, 
it means that some other thread acquired one of the locks, or both of them, then we'll 
sleep for 1 millisecond to  allow that thread to unlock the lock/s that it had acquired,
that'd give t1, a chance to acquire both locks in the next iteration.
If t1 executes acquireLocks, and both invocations of tryLock() return false, it means that 
another thread had already acquired both of them, hence, we should wait until that thread
unlocks the locks and then hope that when t1's acquireLocks method executes another iteration
of the loop it'd acquire both locks.
When the method acquireLocks will acquire both locks, it'll do it safely, without a deadlock. 
*/
/*
Both threads might invoke the acquireLocks method at the same time, but once one thread
acquires both locks, the other thread would have to wait until it can accomplish that. 
So, when acquireLocks acquires both locks, it'll return and the transfer method will be
invoked, then either the t1 thread or t2 thread will unlock both locks, so the other thread 
can acquire them.
*/
/*
The only question remains is why he used 2 locks (lock1 and lock2), because in the code
it seems that 1 lock would have been enough, here are some responses of John from the 
YouTube comments section:
1. There's no physical connection between lock1 and acc1, and lock2 and acc2, 
   it's just that you always acquire lock1 before dealing with acc1, and lock2 for acc2. 
   By sticking to that convention, you ensure that you never have two threads modifying an 
   account at the same time. In a larger system, you could have a table of 
   accounts vs. locks or something.

2. One lock would work here. 
   The problem with using one lock would only occur if you had lots of accounts and lots of 
   threads, Then you need to lock both accounts, out of the many possible accounts.

3. Best answer by John IMHO:
   Small programs like this will often work fine even with no locks, 
   But you can't rely on it working unless you lock all resources that are shared between 
   threads.
   In this program you could use one lock to cover both accounts, but if you had many 
   accounts and didn't know which two you were going to lock each time, and you had many 
   threads accessing them, you'd need to have one lock per account.

4. One lock would work here. The problem with using one lock would only occur if you had lots 
   of accounts and lots of threads. Then you need to lock both accounts, out of the 
   many possible accounts."
   In a real world situation, multiple threads would be trying to access the accounts, 
   so if you only use one lock all the accounts transactions would be stopped only for 1 
   transfer.

5. In real world, there would be > 1 thread trying to manipulate a single account,
   in that case you would need to acquire a lock for that account. 
   In this example you have used 2 locks, but again, practically, there will be a lock 
   associated with each account number, and you would retrieve a lock for that account and 
   lock it. 
*/
/*
Both threads share access to the same instance of Runner class, as can be seen in the
App class, through this instance, both threads have access to shared mutable state, that is
2 instances of the Account class called acc1 and acc2.
The key for optimal practice to remember is to lock all resources that are shared between
threads, and in our case it's acc1 and acc2.
So if we were to synchronize the code responsible to invoke the Account.transfer method,
this would not be enough in case that both threads t1 and t2 can access acc1 and acc2 in other
places in the code!!!
 */

public class Runner
{
    private Account acc1  = new Account();
    private Account acc2  = new Account();

    private Lock    lock1 = new ReentrantLock();
    private Lock    lock2 = new ReentrantLock();

    private void acquireLocks (Lock firstLock, Lock secondLock) throws InterruptedException
    {
        while (true)
        {
            //Try to acquire locks
            boolean gotFirstLock = false;
            boolean gotSecondLock = false;
            try
            {
                gotFirstLock = firstLock.tryLock();
                gotSecondLock = secondLock.tryLock();
            }
            finally//runs even if the code within the try block throws an exception
            {
                if (gotFirstLock && gotSecondLock)
                    return;//Method accomplished what we needed.

                if (gotFirstLock) //if only firstLock has been acquired
                    firstLock.unlock();//allow other threads to acquire this lock.

                if (gotSecondLock) //if only secondLock has been acquired
                    secondLock.unlock();//allow other threads to acquire this lock.
            } //End of try/finally

            //Locks not acquired
            Thread.sleep(1);
        }
    }

    public void firstThread () throws InterruptedException
    {
        Random random = new Random();

        for (int i = 0; i < 10000; i++)
        {
            acquireLocks(lock1, lock2);//Replaces both following commented out statements
            //            lock1.lock();
            //            lock2.lock();
            try
            {
                Account.transfer(acc1, acc2, random.nextInt(100));
            }
            finally
            {
                lock1.unlock();
                lock2.unlock();
            }
        }
    }

    public void secondThread () throws InterruptedException
    {
        Random random = new Random();

        for (int i = 0; i < 10000; i++)
        {
            acquireLocks(lock2, lock1);//Replaces both following commented out statements
            //            lock2.lock();
            //            lock1.lock();
            try
            {
                Account.transfer(acc2, acc1, random.nextInt(100));
            }
            finally
            {
                lock1.unlock();
                lock2.unlock();
            }
        }
    }

    public void finished ()
    {
        System.out.println("Account 1 balance: " + acc1.getBalance());
        System.out.println("Account 2 balance: " + acc2.getBalance());
        System.out.println("Total balance: " + (acc1.getBalance() + acc2.getBalance()));
    }
}
