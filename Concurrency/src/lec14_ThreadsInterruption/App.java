package lec14_ThreadsInterruption;

import java.util.Random;
/*
Threads Interruption and Interrupting Threads.
Arguably a flaw in the Java language, because it's a checked exception that forces us to
handle it in many occasions, but it can also be useful.
*/
/*
We'll create a new thread, its run method will do some computation-intensive code.
Look at the for-loop, if we were to use i<1E6, Finished will be printed soon after Starting.
If we were to use i<1E7 or i<1E8, then Finished will be printed after longer time, in my case
it was a matter of few seconds longer.
*/
/*
When we do in main the following:
t1.start(); Thread.sleep(500); t1.interrupt();
and run it, the same thing happens, as before ( before using t1.interrupt() ), because 
the method interrupt() doesn't kill the thread (The stop() method does that but it's 
deprecated so we should avoid using it).
*/
/*
The best way to stop/kill a thread is probably the way we've seen in lecture 2,
using a volatile boolean flag but if the semantics of volatile is not enough
(volatile guarantees memory visibility, but doesn't guarantee atomicity), 
we need to use other ways, and in any event, we still want to understand the functionality
of the interrupt() method.
*/
/*
If we were to use a method invocation within the thread code, such as sleep(), wait(),
or anything that throws an InterruptedException, we could then use the interrupt() method.
He chose to use Thread.sleep(1) and added try/catch block to handle that exception.
If a thread's (we'll call it t1) code does not contain a method invocation that throws
an InterruptedException, and within another thread, (The main thread in our example), we invoke
t1.interrupt(); then nothing happens, namely the t1 thread's interrupted flag is set, however,
no method invocation within the thread code throws this exception, so it has no impact on
the running course of the thread.
*/
/*
By running this, we can see that indeed after the t1.interrupt() method is invoked in 
the main thread, and then the other thread (t1) invokes the sleep() method, then the
InterruptedException gets thrown, and we can decide how to deal with that.
He chose to do System.out.println("We've been interrupted"); break;
*/
/*
The interrupt() method sets a flag in the thread upon which it's invoked.
Now, in the run method we'll comment out the try-catch block and create an if statement.
This flag can be inspected at any given time by a dedicated method called isInterrupted().
Then as we can see in the code within the if statement, we can check to see if the thread
is interrupted, and if it is, we can gracefully terminate the thread by using break if ending
a loop will end the thread code execution, or better yet, by using return;,
to return from the run() method and thus terminate the thread execution.
So the method isInterrupted() checks for that flag value to see if it's set or not.
*/
/*
Now we'll examine how that'd work with a thread pool rather than a thread object.
The example for thread pool will be shown in class App2, but explanations remain here.
We create a cached thread pool and submit a Callable to it, in order to retrieve a value 
from the submit method, to assign it to a Future variable -
These things are covered in previous lectures.
*/
/*
We submit a Callable to the pool, and see that the code of the Callable is 
computation-intensive, but we have an inspection of the interrupted flag using the 
isInterrupted() method, to check if the thread was interrupted.
There are 2 ways we can interrupt that thread, as following:
1. First way:
According to the code, our program will terminate after the thread finishes, the main
thread will just print Finished...    .
In the main method, we'll invoke future.cancel(boolean arg) method 
(Javadoc for this method at the end of this file).
If the specified argument is false, the method will cancel the thread if it hasn't
already started, but if it has already started it won't.
If the specified argument is true, it sets the interrupted flag, even if the thread had
already started, then using the isInterrupted() method within the thread code, we can
inspect the flag, and if it's set we gracefully terminate the thread.
We could use a boolean variable and use it as an argument for the cancel method, 
to inform thread or threads to stop running and terminate gracefully.
2. Second way:
We'll comment out the future.cancel(true) invocation.
In the main thread, after the sleep invocation, we'll do exec.shutdownNow(), which attempts
to kill all running threads, but again, it doesn't really kill them, it just sets the
interrupted flag in all running threads of the pool.
If we run this, same thing happens, the thread runs, then after some time
(in my case from few milliseconds to few seconds) the exec.shutdownNow() method is
invoked, the running thread's interrupted flag is set, and the thread terminates
gracefully.
*/

public class App
{
    public static void main (String[] args) throws InterruptedException
    {
        System.out.println("Starting...");

        //The thread will simulate computation-intensive code
        Thread t1 = new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                Random random = new Random();
                for (int i = 0; i < 1E8; i++) //1Ey=1*10^y
                {
                    //If the interrupted flag is set, then we terminate the thread 
                    //gracefully, and the main thread gets CPU time.
                    if (Thread.currentThread().isInterrupted())
                    {
                        System.out.println("Interrupted!");
                        break; //Or we could just use return; to guarantee thread termination.
                    }
                    System.out.println(i);//Never comment out this line.
                    //                    try
                    //                    {
                    //                        Thread.sleep(1);
                    //                    }
                    //                    catch (InterruptedException e)
                    //                    {
                    //                        System.out.println("We've been interrupted");
                    //                        break;
                    //                    }
                    Math.sin(random.nextDouble());
                } //End of for-loop
            }//run
        });

        t1.start();

        Thread.sleep(500);

        t1.interrupt();

        t1.join();//Causes the main thread to pause until the thread finishes

        System.out.println("Finished...");
    }//main method
}//class App
