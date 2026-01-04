package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class IntegrationTests {


    @Test
    void controllerDepositFlow_shouldUpdateBalance() {
        Account acc = new Account(
                "C1", "Ali",
                200.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        boolean result = processor.processDeposit(acc, 100);

        assertTrue(result);
        assertEquals(300.0, acc.getBalance());
    }

    @Test
    void controllerDeposit_closedAccount_shouldFail() {
        Account acc = new Account(
                "C2", "Sara",
                200.0,
                AccountStatus.CLOSED
        );

        TransactionProcessor processor = new TransactionProcessor();

        boolean result = processor.processDeposit(acc, 50);

        assertFalse(result);
        assertEquals(200.0, acc.getBalance());
    }

    @Test
    void processDeposit_nullAccount_shouldFail() {
        TransactionProcessor processor = new TransactionProcessor();
        assertFalse(processor.processDeposit(null, 100));
    }

    @Test
    void processDeposit_negativeAmount_shouldFail() {
        Account acc = new Account(
                "C3", "Omar",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processDeposit(acc, -50));
        assertEquals(100.0, acc.getBalance());
    }

    // ---------- WITHDRAW FLOW ----------

    @Test
    void processWithdraw_nullAccount_shouldFail() {
        TransactionProcessor processor = new TransactionProcessor();
        assertFalse(processor.processWithdraw(null, 50));
    }

    @Test
    void processWithdraw_suspendedAccount_shouldFail() {
        Account acc = new Account(
                "C4", "Mona",
                100.0,
                AccountStatus.SUSPENDED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processWithdraw(acc, 30));
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void processWithdraw_valid_shouldPass() {
        Account acc = new Account(
                "C5", "Youssef",
                150.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertTrue(processor.processWithdraw(acc, 50));
        assertEquals(100.0, acc.getBalance());
    }


    @Test
    void processTransfer_nullSource_shouldFail() {
        Account to = new Account(
                "C6", "Nour",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processTransfer(null, to, 50));
    }

    @Test
    void processTransfer_nullDestination_shouldFail() {
        Account from = new Account(
                "C7", "Karim",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processTransfer(from, null, 50));
    }

    @Test
    void processTransfer_negativeAmount_shouldFail() {
        Account from = new Account(
                "C8", "Salma",
                100.0,
                AccountStatus.VERIFIED
        );

        Account to = new Account(
                "C9", "Hassan",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processTransfer(from, to, -10));
        assertEquals(100.0, from.getBalance());
        assertEquals(100.0, to.getBalance());
    }

    @Test
    void processTransfer_overdraft_shouldFail() {
        Account from = new Account(
                "C10", "Aya",
                30.0,
                AccountStatus.VERIFIED
        );

        Account to = new Account(
                "C11", "Ola",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        assertFalse(processor.processTransfer(from, to, 50));
        assertEquals(30.0, from.getBalance());
        assertEquals(100.0, to.getBalance());
    }

    @Test
    void processTransfer_valid_shouldPass() {
        Account from = new Account(
                "C12", "Mostafa",
                200.0,
                AccountStatus.VERIFIED
        );

        Account to = new Account(
                "C13", "Laila",
                100.0,
                AccountStatus.VERIFIED
        );

        TransactionProcessor processor = new TransactionProcessor();

        boolean result = processor.processTransfer(from, to, 50);

        assertTrue(result);
        assertEquals(150.0, from.getBalance());
        assertEquals(150.0, to.getBalance());
    }
}
