package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionProcessorTest {

    private TransactionProcessor processor;
    private Account acc1;
    private Account acc2;

    @BeforeEach
    void setUp() {
        processor = new TransactionProcessor();

        // Use the new 4-parameter constructor
        acc1 = new Account("C001", "Alice", 100.0, AccountStatus.VERIFIED);
        acc2 = new Account("C002", "Bob", 50.0, AccountStatus.VERIFIED);
    }

    /* =======================
       processDeposit()
       ======================= */

    @Test
    void processDeposit_nullAccount_returnsFalse() {
        assertFalse(processor.processDeposit(null, 50));
    }

    @Test
    void processDeposit_negativeAmount_returnsFalse() {
        assertFalse(processor.processDeposit(acc1, -10));
    }

    @Test
    void processDeposit_zeroAmount_returnsFalse() {
        assertFalse(processor.processDeposit(acc1, 0));
    }

    @Test
    void processDeposit_valid_returnsTrue() {
        assertTrue(processor.processDeposit(acc1, 50));
        assertEquals(150, acc1.getBalance(), 0.001);
    }

    /* =======================
       processWithdraw()
       ======================= */

    @Test
    void processWithdraw_nullAccount_returnsFalse() {
        assertFalse(processor.processWithdraw(null, 50));
    }

    @Test
    void processWithdraw_negativeAmount_returnsFalse() {
        assertFalse(processor.processWithdraw(acc1, -10));
    }

    @Test
    void processWithdraw_zeroAmount_returnsFalse() {
        assertFalse(processor.processWithdraw(acc1, 0));
    }

    @Test
    void processWithdraw_overdraft_returnsFalse() {
        assertFalse(processor.processWithdraw(acc1, 200));
    }

    @Test
    void processWithdraw_valid_returnsTrue() {
        assertTrue(processor.processWithdraw(acc1, 50));
        assertEquals(50, acc1.getBalance(), 0.001);
    }

    @Test
    void processWithdraw_suspendedOrClosed_returnsFalse() {
        acc1.setStatus(AccountStatus.SUSPENDED);
        assertFalse(processor.processWithdraw(acc1, 10));

        acc1.setStatus(AccountStatus.CLOSED);
        assertFalse(processor.processWithdraw(acc1, 10));
    }

    /* =======================
       processTransfer()
       ======================= */

    @Test
    void processTransfer_nullFromOrTo_returnsFalse() {
        assertFalse(processor.processTransfer(null, acc2, 10));
        assertFalse(processor.processTransfer(acc1, null, 10));
    }

    @Test
    void processTransfer_negativeOrZeroAmount_returnsFalse() {
        assertFalse(processor.processTransfer(acc1, acc2, -10));
        assertFalse(processor.processTransfer(acc1, acc2, 0));
    }

    @Test
    void processTransfer_overdraft_returnsFalse() {
        assertFalse(processor.processTransfer(acc1, acc2, 200));
        // balances remain unchanged
        assertEquals(100, acc1.getBalance(), 0.001);
        assertEquals(50, acc2.getBalance(), 0.001);
    }

    @Test
    void processTransfer_valid_returnsTrue_andUpdatesBalances() {
        assertTrue(processor.processTransfer(acc1, acc2, 50));
        assertEquals(50, acc1.getBalance(), 0.001);
        assertEquals(100, acc2.getBalance(), 0.001);
    }

    @Test
    void processTransfer_suspendedOrClosed_returnsFalse() {
        acc1.setStatus(AccountStatus.SUSPENDED);
        assertFalse(processor.processTransfer(acc1, acc2, 10));

        acc1.setStatus(AccountStatus.CLOSED);
        assertFalse(processor.processTransfer(acc1, acc2, 10));
    }
}
