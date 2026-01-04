package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntegrationStateTests {
    
    private AccountController controller;
    private TransactionProcessor processor;
    
    @BeforeEach
    void setUp() {
        controller = new AccountController();
        processor = new TransactionProcessor();
    }
    
    // ========== INTEGRATION STATE FLOWS ==========
    
    @Test
    void integration_fullCustomerLifecycle() {
        // 1. Customer opens account (UNVERIFIED)
        Account customer = new Account("CUST001", "John Doe", 0.0, AccountStatus.UNVERIFIED);
        
        // UNVERIFIED: Can deposit but not withdraw
        assertTrue(controller.deposit(customer, 500.0), "Should deposit in UNVERIFIED");
        assertEquals(500.0, customer.getBalance(), 0.01);
        assertFalse(controller.withdraw(customer, 100.0), "Should not withdraw in UNVERIFIED");
        
        // 2. Admin verifies account
        customer.setStatus(AccountStatus.VERIFIED);
        
        // VERIFIED: Full access
        assertTrue(controller.deposit(customer, 200.0), "Should deposit in VERIFIED");
        assertEquals(700.0, customer.getBalance(), 0.01);
        assertTrue(controller.withdraw(customer, 300.0), "Should withdraw in VERIFIED");
        assertEquals(400.0, customer.getBalance(), 0.01);
        
        // 3. Suspicious activity → SUSPENDED
        customer.setStatus(AccountStatus.SUSPENDED);
        
        // SUSPENDED: Limited access
        assertTrue(controller.deposit(customer, 100.0), "Should deposit in SUSPENDED");
        assertEquals(500.0, customer.getBalance(), 0.01);
        assertFalse(controller.withdraw(customer, 50.0), "Should not withdraw in SUSPENDED");
        
        // 4. Appeal approved → back to VERIFIED
        customer.setStatus(AccountStatus.VERIFIED);
        
        // Back to normal
        assertTrue(controller.withdraw(customer, 200.0), "Should withdraw after appeal");
        assertEquals(300.0, customer.getBalance(), 0.01);
        
        // 5. Account closure request → CLOSED
        customer.setStatus(AccountStatus.CLOSED);
        
        // CLOSED: No transactions
        assertFalse(controller.deposit(customer, 100.0), "Should not deposit in CLOSED");
        assertFalse(controller.withdraw(customer, 50.0), "Should not withdraw in CLOSED");
        assertEquals(300.0, customer.getBalance(), 0.01);
    }
    
    @Test
    void integration_stateBasedTransfer() {
        Account alice = new Account("ALICE", "Alice", 1000.0, AccountStatus.VERIFIED);
        Account bob = new Account("BOB", "Bob", 500.0, AccountStatus.VERIFIED);
        Account charlie = new Account("CHARLIE", "Charlie", 300.0, AccountStatus.SUSPENDED);
        
        // Normal transfer between VERIFIED accounts
        assertTrue(controller.transfer(alice, bob, 200.0));
        assertEquals(800.0, alice.getBalance(), 0.01);
        assertEquals(700.0, bob.getBalance(), 0.01);
        
        // Try transfer from SUSPENDED account (should fail)
        assertFalse(controller.transfer(charlie, alice, 100.0));
        assertEquals(300.0, charlie.getBalance(), 0.01);
        assertEquals(800.0, alice.getBalance(), 0.01);
        
        // Try transfer to SUSPENDED account (should work - receiver can be suspended)
        assertTrue(controller.transfer(alice, charlie, 50.0));
        assertEquals(750.0, alice.getBalance(), 0.01);
        assertEquals(350.0, charlie.getBalance(), 0.01);
        
        // Suspend Alice's account
        alice.setStatus(AccountStatus.SUSPENDED);
        
        // Try transfer from suspended account
        assertFalse(controller.transfer(alice, bob, 100.0));
        assertEquals(750.0, alice.getBalance(), 0.01);
        assertEquals(700.0, bob.getBalance(), 0.01);
    }
    
    @Test
    void integration_creditScoreWithState() {
        MockCreditService mockCredit = new MockCreditService();
        AccountController ctrl = new AccountController();
        CreditScoreFeature creditFeature = new CreditScoreFeature(mockCredit, ctrl);
        
        Account account = new Account("CREDIT1", "Credit User", 1000.0, AccountStatus.VERIFIED);
        
        // Test with good credit score
        mockCredit.setScore("CREDIT1", 750);
        String result = creditFeature.handleWithdrawal(account, "200");
        assertTrue(result.contains("successful"));
        assertEquals(800.0, account.getBalance(), 0.01);
        
        // Test with low credit score
        mockCredit.setScore("CREDIT1", 550);
        result = creditFeature.handleWithdrawal(account, "100");
        assertTrue(result.contains("too low") || result.contains("blocked"));
        assertEquals(800.0, account.getBalance(), 0.01); // Balance unchanged
        
        // Test when account is suspended (should fail before credit check)
        account.setStatus(AccountStatus.SUSPENDED);
        result = creditFeature.handleWithdrawal(account, "50");
        assertTrue(result.contains("only for VERIFIED"));
        assertEquals(800.0, account.getBalance(), 0.01);
    }
    
    @Test
    void integration_concurrentStateChanges() {
        // Simulate multiple state changes with transactions
        Account account = new Account("CONC1", "Concurrent", 2000.0, AccountStatus.VERIFIED);
        
        // Thread 1: Multiple deposits
        for (int i = 0; i < 5; i++) {
            assertTrue(controller.deposit(account, 100.0));
        }
        assertEquals(2500.0, account.getBalance(), 0.01);
        
        // Thread 2: State changes
        account.setStatus(AccountStatus.SUSPENDED);
        assertFalse(controller.withdraw(account, 500.0)); // Should fail
        
        // Thread 1: Try another operation (should fail due to state change)
        assertFalse(controller.withdraw(account, 200.0));
        
        // Back to VERIFIED
        account.setStatus(AccountStatus.VERIFIED);
        assertTrue(controller.withdraw(account, 1000.0));
        assertEquals(1500.0, account.getBalance(), 0.01);
    }
    
    @Test
    void integration_errorRecoveryScenarios() {
        Account account = new Account("ERR1", "Error Test", 500.0, AccountStatus.VERIFIED);
        
        // Scenario 1: Failed withdraw due to overdraft
        assertFalse(controller.withdraw(account, 600.0));
        assertEquals(500.0, account.getBalance(), 0.01); // Balance unchanged
        
        // Scenario 2: Failed deposit due to invalid amount
        assertFalse(controller.deposit(account, -50.0));
        assertEquals(500.0, account.getBalance(), 0.01);
        
        // Scenario 3: Account gets suspended mid-operation sequence
        assertTrue(controller.deposit(account, 200.0));
        assertEquals(700.0, account.getBalance(), 0.01);
        
        account.setStatus(AccountStatus.SUSPENDED);
        
        // Next operations should fail
        assertFalse(controller.withdraw(account, 100.0));
        assertFalse(controller.transfer(account, 
            new Account("TEMP", "Temp", 100.0, AccountStatus.VERIFIED), 50.0));
        
        // But deposit should still work
        assertTrue(controller.deposit(account, 50.0));
        assertEquals(750.0, account.getBalance(), 0.01);
        
        // Restore and continue
        account.setStatus(AccountStatus.VERIFIED);
        assertTrue(controller.withdraw(account, 250.0));
        assertEquals(500.0, account.getBalance(), 0.01);
    }
    
    // ========== HELPER MOCK CLASS ==========
    
    class MockCreditService implements CreditService {
        private final java.util.Map<String, Integer> scores = new java.util.HashMap<>();
        
        public void setScore(String clientId, int score) {
            scores.put(clientId, score);
        }
        
        @Override
        public int getCreditScore(String clientId) {
            return scores.getOrDefault(clientId, 0);
        }
    }
}