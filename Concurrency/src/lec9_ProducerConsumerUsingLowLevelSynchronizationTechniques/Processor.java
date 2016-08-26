package lec9_ProducerConsumerUsingLowLevelSynchronizationTechniques;

import java.util.LinkedList;
import java.util.Random;

/*
A Working Example Using Low-Level Synchronization - This lecture is linked to lecture 7,
so I should overview it before proceeding with the reading here.
The example of Producer/Consumer design pattern from lecture 7 will be implemented here using
low level thread synchronization techniques.
*/
/*
Within the main method, 1 thread invokes produce() and the second thread invokes consume().
We'll have shared mutable state between both threads, which is an instance of the Processor
class, and by extension, that instance's mutable fields are also considered as shared mutable
state between the threads (in this case it's a field called list of type LinkedList of
size of maximum 10).
Just as we did in lecture 7, t1 thread will use produce() to insert elements to the list,
and the t2 thread will use consume() to remove elements from the list.
We'll implement a queue (FIFO) structure, where the producer thread adds an element to 
the end (tail) of the list, and the consumer thread removes an element from  the head of the
list.
*/
/*
Now we need synchronization, we'll create a field of type Object called lock, to lock upon.
We could lock on the Processor object itself (As can be seen in lecture 8), but rather he chose
a separate lock in order to emphasize that we need to call wait() and notify() on the object
that the current thread is locking upon (Synchronizing upon), and that was not emphasized
enough in lecture 8.
We've added the synchronized statements, each of which locks on the lock object.

What's left to do is to make sure that the producer thread can only add items if the list
is not full, and full in our implementation is when it has 10 elements.
The producer thread invokes wait() when the list if full, and it's awakened when there
is a space available in the list.
In lecture 7, this integrity was guaranteed for us by the ArrayBlockingQueue instance, which 
has the methods take and put that does the same for us, only implicitly.
*/
/*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Why we should call the wait() method within a loop?????
In the produce() method, he uses a nested while-loop, because normally (also according to
the Javadoc) we'd want to check that the reason we invoked wait() in the first place, is no
longer valid or true, so the nested loop makes sure we do that.
An elaboration by John on the nested while-loop:
""""""
It's there because when other thread notifies you that you can wake up, 
that doesn't mean you should trust that there is really more space in the list. 
Especially, if there are other threads, another one might fill the space before you can 
in your thread, because invocation of notify() awakens the waiting thread but that doesn't
guarantee that it would be given CPU time, it does guarantee that the awakened thread
competes with all other threads for CPU time. 
So the thing to do when you're woken up is to check if there is really space,
then go back to waiting if there isn't. This way you can be really sure that you only add an item
to the list if
 1. you are woken up, 
 and 
 2. there is space in the list. 
Anything else and you should loop around and wait again.
""""""
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
*/
/*
We'll use nested while-loop in the consume() method as well.
In the consume() method, the last statement of the outer while-loop would be sleep of 1
second  (I've changed it to 5) just to show that an invocation of notify() will not
immediately give CPU time to the awakened thread ---> It's wrong - I've elaborated on that
below class Processor.
It's wrong because a thread releases the intrinsic lock when the synchronized statement
block is done, the fact that the sleep is executed just means that the current thread
(the consumer) kept running after the invocation of notify(), and we should be aware that
it's not necessarily the case all the time, it depends on how the OS schedules threads.
Once the notify() is invoked, any threads that are waiting on the same lock object, are awakened,
and then the awakened threads and the thread invoking notify() compete fair and square for
run time.
*/
/*
In lecture 7, we saw the same implementation of 2 threads, one calls produce() and the other
calls consume(), with a shared mutable data that was BlockingQueue<Integer>, that is a queue
implementation, and we've used its methods put() and take() to produce() and to consume()
respectively, the put() method waits if the queue is full and the take() method waits
if the queue is empty, and they're thread safe methods
---->Same happens here, we're just using low level techniques.
*/
/*          
This comment pertains the code line: Thread.sleep(random.nextInt(5000)); in the consume method:
-----------------------------------------------------------------------------------------------
Only after the 5 seconds period of sleep is over, the producer thread gets CPU
and it fills the list, so as we can see, with that sleep statement, the list
will never be empty because for every element the consumer thread removes,
the producer thread has ~5 seconds for adding new elements.
           Thread.sleep(random.nextInt(5000));
           //    while (true)
           //    {
           //        System.out.println("Yoohoo");
           //    }
Here to show that an invocation of notify() 
will not immediately give CPU time to the awakened thread.
After the synchronized statement is done, the current thread releases the
lock, so the fact that there is an infinite loop here, doesn't mean that 
the current thread didn't release the lock, it does seems like so though.
It just that the producer thread cannot enter more elements to the queue,
because it's full, and the consumer thread cannot remove elements from 
the queue because of the infinite loop.
We can test this, by initializing list to 100000 rather than 10, then we'd
see, that while the consumer thread is stuck with the infinite loop, the
producer thread keeps on running, because it takes some time to fill the queue
with 100000 elements. As soon as the queue's size becomes 100000, the program will hang, because,
as explained, since the queue is full the producer thread cannot add elements to the queue 
because it's full, and the consumer will be spins forever in the infinite loop.
*/

public class Processor
{
    /*This LinkedList is a shared mutable state among threads*/
    private LinkedList<Integer> list  = new LinkedList<Integer>();
    private final int           LIMIT = 10;                       /*private final int LIMIT = 100000;*/
    private Object              lock  = new Object();

    public void produce () throws InterruptedException
    {
        int value = 0;
        while (true)
        {
            synchronized (lock)//Synchronizing on the lock object
            {
                //If the list is full, wait.
                while (list.size() == LIMIT)
                {
                    System.out.println("FuckIt");
                    lock.wait();
                    //Calling wait() on the object that we're locking on.
                    //Calling just wait() will cause many problems, because as
                    //mentioned, wait() can be called only on objects that their 
                    //lock is owned by the current thread, and just calling wait()
                    //will cause an exception because it'd mean that the thread
                    //that invokes wait() does it on an object (instance of
                    //Processor) without owning the intrinsic lock of that object - Rather,
                    //in our example the current thread invokes wait() on
                    //the same object it synchronizes on, namely it owns the
                    //intrinsic lock of that object (called lock).
                }
                list.add(value++);
                System.out.println("List size after adding is: " + list.size());
                lock.notify();//wakes up the other thread.
                //After the notify(), the producer thread returns to the while(true)
                //loop, effectively releases the lock object's intrinsic lock, because
                //right after the notify() the synchronized block ends.
            } //Synchronized
        }
    }

    public void consume () throws InterruptedException
    {
        Random random = new Random();
        int value = 0;
        while (true)
        {
            synchronized (lock)//Synchronizing on the lock object
            {
                //If the list is empty, wait.
                while (list.size() == 0)
                {
                    System.out.println("shappa");
                    lock.wait();
                }
                System.out.print("List size before removing is: " + list.size());
                value = list.removeFirst();
                System.out.println("; value removed: " + value);
                lock.notify();
            } //Synchronized

            //Look at my comment above class Processor for elaboration of this line and its
            //effects on the wait() notify() mechanism.
            Thread.sleep(random.nextInt(5000));
        }
    }
}//End of class Processor