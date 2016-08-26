package lec7_arrayBlockingQueue;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
Blocking queues - We'll look at the Consumer Producer Pattern.
The BlockingQueue implementations in the java.util.concurrent package, are thread safe, 
so I can access an instance of that class from multiple threads without worrying about threads 
synchronization or threads interference.
The idea of producer/consumer is that we have 1 or more threads that produce elements,
and those threads add these elements to a shared mutable field that is shared between them -
In our example that would be a queue.
Other threads remove elements from that shared mutable data and do some processing on them.
*/
/*
We'll have the methods producer() and consumer() that do just that, and each of them will be
invoked by 2 different threads.
Within the methods producer() and consumer() we'll invoke the methods put() and take()
respectively (Methods of the BlockingQueue interface). 
What's good about the methods put() and take() is: 
1. They're implicitly synchronized because the ArrayBlockingQueue class is thread safe.
2. If the queue is empty, take() will wait until an element is added to the queue and then
   it'll fetch it, and put() will wait in case the  queue is full,
   until an element is removed, and then it'll enter its element.
He claims that both methods work within our system resources - whatever that means!!!!
*/
/*
When we run the program, the thread that invokes producer() will add elements to the 
queue as fast as it can, the thread that invokes consumer() does that 10 times a second,
and it takes an element from the queue once in every 10 times on average, so in average
it takes an element from the queue 1 time a second.
Everything is synchronized perfectly, put() and take() will wait if needed
(as explained earlier).
*/
/*
It's always better to avoid low level synchronization using the keyword synchronized, and
the methods notify() and wait(), rather, it's always recommended to use the thread safe
classes from the package java.util.concurrent.
In the next lecture we'll look at how to do that, using low-level synchronization 
techniques, with the notify() and wait() methods and the synchronized keyword.
*/
/*
public interface BlockingQueue<E> extends Queue<E>
A Queue that additionally supports operations that wait for the queue to become non-empty
when retrieving an element, and wait for space to become available in the queue when storing
an element (For example the take() and put() methods).
This interface also supports the methods add and poll that adds and removes elements
from the queue respectively, but without waiting in case the queue is full or empty
 */
/*
public E take() throws InterruptedException
Description copied from interface: BlockingQueue
Retrieves and removes the head of this queue, waiting if necessary until an element becomes
available.
Specified by:
take in interface BlockingQueue<E>
Returns:
the head of this queue
 */
/*
public void put(E e) throws InterruptedException
Inserts the specified element at the tail of this queue, waiting for space to become available
if the queue is full.
Specified by:
put in interface BlockingQueue<E>
Parameters:
e - the element to add
*/

public class App
{
    //Maximum size of 10
    private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);

    public static void main (String[] args)
    {
        Thread t1 = new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                try
                {
                    producer();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                try
                {
                    consumer();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
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
    }//End of main method

    /*
    This method could be a thread that checks for text messages sent by users to some
    server, and these messages are stored by the thread in the queue.
    We'll also simulate another thread that takes (removes) messages from that queue and
    send them to their destination
     */
    private static void producer () throws InterruptedException
    {
        Random random = new Random();
        while (true)
        {
            queue.put(random.nextInt(100));//This is just to simulate some real work
        }
    }

    /*
    Because in real systems, fetching elements from the
    queue and process them takes time, we'll simulate that using a random value
     */
    private static void consumer () throws InterruptedException
    {
        Random random = new Random();
        Integer value;
        while (true)
        {
            Thread.sleep(100);
            if (random.nextInt(10) == 0)
            {
                value = queue.take();
              //@formatter:off
                System.out.println("Taken value: " + value + " " + "Queue size is: " 
                            + queue.size());
              //@formatter:on
            }
        }
    }
}//End of public class App