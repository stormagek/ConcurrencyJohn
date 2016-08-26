package lec4_ThreadInterferenceAndInterleavingIntrinsicLockSynchronized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/*
If we invoke the method process(); from the Worker.start() method, we'd have issues. 
Calling this method without both threads,
(Namely we can just call this method from the main thread in the start() method without
using more running threads). 
This method takes the runtime of roughly 2 seconds, actually a bit more
than 2 seconds) because in each iteration of the for loop, there is 
an invocation of methods stageOne and stageTwo, and each of which involve sleep of 1
millisecond, that's 2 milliseconds for each iteration and since we have 1000
iterations it's roughly 2 seconds. (At this point, both methods aren't synchronized).
So by invoking the process() method from the main thread while the main thread is the only
active running thread, gives the expected results, that is each list's size is 1000 and the
time it took is 2015 which is ~ 2 seconds, we'll try to speed it up.
*/
/*
OK, We'll try to speed it up by using 2 threads (Other than the main thread, that is, 
both threads will each invoke the process() method and the main thread will not invoke this 
method), each of which will invoke process(), so I'll comment out the earlier invocation of
process() within the start method.
It's worth mentioning that if we had only 1 thread (such as t1) that calls the
process method, we'd get the same time roughly, that is 2 seconds.
Even with 2 threads (t1,t2) (While the process method is not synchronized) it's still 
roughly 2 seconds because both threads run concurrently, that is when 1 thread sleeps
for 1 millisecond the other one runs so there is no "dead time" during execution,
that is when 1 thread sleeps, another one runs.
*/
/*
The problem we face now is that sometimes both list sizes are 2000 each, and sometimes
it's not, this is because writing to a list is not a single step operation
(Not a native atomic operation) - if more than 1 thread interleaves on that list, 
unexpected results may occur (As we've seen in lecture 3).
The important thing to remember here is that when the collection is not thread-safe, (Vector 
is an example of a thread-safe collection) interleaving threads on the same collection such
as the ArrayList in this code, may generate unexpected and erroneous results.
The ArrayList.add method looks like this (PseudoCode):
public boolean add(E e) {
    ensureCapacity(size + 1);
    elementData[size++] = e;
    return true;
}
So, we see the operation is not atomic, hence without synchronization strange behavior is due.
*/
/*
To solve this, as we saw in Lecture 3, both methods will be defined with synchronized,
and now whenever we run the program both sizes will be 2000, but the running time will 
be roughly 4 seconds, because now when a thread executes one of the 2 synchronized methods
it also acquires the Worker's intrinsic lock (because we're locking on a Worker instance),
so no other thread can run the other synchronized method until the previous thread releases
that lock, so if we have 2 complete milliseconds of sleeping time in each iteration for each
thread, and each thread does 1000 iterations, it adds up to roughly 4 seconds.
So, we see that since the Worker object has only 1 intrinsic lock, the runtime is 
twice as much than we'd expect.
*/
/*
The key to understand is that both synchronized methods here are independent,
that is, they access different data (stageOne accesses list1, and stageTwo accesses
list2), why then, while 1 thread executes stageOne(), no other thread is able to
execute stageTwo()? Namely, we can allow both methods to be invoked concurrently by more than
1 thread at the same time, but in the same vein, we must synchronized the access to both lists, 
list1 and list2, so threads will not be able to interleave on them.
In other words, we can allow each thread to invoke each of these methods at the same time,
concurrently, namely when t1 invokes stageOne() we can allow t2 to invoke stageOne() or
stageTwo at the same time (concurrently) and vice versa, without worrying about possible
interleaving on the same data, because each method acts on different data,
stageOne() on list1, and stageTwo() on list2.
So, because synchronizing both methods increases runtime, we will try to optimize it.
What we want to accomplish is that:
1. 2 threads may run stageOne method at the same time (concurrently),
   but No 2 threads can interleave on list1.
2. 2 threads can run stageTwo method at the same time (concurrently),
   but No 2 threads can interleave on list2.
3. We want to allow one thread to run stageOne while another thread may run either
   stageOne or stageTwo, and vice versa because again, they're not acting on the same data.
*/
/*
We can do that by creating separate locks and synchronizing on these 2 locks separately.
So instead of having a synchronized method, we'll have synchronized statements.
Synchronized statements work similarly to synchronized methods as far as the 
acquisition of the intrinsic lock goes, however, now more than 1 thread can run each
of the methods at the same time. 
Because we're locking on 2 different objects (lock1 and lock2), one thread can run one
synchronized statement and another thread can run a second synchronized statement at the
same time, one thread acquires the intrinsic lock of lock1 object
and the second thread acquires the intrinsic lock of lock2 object.
We've achieved our goal, that is, one thread can run one synchronized statement while 
another thread runs another synchronized statement, BBUUTT no 2 threads can run
either synchronized statement at the same time.
*/
/*
Running the program shows that we get runtime of roughly 2 seconds, because now we
don't have to wait for a Worker instance's intrinsic lock to be released,
so when 1 thread goes to sleep for 1 millisecond, the other thread keeps on running,
there is no complete halt of the program for that 1 millisecond of the sleep.

How did we improve the runtime comparing to what we had at the first paragraph? 
We could change the number of iterations to 500 and the result would be that each list's size
is 1000 and the time it took is approximately 1000 MS, that is 1 second, hence it's a
better performance than a program with only 1 thread where the number of iterations were 1000,
and each list's size is 1000 and the time it took is 2015 which is ~ 2 seconds, so we can see
we improved the runtime by 1 second approximately.
*/
/*
One might wonder why didn't we lock on list1 and list2? He says it'd probably 
work, but it's always a good practice to declare separate lock objects - Ask E-donkey about
that.
Great deal of attention must be given if the same collection can be accessed in 2 different
classes.
Consider the following scenario:
class A has a field of type List called list1, and this field has a public getter.
The field is defined as follows: private List list1=new ArrayList();
class B has a field of type List called list2, and it's initialized to null.
list2 has a public setter.

Within the main method of class A, we'll have: 
A a1=new A();B b1=new B();b1.setList2(a1.getList1); Hence we have the same List accessed by
instances of 2 different classes, and we can design the access to list1 in class A to be
synchronized, so accessing this List will be done in a thread-safe way no many how many threads
are defined within class A that are trying to access that List, however, the List field
within class B (list2), refers to the same List of class A (list1), but if we are careless,
access to this List field within class B by threads defined in class B,
could be done in a non-thread-safe way.
A possible solution for this is to return a synchronized wrapper reference view of the
List in the getter of class A, using code like this: 
return Collections.synchronizedList(A's List), so A and B will both have access to the same
List field defined in class A, but, in a thread-safe way.
*/
/*
Look at this link for more info:
http://stackoverflow.com/questions/25260481/should-one-synchronize-on-a-list-itself-or-on-a-lock-object
http://blog.takipi.com/5-things-you-didnt-know-about-synchronization-in-java-and-scala/
 */
