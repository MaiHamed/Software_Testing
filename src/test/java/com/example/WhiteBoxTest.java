package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class WhiteBoxTest {


    @Test
    void deposit_closedAccount_shouldFail() {
        Account acc = new Account(
                "C1", "Ali",
                100.0,
                AccountStatus.CLOSED
        );

        assertFalse(acc.deposit(50));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void deposit_negativeAmount_shouldFail() {
        Account acc = new Account(
                "C2", "Sara",
                100.0,
                AccountStatus.VERIFIED
        );

        assertFalse(acc.deposit(-20));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void deposit_valid_shouldPass() {
        Account acc = new Account(
                "C3", "Omar",
                100.0,
                AccountStatus.VERIFIED
        );

        assertTrue(acc.deposit(50));
        assertEquals(150.0, acc.getBalance());
    }

    // ---------- WITHDRAW TESTS ----------

    @Test
    void withdraw_nonVerifiedAccount_shouldFail() {
        Account acc = new Account(
                "C4", "Mona",
                100.0,
                AccountStatus.SUSPENDED
        );

        assertFalse(acc.withdraw(30));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void withdraw_overdraft_shouldFail() {
        Account acc = new Account(
                "C5", "Youssef",
                100.0,
                AccountStatus.VERIFIED
        );

        assertFalse(acc.withdraw(200));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void withdraw_valid_shouldPass() {
        Account acc = new Account(
                "C6", "Nour",
                100.0,
                AccountStatus.VERIFIED
        );

        assertTrue(acc.withdraw(40));
        assertEquals(60.0, acc.getBalance());
    }


    @Test
    void transfer_nonVerifiedAccount_shouldFail() {
        Account acc = new Account(
                "C7", "Hassan",
                100.0,
                AccountStatus.CLOSED
        );

        assertFalse(acc.transfer(50));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void transfer_overdraft_shouldFail() {
        Account acc = new Account(
                "C8", "Salma",
                50.0,
                AccountStatus.VERIFIED
        );

        assertFalse(acc.transfer(100));
        assertEquals(50.0, acc.getBalance());
    }

    @Test
    void transfer_valid_shouldPass() {
        Account acc = new Account(
                "C9", "Karim",
                200.0,
                AccountStatus.VERIFIED
        );

        assertTrue(acc.transfer(50));
        assertEquals(150.0, acc.getBalance());
    }
}
