package lec10_ReentrantLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
Reentrant Locks - An alternative to the synchronized keyword.
Look below the code for JavaDoc for the Lock interface and the ReentrantLock class.

In the main method, we have t1 thread invoking the firstThread() method, and t2 thread
invoking the secondThread() method, at the same time (concurrently).

Invoking the increment method within the firstThread() and secondThread() methods without
some form of synchronization does not guarantee that count will be 20000 following the
execution of both threads, because as we saw in previous lectures, what we have here is
the potential of threads interference, sometimes it might be 20000 and at other times it 
might not - the ++ operator is not a native atomic action!!!
*/
/*
Rather than fixing that using low-level synchronized statements, we'll use reentrant locks.

In class Runner we'll have a field of type interface Lock, called lock.
What that means is that when a thread acquires this lock, it can lock it again should it
needs to, and the lock counts the number of times it had been locked, then, we must unlock
it the same number of times - Typically we'll lock it just once.
Only 1 thread can lock on this lock object at any given time, so it works similarly to
the synchronized keyword.

We'll use a Lock implementation which is the ReentrantLock class.
These interface and class have some advantages over the synchronized keyword in some
circumstances.(Look at the Javadoc for Lock interface down below).
*/
/*
The fix for the problem earlier mentioned (Invoking the increment method within the
firstThread() and secondThread() methods without some form of synchronization)
would be to surround the invocation of the increment() method with the statements: 
lock.lock() and lock.unlock(), in both methods firstThread() and secondThread().
This way we've eliminated the potential for threads interference.
When 1 thread is locked on the Lock object, no other thread can lock on it, until it's 
released (unlocked), so the code of the increment() method, that is, the ++ operator is
executed in an atomic way.
*/
/*
However, surrounding the invocation of increment() is not such a good practice, because if
during the execution of the increment() method, an exception is thrown,the statement
lock.unlock() might not be executed rendering the other thread waiting forever,
because with this mechanism, the acquisition and release of the locks, must be done explicitly.
A better practice to use the ReentrantLock objects would be to put the code we'd like to be
synchronized in a try block so we can catch exceptions if we see fit to do so, 
but more importantly, we should have the finally block, within which we'll have the 
lock.unlock() statement.
In our example we didn't use the catch block, rather, only the finally block which is always
guaranteed to be executed regardless of whether or not an exception was thrown in the try
block.
*/
/*
He also shows an example of wait() and notify() for reentrant locks.
The methods wait() and notify() are implemented in the Object class.
The implementors of the ReentrantLock class used different names for the methods that
do similar things to Object's wait() and notify(), so the equivalent is await() and signal(),
and notifyAll() has an equivalent called signalAll() - Note that these methods are not methods
of the Lock interface or the ReentrantLock class, rather, they're methods of class Condition.
*/
/*
I've created 2 more classes called Runner2 and App2, in there I've put the further examples.
My elaboration continues here, however, the changes were made in App2 and Runner2 classes.
We'll add a private field to Runner2 class, of type Condition, so we get the Condition 
object from the lock that we're locking on.
This is a Condition instance that is bound to this Lock instance.
It's important that we call signal() or await() only after we've got the lock, which is 
the same as the methods wait() and notify() which we can call only within synchronized
blocks or in other words only after the thread has acquired the lock upon which we invoke
either wait() or notify().
*/
/*
So, in our example we can invoke these methods only after the statement lock.lock().
So, after the lock.lock() statement, we'll have cond.await(), which does the same as
the method wait() within a synchronized block, namely it releases the lock, or in terms of
the ReentrantLock class, it unlocks the lock, so another thread can acquire that lock and 
lock on it.
*/
/*
Within the secondThread() method we'll have a new first statement of sleep for 1 second,
to make sure that the first thread runs first invoking the firstThread() method, locking
on the lock, and releases that lock due to the invocation of the await() method, then the
second thread wakes up after the 1 second sleep, and will acquire the lock (lock on the lock),
after which, we'll prompt and wait for the Enter key from the user, after which we'll
invoke the cond.signal() method which is the equivalent of the notify() method.
The invocation of signal() will awake the waiting thread so it can get its chance to 
acquire the lock again and continue running, but that's not enough, namely, after the
invocation of signal() we should explicitly unlock the lock, because without unlocking the lock 
we're going to face a problem in which the t2 thread will never unlock the lock and that'll
cause the other thread (t1) to be awake but it will not be able to lock on the lock, 
or in other words to acquire the lock, hence it'll be stuck forever (DEADLOCK).
*/
/*
In the next lecture we'll discuss deadlocks, and how to avoid them by using a method of the
ReentrantLock class called tryLock() (which is one of the big advantages of using the 
ReentrantLock class) which enables us to try to acquire the lock and let us know if we're 
succeeded or not, and that's cannot be done using synchronized, however, we must remember
to explicitly unlock the ReentrantLock lock using lock.unlock(), and that's a disadvantage.
*/
/*
If a thread wants to invoke the wait() or notify() method on some object, 
the thread must acquire that object lock first, so when using low-level synchronization 
(as opposed to using high level synchronization using classes such as the ReentrantLock class)
the wait() or notify() methods must be invoked within a synchronized block and the 
thread that invokes them on an object (could be any object) must acquire that object 
intrinsic lock AKA monitor lock before that.
*/
/*
 public interface Lock
Lock implementations provide more extensive locking operations than can be obtained using 
synchronized methods and statements. They allow more flexible structuring, may have quite 
different properties, and may support multiple associated Condition objects.

A lock is a tool for controlling access to a shared resource by multiple threads. 
Commonly, a lock provides exclusive access to a shared resource: only one thread at a time 
can acquire the lock and all access to the shared resource requires that the lock be acquired 
first. However, some locks may allow concurrent access to a shared resource, such as the 
read lock of a ReadWriteLock.

/-/-/-/-/-/-/-/-/-/-/-/-/-/-//-/-/-/-/-/-/-/-/-/-/-/-/-/-/
The use of synchronized methods or statements provides access to the implicit monitor lock
(AKA intrinsic lock) associated with every object, but forces all lock acquisitions and 
releases to occur in a block-structured way: when multiple locks are acquired they must
be released in the opposite order, and all locks must be released in the same lexical scope
in which they were acquired.
/-/-/-/-/-/-/-/-/-/-/-/-/-/-//-/-/-/-/-/-/-/-/-/-/-/-/-/-/
 * 
While the scoping mechanism for synchronized methods and statements makes it much easier 
to program with monitor locks, and helps avoid many common programming errors involving locks,
there are occasions where you need to work with locks in a more flexible way. 
For example, some algorithms for traversing concurrently accessed data structures require 
the use of "hand-over-hand" or "chain locking": you acquire the lock of node A, then node B,
then release A and acquire C, (rather than release B first and then A) then release B and 
acquire D and so on.
/-/-/-/-/-/-/-/-/-/-/-/-/-/-//-/-/-/-/-/-/-/-/-/-/-/-/-/-/ 
Implementations of the Lock interface enable the use of such techniques by allowing a lock
to be acquired and released in different scopes, and allowing multiple locks to be acquired
and released in any order.
/-/-/-/-/-/-/-/-/-/-/-/-/-/-//-/-/-/-/-/-/-/-/-/-/-/-/-/-/

With this increased flexibility comes additional responsibility!!!!!
The absence of block-structured locking removes the automatic release of locks that occurs
with synchronized methods and statements. In most cases, the following idiom should be used:

 Lock l = ...;
 l.lock();
 try {
   // access the resource protected by this lock
 } finally {
   l.unlock();
 }
 */
