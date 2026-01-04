package com.example;

public class AccountController {

    private final TransactionProcessor processor;

    public AccountController() {
        this.processor = new TransactionProcessor();
    }

    public boolean withdraw(Account account, double amount) {
        return processor.processWithdraw(account, amount);
    }

    public boolean deposit(Account account, double amount) {
        return processor.processDeposit(account, amount);
    }

    public boolean transfer(Account from, Account to, double amount) {
        return processor.processTransfer(from, to, amount);
    }
}
