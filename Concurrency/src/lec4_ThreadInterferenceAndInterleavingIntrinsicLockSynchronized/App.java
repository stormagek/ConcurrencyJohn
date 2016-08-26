package lec4_ThreadInterferenceAndInterleavingIntrinsicLockSynchronized;

public class App
{
    public static void main (String[] args)
    {
        new Worker().start();
    }
}
