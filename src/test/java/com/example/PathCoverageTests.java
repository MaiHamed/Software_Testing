package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PathCoverageTests {
    
    // ========== DEPOSIT METHOD PATH COVERAGE ==========
    
    // Path 1: Closed account → false
    @Test
    void deposit_path1_closedAccount() {
        Account acc = new Account("P1", "Path Test", 100.0, AccountStatus.CLOSED);
        assertFalse(acc.deposit(50.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 2: Negative amount → false
    @Test
    void deposit_path2_negativeAmount() {
        Account acc = new Account("P2", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.deposit(-10.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 3: Zero amount → false
    @Test
    void deposit_path3_zeroAmount() {
        Account acc = new Account("P3", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.deposit(0.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 4: Valid deposit → true
    @Test
    void deposit_path4_validDeposit() {
        Account acc = new Account("P4", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.deposit(50.0));
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    // Path 5: Suspended account, positive amount → true
    @Test
    void deposit_path5_suspendedValid() {
        Account acc = new Account("P5", "Path Test", 100.0, AccountStatus.SUSPENDED);
        assertTrue(acc.deposit(50.0));
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    // ========== WITHDRAW METHOD PATH COVERAGE ==========
    
    // Path 1: Non-verified account (Suspended) → false
    @Test
    void withdraw_path1_suspendedAccount() {
        Account acc = new Account("P6", "Path Test", 100.0, AccountStatus.SUSPENDED);
        assertFalse(acc.withdraw(50.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 2: Non-verified account (Closed) → false
    @Test
    void withdraw_path2_closedAccount() {
        Account acc = new Account("P7", "Path Test", 100.0, AccountStatus.CLOSED);
        assertFalse(acc.withdraw(50.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 3: Non-verified account (Unverified) → false
    @Test
    void withdraw_path3_unverifiedAccount() {
        Account acc = new Account("P8", "Path Test", 100.0, AccountStatus.UNVERIFIED);
        assertFalse(acc.withdraw(50.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 4: Verified account, amount > balance → false
    @Test
    void withdraw_path4_overdraft() {
        Account acc = new Account("P9", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.withdraw(150.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 5: Verified account, valid amount → true
    @Test
    void withdraw_path5_validWithdraw() {
        Account acc = new Account("P10", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(50.0));
        assertEquals(50.0, acc.getBalance(), 0.01);
    }
    
    // Path 6: Verified account, amount == balance → true
    @Test
    void withdraw_path6_exactBalance() {
        Account acc = new Account("P11", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(100.0));
        assertEquals(0.0, acc.getBalance(), 0.01);
    }
    
    // Path 7: Verified account, amount <= 0 → should fail (but not covered in original)
    // Note: Original withdraw() doesn't check for negative/zero amount
    
    // ========== TRANSFER METHOD PATH COVERAGE ==========
    
    // Path 1: Non-verified account → false
    @Test
    void transfer_path1_suspendedAccount() {
        Account acc = new Account("P12", "Path Test", 100.0, AccountStatus.SUSPENDED);
        assertFalse(acc.transfer(50.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 2: Verified account, amount > balance → false
    @Test
    void transfer_path2_overdraft() {
        Account acc = new Account("P13", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.transfer(150.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    // Path 3: Verified account, valid amount → true
    @Test
    void transfer_path3_validTransfer() {
        Account acc = new Account("P14", "Path Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.transfer(50.0));
        assertEquals(50.0, acc.getBalance(), 0.01);
    }
    
    // ========== COMPOUND PATH COVERAGE ==========
    
    @Test
    void compoundPath_depositThenWithdraw() {
        Account acc = new Account("CP1", "Compound", 200.0, AccountStatus.VERIFIED);
        
        // Path: deposit valid → withdraw valid
        assertTrue(acc.deposit(100.0));  // Path 4
        assertEquals(300.0, acc.getBalance(), 0.01);
        
        assertTrue(acc.withdraw(150.0)); // Path 5
        assertEquals(150.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void compoundPath_multipleFailures() {
        Account acc = new Account("CP2", "Compound", 100.0, AccountStatus.CLOSED);
        
        // Path: closed account tries multiple operations
        assertFalse(acc.deposit(50.0));   // Path 1
        assertFalse(acc.withdraw(50.0));  // Path 2
        assertFalse(acc.transfer(50.0));  // Path 1 (for transfer)
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void compoundPath_stateTransitionAndOperations() {
        Account acc = new Account("CP3", "Compound", 500.0, AccountStatus.UNVERIFIED);
        
        // UNVERIFIED: deposit works, withdraw/transfer fail
        assertTrue(acc.deposit(100.0));   // Should work
        assertFalse(acc.withdraw(50.0));  // Should fail
        assertFalse(acc.transfer(50.0));  // Should fail
        
        // Transition to VERIFIED
        acc.setStatus(AccountStatus.VERIFIED);
        
        // VERIFIED: all operations work (if balance sufficient)
        assertTrue(acc.deposit(50.0));
        assertTrue(acc.withdraw(100.0));
        assertTrue(acc.transfer(50.0));
        
        // Transition to SUSPENDED
        acc.setStatus(AccountStatus.SUSPENDED);
        
        // SUSPENDED: deposit works, withdraw/transfer fail
        assertTrue(acc.deposit(50.0));
        assertFalse(acc.withdraw(50.0));
        assertFalse(acc.transfer(50.0));
        
        // Transition to CLOSED
        acc.setStatus(AccountStatus.CLOSED);
        
        // CLOSED: all operations fail
        assertFalse(acc.deposit(50.0));
        assertFalse(acc.withdraw(50.0));
        assertFalse(acc.transfer(50.0));
    }
    
    // ========== EDGE PATH COVERAGE ==========
    
    @Test
    void edgePath_depositMaxValue() {
        Account acc = new Account("EP1", "Edge", 100.0, AccountStatus.VERIFIED);
        
        // Test with very large value
        assertTrue(acc.deposit(Double.MAX_VALUE));
        
        // Note: Next withdraw might overflow, but that's not tested
    }
    
    @Test
    void edgePath_rapidStateChanges() {
        Account acc = new Account("EP2", "Edge", 1000.0, AccountStatus.VERIFIED);
        
        // Rapid state changes with operations
        for (int i = 0; i < 10; i++) {
            acc.setStatus(AccountStatus.VERIFIED);
            assertTrue(acc.withdraw(10.0));
            
            acc.setStatus(AccountStatus.SUSPENDED);
            assertFalse(acc.withdraw(10.0));
        }
        
        assertEquals(900.0, acc.getBalance(), 0.01);
    }
}
