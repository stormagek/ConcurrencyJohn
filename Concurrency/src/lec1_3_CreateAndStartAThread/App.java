package lec1_3_CreateAndStartAThread;

/*
Third way to start a thread:
Sometimes, we just want to run one method in its own thread and it seems too much work 
to create a separate class (Runner class in the examples of packages demo1 and demo2),
the solution is using a technique with anonymous class.
*/
/*
Note: There is 1 more way to create and start a thread/s, which is using thread pools,
that will be discussed in a later lecture.
*/

public class App
{
    public static void main (String[] args)
    {
        Thread t1 = new Thread(new Runnable()//Using anonymous class
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
        });
        t1.start();//The code in t1 runs its own thread (the code in its run method).
    }
}
