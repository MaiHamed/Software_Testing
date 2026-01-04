package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class StateTransitionTests {
    
    // ========== STATE-BASED DEPOSIT TESTS ==========
    
    @Test
    void deposit_whenClosed_shouldFail() {
        Account acc = new Account("C1", "Test", 100.0, AccountStatus.CLOSED);
        assertFalse(acc.deposit(50), "Deposit should fail for CLOSED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_whenSuspended_shouldPass() {
        Account acc = new Account("C2", "Test", 100.0, AccountStatus.SUSPENDED);
        assertTrue(acc.deposit(50), "Deposit should work for SUSPENDED account");
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_whenUnverified_shouldPass() {
        Account acc = new Account("C3", "Test", 100.0, AccountStatus.UNVERIFIED);
        assertTrue(acc.deposit(50), "Deposit should work for UNVERIFIED account");
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_whenVerified_shouldPass() {
        Account acc = new Account("C4", "Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.deposit(50), "Deposit should work for VERIFIED account");
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    // ========== STATE-BASED WITHDRAW TESTS ==========
    
    @Test
    void withdraw_whenClosed_shouldFail() {
        Account acc = new Account("C5", "Test", 100.0, AccountStatus.CLOSED);
        assertFalse(acc.withdraw(50), "Withdraw should fail for CLOSED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_whenSuspended_shouldFail() {
        Account acc = new Account("C6", "Test", 100.0, AccountStatus.SUSPENDED);
        assertFalse(acc.withdraw(50), "Withdraw should fail for SUSPENDED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_whenUnverified_shouldFail() {
        Account acc = new Account("C7", "Test", 100.0, AccountStatus.UNVERIFIED);
        assertFalse(acc.withdraw(50), "Withdraw should fail for UNVERIFIED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_whenVerified_shouldPass() {
        Account acc = new Account("C8", "Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(50), "Withdraw should work for VERIFIED account");
        assertEquals(50.0, acc.getBalance(), 0.01);
    }
    
    // ========== STATE-BASED TRANSFER TESTS ==========
    
    @Test
    void transfer_whenClosed_shouldFail() {
        Account acc = new Account("C9", "Test", 100.0, AccountStatus.CLOSED);
        assertFalse(acc.transfer(50), "Transfer should fail for CLOSED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void transfer_whenSuspended_shouldFail() {
        Account acc = new Account("C10", "Test", 100.0, AccountStatus.SUSPENDED);
        assertFalse(acc.transfer(50), "Transfer should fail for SUSPENDED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void transfer_whenUnverified_shouldFail() {
        Account acc = new Account("C11", "Test", 100.0, AccountStatus.UNVERIFIED);
        assertFalse(acc.transfer(50), "Transfer should fail for UNVERIFIED account");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // ========== STATE TRANSITION WORKFLOW TESTS ==========
    
    @Test
    void fullStateTransitionWorkflow() {
        // Start: UNVERIFIED
        Account acc = new Account("WF1", "Workflow Test", 200.0, AccountStatus.UNVERIFIED);
        
        // UNVERIFIED → VERIFIED (admin verification)
        acc.setStatus(AccountStatus.VERIFIED);
        assertTrue(acc.deposit(100), "Should deposit after verification");
        assertEquals(300.0, acc.getBalance(), 0.01);
        
        // VERIFIED → SUSPENDED (violation)
        acc.setStatus(AccountStatus.SUSPENDED);
        assertFalse(acc.withdraw(50), "Should not withdraw when suspended");
        assertTrue(acc.deposit(50), "Should still deposit when suspended");
        assertEquals(350.0, acc.getBalance(), 0.01);
        
        // SUSPENDED → VERIFIED (appeal approved)
        acc.setStatus(AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(100), "Should withdraw after appeal");
        assertEquals(250.0, acc.getBalance(), 0.01);
        
        // VERIFIED → CLOSED (admin closure)
        acc.setStatus(AccountStatus.CLOSED);
        assertFalse(acc.deposit(50), "Should not deposit when closed");
        assertFalse(acc.withdraw(50), "Should not withdraw when closed");
        assertEquals(250.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void appealFromSuspended_toVerified_shouldEnableTransactions() {
        Account acc = new Account("AP1", "Appeal Test", 150.0, AccountStatus.SUSPENDED);
        
        // Before appeal - suspended
        assertFalse(acc.withdraw(50), "Should fail when suspended");
        assertFalse(acc.transfer(50), "Should fail when suspended");
        
        // Appeal approved
        acc.setStatus(AccountStatus.VERIFIED);
        
        // After appeal - verified
        assertTrue(acc.withdraw(50), "Should work after appeal");
        assertTrue(acc.transfer(30), "Should work after appeal");
        assertEquals(70.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void illegalTransitionAttempts_shouldBeHandled() {
        Account acc = new Account("IL1", "Illegal Test", 100.0, AccountStatus.VERIFIED);
        
        // Try illegal operation (CLOSED state operations)
        acc.setStatus(AccountStatus.CLOSED);
        
        // All operations should fail in CLOSED state
        assertFalse(acc.deposit(10), "Deposit should fail in CLOSED");
        assertFalse(acc.withdraw(10), "Withdraw should fail in CLOSED");
        assertFalse(acc.transfer(10), "Transfer should fail in CLOSED");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // ========== PARAMETERIZED STATE TESTS ==========
    
    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"VERIFIED"})
    void deposit_allowedForAllExceptClosed(AccountStatus status) {
        Account acc = new Account("P1", "Param Test", 100.0, status);
        assertTrue(acc.deposit(50), "Deposit should work for status: " + status);
    }
    
    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"SUSPENDED", "CLOSED", "UNVERIFIED"})
    void withdraw_notAllowedForNonVerified(AccountStatus status) {
        Account acc = new Account("P2", "Param Test", 100.0, status);
        assertFalse(acc.withdraw(50), "Withdraw should fail for status: " + status);
    }
    
    @Test
    void getBalance_shouldWorkInAllStates() {
        AccountStatus[] allStates = AccountStatus.values();
        
        for (AccountStatus state : allStates) {
            Account acc = new Account("BAL" + state, "Balance Test", 200.0, state);
            assertEquals(200.0, acc.getBalance(), 0.01, 
                "getBalance should work in state: " + state);
        }
    }
    
    // ========== EDGE CASE STATE TESTS ==========
    
    @Test
    void stateTransition_preservesBalance() {
        Account acc = new Account("ST1", "State Test", 500.0, AccountStatus.VERIFIED);
        
        double initialBalance = acc.getBalance();
        
        // Go through all states
        acc.setStatus(AccountStatus.SUSPENDED);
        assertEquals(initialBalance, acc.getBalance(), 0.01, "Balance preserved in SUSPENDED");
        
        acc.setStatus(AccountStatus.VERIFIED);
        assertEquals(initialBalance, acc.getBalance(), 0.01, "Balance preserved back to VERIFIED");
        
        acc.setStatus(AccountStatus.CLOSED);
        assertEquals(initialBalance, acc.getBalance(), 0.01, "Balance preserved in CLOSED");
    }
    
    @Test
    void multipleStateTransitions_shouldWork() {
        Account acc = new Account("MST1", "Multi-State", 1000.0, AccountStatus.UNVERIFIED);
        
        // Multiple transitions
        for (int i = 0; i < 5; i++) {
            acc.setStatus(AccountStatus.VERIFIED);
            assertTrue(acc.deposit(100));
            
            acc.setStatus(AccountStatus.SUSPENDED);
            assertFalse(acc.withdraw(50));
            
            acc.setStatus(AccountStatus.VERIFIED);
            assertTrue(acc.withdraw(50));
        }
        
        assertEquals(1250.0, acc.getBalance(), 0.01);
    }
}
