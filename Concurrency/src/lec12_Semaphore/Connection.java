package lec12_Semaphore;

import java.util.concurrent.Semaphore;

/*
public void acquire() throws InterruptedException

Acquires a permit from this semaphore, blocking until one is available, or the thread is 
interrupted. 
Acquires a permit, if one is available and returns immediately, reducing the number of 
available permits by one. 

If no permit is available then the current thread becomes disabled for thread scheduling 
purposes and lies dormant until one of two things happens: 
1. Some other thread invokes the release() method for this semaphore and the current thread is 
next to be assigned a permit; or 
2. Some other thread interrupts the current thread. 

If the current thread: 
1. has its interrupted status set on entry to this method; or 
2. is interrupted while waiting for a permit, 
then InterruptedException is thrown and the current thread's interrupted status is cleared.
Throws:InterruptedException - if the current thread is interrupted 
*/

public class Connection
{
    private static Connection instance    = new Connection();
    private Semaphore         sem         = new Semaphore(10);//Max. 10 connections at a time.
    private int               connections = 0;

    private Connection ()
    {

    }

    public static Connection getInstance ()
    {
        return instance;
    }

    public void connect ()
    {
        try
        {
            sem.acquire();//Has to acquire a permit before it can run, permits number is
                          //decreased by 1. - Look below for Javadoc for this method.
        }
        catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }
        try
        {
            //This method is allowed to be invoked because the current thread had acquired a 
            //permit. The important thing to remember is that every thread that acquired a permit
            //has to release that permit (The same importance for lock a lock and unlock that
            //same lock).
            //So, the doConnect() method will be invoked, and even if its code were to cause
            //an exception to be thrown, the release() method will still be called no matter 
            //what!!!
            doConnect();
        }
        finally
        {
            sem.release();//Has to release a permit before it ends, permits number is
            ////increased by 1.
        }
    }

    private void doConnect ()
    {
        //        try
        //        {
        //            sem.acquire();//Has to acquire a permit before it can run, permits number is
        //                          //decreased by 1.
        //        }
        //        catch (InterruptedException e1)
        //        {
        //            e1.printStackTrace();
        //        }
        synchronized (this)//synchronized block is needed because of the ++ operator
        {
            connections++;
            System.out.println("Current connections: " + connections);
        }
        try
        {
            //Sleeping in order to simulate some real work that is done
            //using the obtained connection.
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        synchronized (this)//synchronized block is needed because of the -- operator
        {
            connections--;
            //System.out.println("Current connections after --: " + connections);
        }
        //        sem.release();//Has to release a permit before it ends, permits number is
        //                      ////increased by 1.
    }//End of connect() method
}//End of class Connection