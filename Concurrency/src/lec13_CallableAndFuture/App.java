package lec13_CallableAndFuture;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
Callable and Future (Both are interfaces)
These 2 types enable us to get returned-results from our threads, and also allow
thread code to throw exception.
*/
/*
As in lecture 12, we use ExecutorService executor = Executors.newCachedThreadPool();
Check lecture 12 for elaboration on the cached thread pool.
We're submitting one task (Runnable instance) to the pool, how can we get a returned value
from that thread code?
*/
/*
One way to do it would be to create a separate class that implements Runnable,
and the run method implementation could save the result in an instance variable of that class.
So if that new class name is:A, and the field storing the value is called b, then
we could do the following in class App (This class is defined in this file, below):
A a=new A(); executor.submit(a); 
After the thread is done and b had been assigned a new value, we'll do in the main thread:
syso(a.b);
-->An illustrative code for this, at a lower comment that starts with:
"Reference from my comments above: Code example that illustrates how we can get a returned".

Second way which is using Callable and Future which is more elegant.
One more thing we'd like to explore is how we can throw exceptions from thread code.
*/
/*
The original main method has been commented out and placed below the code at the bottom
of this file.
Rather than submitting a Runnable to the pool, he submits a Callable.
*/
/*
Callable is a parameterized interface, we'll give it Integer as a generic type, and that 
Integer type, is the type we want returned from the thread code.
The method to implement in this interface is call(), and it returns the type we've specified,
which is Integer in this case - Elaboration on this method - down below. 
Now we'll add the implementation of the Runnable interface from the original method to the
Callable interface.
Now as opposed to before, I can get the value returned from the running thread, we do it
using Future.
The code of call() method executes in one of the pool threads.
The submit method returns a Future object. 
The method submit, submits a value-returning task for execution and returns a Future 
representing the pending results of the task. The Future's get method will return the task's
result upon successful completion.(See at bottom of file Javadoc for this method).
Future is also parameterized, and the parameter has to be the same as the one we've 
specified for the Callable (Integer in our case).
We'll use a variable of type Future called future, to obtain the returned value from the
thread, using future.get() - This method throws 2 checked exceptions.
In the main thread, we invoke future.get(), without blocking the main thread
until the pool's threads have finished, and as we saw in previous lectures, we can block by
invoking executor.awaitTermination(timeout, unit) below the thread code.
If we don't invoke this method, have no fear, because invoking future.get()
(in the main method) blocks, it'll block until the thread associated with future is finished,
and when it finishes and terminates, get() stops blocking and returns.
*/
/*
Now we'll see how to throw exceptions from thread code (from the call method).
The statement  if(duration>2000) pertains that.
*/
/*
The method call() has the "throws Exception" clause in its signature, so we can throw
virtually every exception that extends the Exception class, and he chose IOException 
even though it's not relevant, just for illustration purposes.
*/
/*
An important thing to note here is that the exception might be thrown from the thread
in a time that it and the main thread run concurrently, this means that the exception
does not propagate all the way up the stack to the main method:
Every thread has its own stack, the main thread has the main method at the top of the stack,
and any other thread has their own stack with stack frame for every method it invoked, hence,
the exception is not thrown and propagate to the main method stack-frame in the main thread's
stack, rather, it propagates in the stack of the other thread (The thread that invoked
the method that had caused the exception to be thrown).
*/
/*
Just for the sake of testing, I've commented out the future.get() invocation, and at
the beginning of the call() method I did the following:
int k=0; k=k/0; This code leads to an arithmeticException being thrown, however, I didn't
see anything at the console, namely, some code caught this exception and handled it.
The call() method's signature has the "throws Exception" clause, but that has nothing to do
with this arithmeticException, hence, it's thrown by the call() method to the method that 
invoked it.
I've marked the code line k=k/0; with a break point, and started the debugger. 
Here is the status of the stack when the debugger stopped at the break point:

App (14) [Java Application] 
          lecture13_1.App at localhost:57906  
              Thread [pool-1-thread-1] (Suspended (breakpoint at line 85 in App$1))   
                  App$1.call() line: 85   
                  App$1.call() line: 1    
                  FutureTask<V>.run() line: not available 
                  ThreadPoolExecutor.runWorker(ThreadPoolExecutor$Worker) line: not available 
                  ThreadPoolExecutor$Worker.run() line: not available 
                  Thread.run() line: not available [local variables unavailable]  
              Thread [DestroyJavaVM] (Running)    
          C:\Program Files\Java\jre1.8.0_73\bin\javaw.exe (Feb 22, 2016, 11:09:36 PM)

So we can see the method invocation chain that eventually invoked the call() method, an
invocation that is done by a thread which is part of the pool,
( The chain started with Thread.run() ).
The exception is thrown because of the division by zero, and it's caught and handled by one of 
those methods shown in the method invocation chain that led to the invocation of call().

Now I've commented-in the future.get() invocation, and when duration<2000 is true, an
IOException is thrown (The call method's signature throws Exception), and it's shown in
the console like this:
java.util.concurrent.ExecutionException: java.io.IOException: Sleeping for too long.

The thrown exception will propagate to the future.get() method (If it's invoked).
One of the exceptions future.get() throws is of type ExecutionException which is
thrown if the computation threw an exception.
Indeed the computation of the task threw an exception which is IOException.
Some mechanism is in place, that handles that thrown exception in a way that alerts the
future.get() method that the computation threw an exception, and as a result it needs
to throw the relevant exception (future.get() throws 2 checked exceptions) - One of which is 
ExecutionException, that according to Javadoc, the method throws this exception if the 
computation threw an exception, and indeed the computation threw an exception, hence the
future.get() method throws an exception of type ExecutionException.
In other words, the cause of the ExecutionException exception that is thrown in the
future.get() method, is the IOException exception that is thrown in the call() method
of the thread we've created.
*/
/*
Look at the invocation of future.get() in the code, its catch block has different ways
of obtaining data from the original thrown exception (Which is IOException).
*/
/*
Future class has some useful methods like cancel, that allows us to cancel the thread - 
We'll look at threads interruption in next lecture.
The method isDone() that lets us know if the thread is finished or not.
*/
/*
What if we want to use a method of Future, but we don't want to get a returned result?
An example for such scenario could be one in which we want to throw exception or exceptions
from within the thread's code, but don't want to use a returned value from the thread's code.
It's worth mentioning that an exception can be thrown from thread's code within a pool, even
if we don't use Callable, and it may be a checked and unchecked excetptions, the only
difference being is that with the Callable interface, we can use its call() method, which
may throw exceptions, so within the call() method code, we can explicitly throw exceptions,
by using the idiom: throw new SomeKindOfException();  .

So, exceptions can be thrown from thread's codeeven by not using Callable, and using the
good old Runnable will do, and these exceptions can then be caught and used in the same way
we used here with Callable.
To illustrate this, instead of callable, we'll use the following:

Future future = executor.submit(new Runnable()
{

  @Override
  public void run ()
  {
      System.out.println("dddddd");
      System.out.println(6/0);
  }
});
executor.shutdown();
System.out.println("Result is: " + future.get());
The ArithmeticException thrown within the thread's code, is caught when future.get()
method is invoked.
We can then catch and use these exceptions in the same way we used here with Callable (As
can be seen in the code snippet above).

So, What if we want to use a method of Future, but we don't want to get a returned result?
We can do that by specifying a question mark (AKA wild card) within the angle brackets,
at the creation of the Future variable (In our case it comes instead of Integer), 
and the parameterized type for Callable will be Void.
The method will return null.
In this situation, we can use all of the Future class methods, without getting a returned
value from the thread.
*/
/* 
(Void is a AutoBoxing feature (since JDK 1.5) of void).
It's self explanatory that Void is a reference whereas void is a primitive type.
Where the requirement comes to have to use Void ?
One common usage with Generic types where we can't use primitive. 
*/
/*
<T> Future<T> submit(Callable<T> task) 
Submits a value-returning task for execution and returns a Future representing the 
pending results of the task. 
The Future's get method will return the task's result upon successful completion.
If you would like to immediately block waiting for a task, you can use constructions of the
form:
result = exec.submit(aCallable).get();
In our code example he did the same, but in
a bit longer way, he invoked the get() method on the returned future instance (Basically,
this is the same).

Note: The Executors class includes a set of methods that can convert some other common 
closure-like objects, for example, PrivilegedAction to Callable form so they can be submitted.

Type Parameters:
T - the type of the task's result
Parameters:
task - the task to submit
Returns:
a Future representing pending completion of the task
*/
/*
Reference from my comments above: Code example that illustrates how we can get a returned
value from thread's code. In other words, how we can get a value back from an instance
of a class that the JVM executes its run() method in a separate thread
The other way to do that is using Callable, as described thoroughly above!

package lecture13_1;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Fuck implements Runnable
{
    private int a = 0;

    public int getA ()
    {
        return this.a;
    }

    @Override
    public void run ()
    {
        Random random = new Random();
        int duration = random.nextInt(4000);
        System.out.println("Starting...");

        try
        {
            Thread.sleep(duration);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        a = duration;
        System.out.println("Finished...");
    }//End of run().
}

public class ASSS
{
    public static void main (String[] args)
    {
        System.out.println("In main " + System.identityHashCode(Thread.currentThread()));

        !!!Option 1!!!:
        //-------------------
        Fuck f1 = new Fuck();
        Thread t1 = new Thread(f1);
        t1.start();
        try
        {
            t1.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("a is: " + f1.getA());
        //End of Option 1:
        //-------------------

        !!!Option 2!!!:
        //-------------------
        Runnable runnable = new Fuck();
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("In main " + System.identityHashCode(Thread.currentThread()));
        executor.submit(runnable);
        executor.shutdown();//Managerial thread of the pool shutdown when all its threads
                            //are done.
        try
        {
            executor.awaitTermination(1, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("a is: " + ((Fuck) runnable).getA());
        //End of Option 2:
        //-------------------
    }
}
 */
