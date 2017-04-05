import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class Account {
    private String number;
    private double balance;
    private ReentrantLock lock;

    public Account() {
        number = AccountNumberGenerator.generateAccountNumber();
        balance = 0;
        lock = new ReentrantLock();
    }

    public void adjustBalance(double amt) {
        balance += amt;
    }

    public String getAccountNumber() {
        return number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double amt) {
        balance = amt;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
