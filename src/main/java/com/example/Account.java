package com.example;

public class Account {

    private String clientId;
    private String clientName;
    private double balance;
    private AccountStatus status;

    public Account(String clientId, String clientName,
                   double balance, AccountStatus status) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.balance = balance;
        this.status = status;
    }

    public boolean withdraw(double amount) {
        if (status != AccountStatus.VERIFIED) return false;
        if (amount <= 0) return false;  // ← ADD THIS LINE
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean deposit(double amount) {
        if (status == AccountStatus.CLOSED || amount <= 0) return false;
        balance += amount;
        return true;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean transfer(double amount) {
        if (status != AccountStatus.VERIFIED) return false;
        if (amount <= 0) return false;  
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientId() {
        return clientId;
    }
}