/*
The following comment is unrelated, it's just for me!
     public synchronized void stageOne ()
    {
        //Let's assume this method does some calculation that takes time or maybe it 
        //waits for some data from other source, so to simulate this we'll slow the program
        //just for a bit with the sleep method, and also simulate that the returned data is 
        //added to list1
//        synchronized (lock1)
//        {
//            list1.add(4);
//            list1.add(new Integer(23));
//            Iterator<Integer> it=list1.iterator();
//
//            try 
//            {
//                Thread.sleep(1);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//            while(it.hasNext())
//            {
//                it.next();
//                it.remove();
//            }
//            list1.add(random.nextInt(100));//This line doesn't cause an exception 
//            //because the iterator is done iterating over the collection
//        }
//    }
 */
/* Accoring to e-donkey, it'd be a better practice to lock on a separate lock object, rather than
 * on the list itself, beofre proceeding here read the previous comment. 
 * Consider the scenario below, where we might have million business lists
 * and million private lists, we could only imagine how hard it could get to maintain and
 * understand our code, if we were to use a specific list as a lock before we want 
 * to access that specific list, rather, it'd be better to have 1 lock, or maybe more than one 
 * to access each list (Chances are we would not need 2 million different locks...).
 * Also, what happens if we need to synchronize access to million lists within 1 method, one
 * after the other (This example refers to a single synchronized statement within some method and
 * the questions asked now, is what object should we lock on for this synchronized statement),
 * like this:
 * listBusines1.add(e); listBusines2.add(e); listBusines3.add(e); etc. 
 * on which list should we lock on? on listBusines1? or listBusines2? If we were to lock on
 * listBusines1 for example, its intrinsic lock will be acquired.
 * If we have another method that has the exact same code, that is:
 * listBusines1.add(e); listBusines2.add(e); listBusines3.add(e);,
 * then we should remember that in order to insure memory visibility we should lock on the same
 * lock, but then things can get confusing and messy, what would we choose to lock on?
 * listBusines1 or listBusines2 or listBusines3 etc.??? Maintaining the code gets more difficult.
 * It'd be better to have 1 object as lock, or few objects as locks, and whenever we access
 * business lists, we'd use lockBusines1 for listBusiness1, or if we need to access many lists,
 * within a single synchronized statement, we can use a generic lockBusines object as a lock.
 *
 * One more approach from the link provided:
 * both options are more or less equivalent as long as you consistently lock your object. 
 * Therefore synchronization on the list itself (as a policy) is maybe more error-free since 
 * you can't accidentally use "another object" for synchronization - Recall that in order to 
 * guarantee memory visibility of memory writes across multiple threads, we must lock on the
 * same lock object, and by using the list itself as the lock, we reduce the chance that we might
 * lock on different lock when the list is accessed by many methods.
 *
 *At the link provided, it's mentioned that it always better to let Java do the heavy lifting 
 *for us, like: syncList = Collections.synchronizedList(yourList), which returns a synchronized
 *(thread-safe) list backed by the specified list, however, it would not be safe
 *in the following example:
 *if (syncList.contains(element)) { syncList.add(element); }
 *This code snippet because what we want, is the operation Put-If-Absent to be atomic, and this
 *snippet doesn't represent an atomic version of the operation. When the control is in the 
 *evaluation of the if-statement's condition, the contains method is invoked in a thread-safe way,
 *namely, no other thread can access syncList until the method contains(), finishes.
 *When the method finishes, it releases the lock it used to synchronize access to syncList.
 *Just before executing { syncList.add(element); }, other thread may gain access to syncList, by
 *acquire the lock needed to synchronize access to syncList, hence the operation Put-If-Absent,
 *will not be executed in an atomic way, and that should be noted!
 *
 *
 * Any way, we should read the links I've provided at the documentation above.
 * 
 * private Object lockBusines1 = new Object();
   private Object lockPrivate2 = new Object();

    private List<Integer> listBusines1 = new ArrayList<Integer>();
    private List<Integer> listBusines2 = new ArrayList<Integer>();
    private List<Integer> listBusines3 = new ArrayList<Integer>();
    private List<Integer> listBusines4 = new ArrayList<Integer>();
    private List<Integer> listBusines5 = new ArrayList<Integer>();
    private List<Integer> listBusines6 = new ArrayList<Integer>();

    private List<Integer> listPrivate1 = new ArrayList<Integer>();
    private List<Integer> listPrivate2 = new ArrayList<Integer>();
    private List<Integer> listPrivate3 = new ArrayList<Integer>();
    private List<Integer> listPrivate4 = new ArrayList<Integer>();
    private List<Integer> listPrivate5 = new ArrayList<Integer>();
    private List<Integer> listPrivate6 = new ArrayList<Integer>();
*/

