/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class Account {
    private String number;
    private double balance;

    public Account() {
        number = AccountNumberGenerator.generateAccountNumber();
        balance = 0;
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
}
