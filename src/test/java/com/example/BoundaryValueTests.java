package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class BoundaryValueTests {
    
    // ========== DEPOSIT BOUNDARY TESTS ==========
    
    @Test
    void deposit_zeroAmount_shouldFail() {
        Account acc = new Account("B1", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.deposit(0.0), "Deposit of 0 should fail");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_negativeAmount_shouldFail() {
        Account acc = new Account("B2", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.deposit(-0.01), "Deposit of negative amount should fail");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_smallestPositive_shouldPass() {
        Account acc = new Account("B3", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.deposit(0.01), "Deposit of smallest positive amount should pass");
        assertEquals(100.01, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_largeAmount_shouldPass() {
        Account acc = new Account("B4", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        double largeAmount = 1_000_000.0;
        assertTrue(acc.deposit(largeAmount), "Deposit of large amount should pass");
        assertEquals(100.0 + largeAmount, acc.getBalance(), 0.01);
    }
    
    @Test
    void deposit_maxDouble_shouldPass() {
        Account acc = new Account("B5", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        // Note: In real system, you'd need overflow handling
        assertTrue(acc.deposit(Double.MAX_VALUE), "Deposit of MAX_VALUE should pass");
    }
    
    // ========== WITHDRAW BOUNDARY TESTS ==========
    
    @Test
    void withdraw_zeroAmount_shouldFail() {
        Account acc = new Account("B6", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.withdraw(0.0), "Withdraw of 0 should fail");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_negativeAmount_shouldFail() {
        Account acc = new Account("B7", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.withdraw(-0.01), "Withdraw of negative amount should fail");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_smallestPositive_shouldPass() {
        Account acc = new Account("B8", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(0.01), "Withdraw of smallest positive amount should pass");
        assertEquals(99.99, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_exactBalance_shouldPass() {
        Account acc = new Account("B9", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(100.0), "Withdraw of exact balance should pass");
        assertEquals(0.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_justAboveBalance_shouldFail() {
        Account acc = new Account("B10", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.withdraw(100.01), "Withdraw just above balance should fail");
        assertEquals(100.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void withdraw_justBelowBalance_shouldPass() {
        Account acc = new Account("B11", "Boundary Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.withdraw(99.99), "Withdraw just below balance should pass");
        assertEquals(0.01, acc.getBalance(), 0.01);
    }
    
    // ========== TRANSFER BOUNDARY TESTS ==========
    
    @Test
    void transfer_exactBalance_shouldPass() {
        Account from = new Account("BT1", "From", 100.0, AccountStatus.VERIFIED);
        Account to = new Account("BT2", "To", 50.0, AccountStatus.VERIFIED);
        
        assertTrue(from.transfer(100.0), "Transfer of exact balance should pass");
        assertEquals(0.0, from.getBalance(), 0.01);
    }
    
    @Test
    void transfer_justBelowBalance_shouldPass() {
        Account from = new Account("BT3", "From", 100.0, AccountStatus.VERIFIED);
        Account to = new Account("BT4", "To", 50.0, AccountStatus.VERIFIED);
        
        assertTrue(from.transfer(99.99), "Transfer just below balance should pass");
        assertEquals(0.01, from.getBalance(), 0.01);
    }
    
    @Test
    void transfer_justAboveBalance_shouldFail() {
        Account from = new Account("BT5", "From", 100.0, AccountStatus.VERIFIED);
        Account to = new Account("BT6", "To", 50.0, AccountStatus.VERIFIED);
        
        assertFalse(from.transfer(100.01), "Transfer just above balance should fail");
        assertEquals(100.0, from.getBalance(), 0.01);
    }
    
    // ========== PARAMETERIZED BOUNDARY TESTS ==========
    
    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -1.0, -0.01, 0.0})
    void deposit_invalidAmounts_shouldFail(double amount) {
        Account acc = new Account("PB1", "Param Test", 100.0, AccountStatus.VERIFIED);
        assertFalse(acc.deposit(amount), 
            "Deposit should fail for amount: " + amount);
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {0.01, 1.0, 50.0, 100.0, 1000.0})
    void deposit_validAmounts_shouldPass(double amount) {
        Account acc = new Account("PB2", "Param Test", 100.0, AccountStatus.VERIFIED);
        assertTrue(acc.deposit(amount), 
            "Deposit should pass for amount: " + amount);
    }
    
    @ParameterizedTest
    @CsvSource({
        "100.0, 50.0, true",    // Normal
        "100.0, 100.0, true",   // Exact balance
        "100.0, 99.99, true",   // Just below
        "100.0, 100.01, false", // Just above
        "100.0, 0.0, false",    // Zero
        "100.0, -10.0, false"   // Negative
    })
    void withdraw_boundaryValues(double balance, double amount, boolean expected) {
        Account acc = new Account("PB3", "Boundary Test", balance, AccountStatus.VERIFIED);
        assertEquals(expected, acc.withdraw(amount),
            String.format("Withdraw %.2f from %.2f should be %s", amount, balance, expected));
    }
    
    // ========== BALANCE PRECISION TESTS ==========
    
    @Test
    void floatingPointPrecision_deposit() {
        Account acc = new Account("FP1", "Precision Test", 100.0, AccountStatus.VERIFIED);
        
        // Multiple small deposits
        acc.deposit(0.1);
        acc.deposit(0.2);
        acc.deposit(0.3);
        
        // 100 + 0.1 + 0.2 + 0.3 = 100.6
        assertEquals(100.6, acc.getBalance(), 0.00001);
    }
    
    @Test
    void floatingPointPrecision_withdraw() {
        Account acc = new Account("FP2", "Precision Test", 100.0, AccountStatus.VERIFIED);
        
        // Multiple small withdrawals
        acc.withdraw(0.1);
        acc.withdraw(0.2);
        acc.withdraw(0.3);
        
        // 100 - 0.1 - 0.2 - 0.3 = 99.4
        assertEquals(99.4, acc.getBalance(), 0.00001);
    }
    
    // ========== EDGE CASE BOUNDARIES ==========
    
    @Test
    void veryLargeDeposit_thenWithdraw() {
        Account acc = new Account("VL1", "Large Amount", 1000.0, AccountStatus.VERIFIED);
        
        double largeDeposit = 10_000_000.0;
        assertTrue(acc.deposit(largeDeposit));
        assertEquals(1000.0 + largeDeposit, acc.getBalance(), 0.01);
        
        assertTrue(acc.withdraw(largeDeposit));
        assertEquals(1000.0, acc.getBalance(), 0.01);
    }
    
    @Test
    void sequenceOfBoundaryOperations() {
        Account acc = new Account("SEQ1", "Sequence", 500.0, AccountStatus.VERIFIED);
        
        // Test sequence of boundary operations
        assertTrue(acc.deposit(0.01));   // Smallest deposit
        assertEquals(500.01, acc.getBalance(), 0.01);
        
        assertTrue(acc.withdraw(0.01));  // Smallest withdraw
        assertEquals(500.0, acc.getBalance(), 0.01);
        
        assertFalse(acc.withdraw(500.01)); // Just over balance
        assertEquals(500.0, acc.getBalance(), 0.01);
        
        assertTrue(acc.withdraw(500.0));   // Exact balance
        assertEquals(0.0, acc.getBalance(), 0.01);
        
        assertFalse(acc.withdraw(0.01));   // Can't withdraw from zero
        assertEquals(0.0, acc.getBalance(), 0.01);
    }
}