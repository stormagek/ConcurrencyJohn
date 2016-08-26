package lec1_1_CreateAndStartAThread;

//The course and source are code taken from 
//https://www.youtube.com/watch?v=YdlnEWC-7Wo&list=PLBB24CFB073F1048E

/*
Processes and Threads - Important Information from JavaDoc
https://docs.oracle.com/javase/tutorial/essential/concurrency/procthread.html
Processes and Threads

In concurrent programming, there are two basic units of execution: processes and threads. 
In the Java programming language, concurrent programming is mostly concerned with threads. 
However, processes are also important.

A computer system normally has many active processes and threads. This is true even in systems 
that only have a single execution core, and thus only have one thread actually executing at any 
given moment. Processing time for a single core is shared among processes and threads through an 
OS feature called time slicing.
It's becoming more and more common for computer systems to have multiple processors or processors
with multiple execution cores. This greatly enhances a system's capacity for concurrent 
execution of processes and threads — but concurrency is possible even on simple systems, 
without multiple processors or execution cores.

Processes
--------------
A process has a self-contained execution environment. A process generally has a complete, 
private set of basic run-time resources; in particular, each process has its own memory space.
Processes are often seen as synonymous with programs or applications. However, what the user sees
as a single application may in fact be a set of cooperating processes.
To facilitate communication between processes, most operating systems support Inter Process 
Communication (IPC) resources, such as pipes and sockets. IPC is used not just for communication
between processes on the same system, but processes on different systems.

Most implementations of the Java virtual machine run as a single process. A Java application can 
create additional processes using a ProcessBuilder object. Multiprocess applications are beyond 
the scope of this lesson.

Threads
--------------
Threads are sometimes called lightweight processes. Both processes and threads provide an 
execution environment, but creating a new thread requires fewer resources than creating a new 
process.
Threads exist within a process — every process has at least one. 
Threads share the process's resources, including memory and open files. 
This makes for efficient, but potentially problematic, communication.

Multithreaded execution is an essential feature of the Java platform. Every application has 
at least one thread — or several, if you count "system" threads that do things like memory 
management and signal handling. But from the application programmer's point of view, 
you start with just one thread, called the main thread. This thread has the ability to create 
additional threads, as we'll demonstrate in the next section.

======================================================================================
*/
/*
 More on threads:
 
http://www.javaworld.com/article/2074217/java-concurrency/java-101--understanding-java-threads--part-1--introducing-threads-and-runnables.html
http://www.javaworld.com/article/2078809/java-concurrency/java-concurrency-java-101-the-next-generation-java-concurrency-without-the-pain-part-1.html
----------------------
*/
/*Every running thread is an instance of the Thread class, and when a thread is
started, the JVM creates an instance of that class, and runs its run() method
in a separate thread.

Starting threads
------------------------
First way to start a thread:
Extend the Thread class and override the run method - According to the run() method
documentation below, if we don't override this method, it does nothing.

This way for creating and starting a thread is less preferable, because in Java a class
may extend/inherit/subclass only one class, so by extending the Thread class, we're 
exhausting the one and only "slot" for the class to use inheritance, hence, it's recommended to 
always use the way illustrated in demo2, which is to implement the Runnable interface.
*/
/*The Thread class implements Runnable - Here is the definition of the class from Javadoc:
public class Thread
extends Object
implements Runnable
*/
/*
public void run() - From Javadoc:
If this thread was constructed using a separate Runnable run object, then that Runnable
object's run method is called; otherwise, this method does nothing and returns.
Subclasses of Thread should override this method.
 */
/*
From Javadoc:

    Thread.sleep-
     Causes the currently executing thread to sleep (temporarily cease execution)
     for the specified number of milliseconds, subject to the precision and accuracy 
     of system timers and schedulers. The thread does not lose ownership of any monitors.
     When the thread "goes to sleep", the OS lets other thread or threads get CPU time,
     so only when the sleep period is over does the thread is back to compete with other
     threads for CPU time.
*/

class Runner extends Thread
{
    @Override
    public void run ()
    {
        for (int i = 0; i < 10; i++)
        {
            System.out.println("Hello " + i + " " + System.identityHashCode(Thread.currentThread()));

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
}

public class App
{
    public static void main (String[] args)
    {
        Runner runner1 = new Runner();
        runner1.start();
        //runner1.run(); //If we were to use this code, it'll run the code of the run method
        //as part of the main thread of the application normally, but if we invoke
        //the start method, the code of the run method will be executed in its own
        //thread
        Runner runner2 = new Runner();
        runner2.start();
        /*We have 2 threads (other than the main thread) that run concurrently, which is the 
          point of threads*/

        //This piece of code has been added by me to illustrate what happens if
        //the main thread also had a piece of code that runs concurrently with the other
        //two threads
        //                        for (int i = 10; i < 20; i++)
        //                        {
        //                            System.out.println("Hello main thread " + i);
        //                        }
    }
}