/*
More on Callable and Future:
http://www.javaworld.com/article/2071323/using-callable-to-return-results-from-runnables.html
 */

public class App
{
    public static void main (String[] args)
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        //Submitting a value-returning task to the pool
        Future<Integer> future = executor.submit(new Callable<Integer>()
        {
            @Override
            public Integer call () throws Exception
            {
                Random random = new Random();
                int duration = random.nextInt(4000);
                //This if block pertains the exception throwing from within a thread code
                if (duration > 2000)
                {
                    System.out.println("duration is greater than 2000: " + duration);
                    throw new IOException("Sleeping for too long");
                    //throw new Throwable(); //Just for debugging
                }
                //Between  the "Starting" print and the "Finished" print we'll simulate
                //real work by sleeping.
                System.out.println("Starting...");

                try
                {
                    Thread.sleep(duration);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                System.out.println("Finished...");

                return duration;
            }//call() method
        });

        executor.shutdown();//Managerial thread of the pool shutdown when all its threads
                            //are done.

        //If we invoke future.get(), it blocks, so no need for this method invocation:
        //executor.awaitTermination(timeout, unit)

        try
        {
            System.out.println("Result is: " + future.get());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            //Prints:java.util.concurrent.ExecutionException: java.io.IOException: Sleeping for too long
            //The ExecutionException carries the message we've specified to the IOException.
            System.out.println("1:\n" + " " + e);

            //Prints java.io.IOException: Sleeping for too long
            //The ExecutionException carries the message we've specified to the IOException.
            System.out.println("2:\n" + " " + e.getMessage());

            //This is done to retrieve the original exception which is IOException.
            //Prints Sleeping for too long
            System.out.println("3:\n" + " " + e.getCause().getClass());
            IOException ex = (IOException) e.getCause();
            System.out.println(ex.getMessage());
        }
    }//End of main method
}//End of App class

/*class J extends InputStream
{  //This class has nothing to do with anything, just for testing some stuff, ignore it
    private InputStream is=new J();

    @Override
    public int read () throws IOException
    {
       System.out.println("ddddd");
        return 0;
    }    
}*/

//public static void main (String[] args)
//{
//    ExecutorService executor = Executors.newCachedThreadPool();
//    executor.submit(new Runnable() //Submitting a task to the pool
//    {
//        @Override
//        public void run ()
//        {
//            //Between  the "Starting" print and the "Finished" print we'll simulate
//            //real work by sleeping.
//            Random random = new Random();
//            int duration = random.nextInt(4000);
//            System.out.println("Starting...");
//
//            try
//            {
//                Thread.sleep(duration);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//
//            System.out.println("Finished...");
//        }
//
//    });
//    executor.shutdown();//Managerial thread of the pool shutdown when all its threads
//                        //are done.
//}
