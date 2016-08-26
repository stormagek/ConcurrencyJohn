package lec10_ReentrantLock;

public class App2
{
    public static void main (String[] args) throws InterruptedException
    {
        final Runner2 runner = new Runner2();
        Thread t1 = new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                try
                {
                    runner.firstThread();
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
                    runner.secondThread();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        runner.finished();
    }
}