/*
  public class ReentrantLock extends Object implements Lock, Serializable
A reentrant mutual exclusion Lock with the same basic behavior and semantics as the implicit 
monitor lock accessed using synchronized methods and statements, but with extended capabilities.

A ReentrantLock is owned by the thread last successfully locking, but not yet unlocking it.
We see that we need to explicitly acquire the lock and explicitly release the lock, and 
this is done using the methods lock() and unlock() respectively, all of this is as opposed
to using synchronized methods and synchronized statements in which the acquisition and
release of the locks are done implicitly.

A thread invoking lock() will return, successfully acquiring the lock, when the lock is not 
owned by another thread. 
The method will return immediately if the current thread already owns the lock. 
This can be checked using methods isHeldByCurrentThread(), and getHoldCount().
It is a recommended practice to always immediately follow a call to lock with a try block, 
most typically in a before/after construction such as:

 class X {
   private final ReentrantLock lock = new ReentrantLock();
   // ...

   public void m() {
     lock.lock();  // block until condition holds
     try {
       // ... method body
     } finally {
       lock.unlock()
     }
   }
 }
 */

public class Runner
{
    private int  count = 0;
    private Lock lock  = new ReentrantLock();

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
        //System.out.println(((ReentrantLock)lock).getHoldCount());//Prints 1
        try
        {
            increment();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void secondThread () throws InterruptedException
    {
        lock.lock();
        try
        {
            increment();
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