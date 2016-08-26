package lec1_2_CreateAndStartAThread;

/*
Starting threads
------------------------
Second way to start a thread:
Creating an instance of a Thread class (We'll call it t1) using its CTOR which accepts
a Runnable instance as an argument, and invoking the start() method on the t1 instance.
*/

class Runner implements Runnable
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
        //Every running thread is an instance of the Thread class
        Thread t1 = new Thread(new Runner());
        Thread t2 = new Thread(new Runner());
        t1.start();
        t2.start();

        //One more way to implement it (the former implementation is better because we get
        //to keep the thread's variables so we can invoke certain methods upon them):
        //                Runnable t4 = new Runner();
        //                new Thread(t4).start();
    }

}
