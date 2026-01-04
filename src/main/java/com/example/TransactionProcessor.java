package com.example;

public class TransactionProcessor {

    public boolean processDeposit(Account acc, double amount) {
        if (acc == null) return false;
        if (amount <= 0) return false;
        return acc.deposit(amount);
    }

    public boolean processWithdraw(Account acc, double amount) {
        if (acc == null) return false;
        if (amount <= 0) return false;  
        return acc.withdraw(amount);
    }

    public boolean processTransfer(Account from, Account to, double amount) {
        if (from == null || to == null) return false;
        if (amount <= 0) return false;
        if (!from.withdraw(amount)) return false;
        to.deposit(amount);
        return true;
    }
}