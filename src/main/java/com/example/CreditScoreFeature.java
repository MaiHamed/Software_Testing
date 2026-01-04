package com.example;

public class CreditScoreFeature {

    private CreditService creditService;
    private AccountController accountController;

    public CreditScoreFeature(CreditService creditService, AccountController accountController) {
        this.creditService = creditService;
        this.accountController = accountController;
    }

    /**
     * Handles withdrawal request coming from GUI
     * Applies credit score validation before calling backend logic
     *
     * @param account Account object selected in GUI
     * @param amount Withdrawal amount entered by user
     * @return Result message for notification box
     */
    public String handleWithdrawal(Account account, String amount) {

        // ---------- Input Validation ----------
        if (account == null) {
            return "Error: No account selected.";
        }

        if (amount == null || amount.isEmpty()) {
            return "Error: Amount field is empty.";
        }

        double withdrawalAmount;

        try {
            withdrawalAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return "Error: Amount must be a numeric value.";
        }

        if (withdrawalAmount <= 0) {
            return "Error: Amount must be greater than zero.";
        }

        // ---------- State Validation ----------
        if (!account.getStatus().equals(AccountStatus.VERIFIED)) {
            return "Error: Transactions allowed only for VERIFIED accounts.";
        }

        // ---------- Credit Score Check ----------
        int creditScore = creditService.getCreditScore(account.getClientId());

        if (creditScore < 600) {
            return "Transaction blocked: Credit score too low.";
        }

        // ---------- Backend Call ----------
        boolean success = accountController.withdraw(account, withdrawalAmount);

        if (success) {
            return "Withdrawal successful.";
        } else {
            return "Error: Insufficient balance.";
        }
    }
}
