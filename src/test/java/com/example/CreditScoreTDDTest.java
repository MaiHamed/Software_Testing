package com.example;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreditScoreTDDTest {

    private CreditScoreFeature feature;
    private AccountController controller;
    private MockCreditService mockCreditService;

    @BeforeEach
    void setUp() {
        mockCreditService = new MockCreditService();
        controller = new AccountController();
        feature = new CreditScoreFeature(mockCreditService, controller);
    }

    @Test
    void transactionAllowed_highCreditScore() {
        String clientId = "client1";
        mockCreditService.setScore(clientId, 750);
        Account account = new Account(clientId, "Test User", 1000.0, AccountStatus.VERIFIED);

        String result = feature.handleWithdrawal(account, "100.0");

        assertTrue(result.toLowerCase().contains("successful"),
                "Transaction should be approved");
        assertEquals(900.0, account.getBalance(), 0.01,
                "Balance should decrease");
    }

    @Test
    void transactionAllowed_boundaryScore() {
        String clientId = "client2";
        mockCreditService.setScore(clientId, 600);
        Account account = new Account(clientId, "Test User", 1000.0, AccountStatus.VERIFIED);

        String result = feature.handleWithdrawal(account, "100.0");

        assertTrue(result.toLowerCase().contains("successful"),
                "Transaction allowed at threshold 600");
    }

    @Test
    void transactionBlocked_lowCreditScore() {
        String clientId = "client3";
        mockCreditService.setScore(clientId, 599);
        Account account = new Account(clientId, "Test User", 1000.0, AccountStatus.VERIFIED);

        String result = feature.handleWithdrawal(account, "100.0");

        assertTrue(
                result.toLowerCase().contains("too low")
                        || result.toLowerCase().contains("blocked"),
                "Message should indicate low credit score"
        );
        assertEquals(1000.0, account.getBalance(), 0.01,
                "Balance should not change");
    }

    @Test
    void withdrawalBlocked_suspendedAccount() {
        String clientId = "client_susp";
        mockCreditService.setScore(clientId, 750);

        Account account = new Account(clientId, "Test User", 1000.0, AccountStatus.SUSPENDED);

        String result = feature.handleWithdrawal(account, "50.0");

        assertTrue(result.contains("only for VERIFIED"),
                "Should fail due to suspended state");
        assertEquals(1000.0, account.getBalance(), 0.01);
    }

    @Test
    void withdrawalBlocked_unverifiedAccount() {
        String clientId = "client_unv";
        mockCreditService.setScore(clientId, 750);

        Account account = new Account(clientId, "Test User", 1000.0, AccountStatus.UNVERIFIED);

        String result = feature.handleWithdrawal(account, "50.0");

        assertTrue(result.contains("only for VERIFIED"),
                "Should fail due to unverified state");
    }

    // ---------------- MOCK ----------------

    class MockCreditService implements CreditService {

        private final Map<String, Integer> scores = new HashMap<>();

        public void setScore(String clientId, int score) {
            scores.put(clientId, score);
        }

        @Override
        public int getCreditScore(String clientId) {
            return scores.getOrDefault(clientId, 0);
        }
    }
}
