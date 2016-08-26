package lec2_volatile;

/*The best way to learn and read on volatile is using the relevant AP.*/

/*
We have 2 threads access the same data (A field called running).
The main thread accesses the running field by invoking the shutdown method.

(The shutdown method isn't invoked from
within the Processor class) - Here the running field is changed from the main thread,
by the invocation of the method proc1.shutdown();  .

The technique we see here is useful if we'd like to shutdown a thread or multiple threads-
(We'll call it thread_1) gracefully from another thread (We'll call it thread_2) 
by accessing fields of thread_1 from thread_2 (And this is where the volatile keyword is 
important), which is what we did here, we access a field of the Processor thread class
from the main thread, and by doing so we gracefully shutdown the Processor thread.

tutorials.jenkov.com/java-concurrency/volatile.html#the-java-volatile-visibility-guarantee:
The Java volatile keyword is used to mark a Java variable as "being stored in main memory".
More precisely that means, that every read of a volatile variable will be read from the 
computer's main memory, and not from the CPU cache, and that every write to a volatile 
variable will be written to main memory, and not just to the CPU cache.

The real definition and usage of the volatile keyword.
Taken from the following links:
https://docs.oracle.com/javase/tutorial/essential/concurrency/memconsist.html 
https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html
docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html#MemoryVisibility
https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4.5
stackoverflow.com/questions/3519664/difference-between-volatile-and-synchronized-in-java
http://javarevisited.blogspot.co.il/2011/06/volatile-keyword-java-example-tutorial.html

Memory Consistency Errors
-------------------------------
Memory consistency errors occur when different threads have inconsistent views of what should
be the same data. The causes of memory consistency errors are complex and beyond the scope of
this tutorial. 
Fortunately, the programmer does not need a detailed understanding of these causes.
All that is needed is a strategy for avoiding them.

The key to avoiding memory consistency errors is understanding the happens-before 
relationship.
This relationship is simply a guarantee that memory writes by one specific statement are 
visible to another specific statement (Not necessarily following statements).

Chapter 17 of the Java Language Specification defines the happens-before relation on memory 
operations such as reads and writes of shared variables.
The results of a write by one thread are guaranteed to be visible to a read by another 
thread only if the write operation has a happens-before relationship with the read operation.

The programmer has to establish a happens-before relationship between these two statements.
It should be noted that when we create a synchronization mechanism for accessing
some data, it makes the access to that data, an atomic action, however, according to the
terminology used by Javadoc, synchronized access is different than atomic access, as
explained below.

-->So for example if I do int x=0; x++; then the value of x that is visible to a statement
called y that is in the HB relationship with the statement x++, is 1.
So, we have 2 statements in the HB relationship, one is x++,and the second is statement y.
If x++ and y, are statements that have the HB relationship, it means that when y reads the
value of x, the visible value would be 1, that is the statement x++ writes to memory, and
only when it's done, the y statement reads it.

(The statement following x++ (statement y) can also be on another thread).

To see this, consider the following example. 
Suppose a simple int field is defined and initialized:
int counter = 0;
The counter field is shared between two threads, A and B. 
Suppose thread A increments counter:
counter++;
Then, shortly afterwards, thread B prints out counter:
System.out.println(counter);

If the two statements had been executed in the same thread, it would be safe to assume 
that the value printed out would be "1". 
But if the two statements are executed in separate threads, the value printed out might 
well be "0", because there's no guarantee that thread A's change to counter will be visible
to thread B — unless the programmer has established a happens-before relationship between 
these two statements. (In other words established a synchronization mechanism on the shared
data, that is the field count).

There are several actions that create happens-before relationships.
One of them is synchronization, as we will see in the following sections.
We can also say that using synchronization, we can make operations that are not natively 
atomic, an atomic ones, but according to the terminology using synchronization we
can create Happens-Before relationships (It's pretty much the same).

We've already seen two actions that create happens-before relationships.
1. When a statement invokes Thread.start(), every statement that has a happens-before 
relationship with that statement also has a happens-before relationship with every statement
executed by the new thread.
The effects of the code that led up to the creation of the new thread are visible to the new
thread (Namely the new running thread code is in HB with the code that led up to its creation
so every write operations that code did, is visible to the new thread code).
2. When a thread terminates and causes a Thread.join in another thread to return, then all
the statements executed by the terminated thread have a happens-before relationship with all 
the  statements following the successful join. The effects of the code in the thread are 
now visible to the thread that performed the join (The thread that was waiting on a join,
has all of the terminated thread's effects, which is whatever write operations it did).

Atomic Access
In programming, an atomic action is one that effectively happens all at once. 
An atomic action cannot stop in the middle: it either happens completely, or it doesn't 
happen at all. No side effects of an atomic action are visible until the action is complete.
The definition applies to actions that are natively atomic in Java.

We have already seen that an increment expression, such as c++, does not describe an atomic
action. 
Even very simple expressions can define complex actions that can decompose into other actions
However, there are actions you can specify that are atomic:
1. Reads and writes are atomic for reference variables and for most primitive variables 
 (all types except long and double).
2. Reads and writes are atomic for all variables declared volatile
 (including long and double variables). - The definition refers to writes that are atomic,
 hence, if we have a volatile variable called var, then var++, is not an atomic operation
 and even declaring var as volatile will not make it atomic.
 
 
The programmer has to establish a happens-before relationship between two statements in
order to avoid memory inconsistency errors.
It should be noted that when we create a synchronization mechanism for accessing
some data, it makes the access to that data, an atomic action, however, according to the
terminology used by Javadoc, synchronized access is different than atomic access, as
explained below:

Atomic actions cannot be interleaved,so they can be used without fear of thread interference.
However, this does not eliminate all need to synchronize atomic actions, because memory 
consistency errors are still possible.
Using volatile variables reduces the risk of memory consistency errors, because any write 
(write that is an atomic action) to a volatile variable establishes a happens-before 
relationship with subsequent reads of that same variable. 
This means that changes to a volatile variable are always visible to other threads. 
What's more, it also means that when a thread reads a volatile variable, it sees not just the
latest change to the volatile, but also the side effects of the code that led up the change.

Using simple atomic variable access is more efficient than accessing these variables 
through synchronized code, but requires more care by the programmer to avoid memory 
consistency errors. Whether the extra effort is worthwhile depends on the size and 
complexity of the application.

Some of the classes in the java.util.concurrent package provide atomic methods that do not 
rely on synchronization. We'll discuss them in the section on High Level Concurrency Objects.
*/

import java.util.Scanner;

class Processor extends Thread
{
    //private boolean running = true;
    private volatile boolean running = true;

    @Override
    public void run ()
    {
        while (running)
        {
            System.out.println("Hello");
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void shutdown ()
    {
        running = false;
    }
}//Processor class

public class App
{
    public static void main (String[] args)
    {
        Processor proc1 = new Processor();
        proc1.start();
        System.out.println("This is the main thread, Press Enter to stop the other thread");
        try (Scanner scanner = new Scanner(System.in))
        {
            //Pauses execution in the main thread until I hit the Enter key
            scanner.nextLine();
        }
        //The main thread accesses a shared mutable state of the Processor class called running.
        //As we can see, that shared state is accessed by 2 threads concurrently, the main thread
        //writes to this field, and the Processor thread reads it, so in order to guarantee
        //visibility of memory writes  across multiple threads, we must use some
        //form of synchronization, and here we do it using volatile.
        //In other words, 2 threads operate on the same state at the same time AKA they interleave
        //on that state - This is no interleaving!!!!!!!!!!!!!!!!!!!It doesn't coincide with the
        //definition of interleaving.
        proc1.shutdown();
    }//main
}//App class