package lec11_Deadlock;

class Account
{
    private int balance = 10000;

    public void deposit (int amount)
    {
        //Equivalent to balance=(balance+amount)-->This is not a native atomic operation,
        //it consists of 3 JVM steps.
        balance += amount;
    }

    public void withdraw (int amount)
    {
        //Equivalent to balance=(balance-amount)-->This is not a native atomic operation,
        //it consists of 3 JVM steps.
        balance -= amount;
    }

    public int getBalance ()
    {
        return balance;
    }

    public static void transfer (Account acc1, Account acc2, int amount)
    {
        acc1.withdraw(amount);
        acc2.deposit(amount);
    }
}