public class Worker
{
    private Random        random = new Random();
    private Object        lock1  = new Object();
    private Object        lock2  = new Object();
    private List<Integer> list1  = new ArrayList<Integer>();
    private List<Integer> list2  = new ArrayList<Integer>();

    /*
    Let's assume this method does some calculation that takes time or maybe it 
    waits for some data from other source, so to simulate this we'll slow the
    program just for a bit with the sleep method, and also simulate that the
    returned data is added to list1
     */
    public /*synchronized*/ void stageOne ()
    {

        synchronized (lock1)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            list1.add(random.nextInt(100));
        }
    }

    /*
    Let's assume this method does some calculation that takes time or maybe it 
    waits for some data from other source, so to simulate this we'll slow the
    program just for a bit with the sleep method, and also simulate that the
    returned data is added to list2
     */
    public /*synchronized*/ void stageTwo ()
    {
        synchronized (lock2)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            list2.add(random.nextInt(100));
        }
    }

    public void process ()
    {
        for (int i = 0; i < 1000; i++)
        {
            stageOne();
            stageTwo();
        }
    }

    public void start ()
    {
        System.out.println("Starting...");
        long start = System.currentTimeMillis();

        //process(); //This line pertains the first paragraph at the top of the file.

        Thread t1 = new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                process();
            }

        });

        Thread t2 = new Thread(new Runnable()
        {

            @Override
            public void run ()
            {
                process();
            }

        });
        t1.start();
        t2.start();
        try
        {
            t1.join();
            t2.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("Time take: " + (end - start));
        System.out.println("list1: " + list1.size() + " " + "list2 " + list2.size());

    }//start method

}//End of public class Worker