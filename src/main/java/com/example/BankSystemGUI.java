package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BankSystemGUI {

    public static void main(String[] args) {

        // ---------- Backend Setup ----------
        CreditService creditService = clientId -> 620; // Mock credit score
        AccountController controller = new AccountController();
        CreditScoreFeature creditFeature = new CreditScoreFeature(creditService, controller);

        // ---------- Accounts ----------
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account("1001", "Hania", 1000.0, AccountStatus.VERIFIED));
        accounts.add(new Account("1002", "Mai", 500.0, AccountStatus.VERIFIED));
        accounts.add(new Account("1003", "Maryam", 300.0, AccountStatus.SUSPENDED));
        accounts.add(new Account("1004", "Nourhan", 800.0, AccountStatus.UNVERIFIED));

        // Map for quick lookup (Transfers)
        Map<String, Account> accountMap = new HashMap<>();
        for (Account acc : accounts) {
            accountMap.put(acc.getClientId(), acc);
        }

        // Transaction History (ClientId -> Logs)
        Map<String, List<String>> historyMap = new HashMap<>();
        for (Account acc : accounts) {
            historyMap.put(acc.getClientId(), new ArrayList<>());
        }

        // Current User (index-based switching)
        final int[] currentIndex = {0};
        final Account[] currentAccount = {accounts.get(0)};

        // ---------- GUI Setup ----------
        JFrame frame = new JFrame("Bank System - Term Project");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // ---------- Top Panel ----------
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Account Details"));

        infoPanel.add(new JLabel("Client Name:"));
        JTextField nameField = new JTextField();
        nameField.setEditable(false);
        infoPanel.add(nameField);

        infoPanel.add(new JLabel("Account Number:"));
        JTextField accountNumField = new JTextField();
        accountNumField.setEditable(false);
        infoPanel.add(accountNumField);

        infoPanel.add(new JLabel("Balance:"));
        JTextField balanceField = new JTextField();
        balanceField.setEditable(false);
        infoPanel.add(balanceField);

        infoPanel.add(new JLabel("Status:"));
        JLabel statusLabel = new JLabel();
        statusLabel.setForeground(Color.BLUE);
        infoPanel.add(statusLabel);

        infoPanel.add(new JLabel("Active User:"));
        JButton switchUserButton = new JButton("Switch User");
        infoPanel.add(switchUserButton);

        frame.add(infoPanel, BorderLayout.NORTH);

        // ---------- Action Panel ----------
        JPanel actionPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        actionPanel.add(new JLabel("Amount:"));
        JTextField amountField = new JTextField();
        actionPanel.add(amountField);

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        JButton viewStatementButton = new JButton("View Statement");

        actionPanel.add(depositButton);
        actionPanel.add(withdrawButton);
        actionPanel.add(transferButton);
        actionPanel.add(viewStatementButton);

        frame.add(actionPanel, BorderLayout.CENTER);

        // ---------- Notification Panel ----------
        JTextArea notificationBox = new JTextArea(5, 40);
        notificationBox.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(notificationBox);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Notifications"));
        frame.add(scrollPane, BorderLayout.SOUTH);

        // ---------- Helper Methods ----------
        Runnable updateUI = () -> {
            Account acc = currentAccount[0];

            nameField.setText(acc.getClientName());
            accountNumField.setText(acc.getClientId());
            balanceField.setText(String.valueOf(acc.getBalance()));
            statusLabel.setText(acc.getStatus().toString());

            boolean isVerified = acc.getStatus() == AccountStatus.VERIFIED;
            boolean isSuspended = acc.getStatus() == AccountStatus.SUSPENDED;
            boolean isClosed = acc.getStatus() == AccountStatus.CLOSED;

            withdrawButton.setEnabled(isVerified);
            transferButton.setEnabled(isVerified);
            depositButton.setEnabled(!isSuspended && !isClosed);
        };

        java.util.function.BiConsumer<Account, String> logTransaction
                = (acc, msg) -> historyMap.get(acc.getClientId()).add(msg);

        updateUI.run();


        switchUserButton.addActionListener(e -> {
            currentIndex[0] = (currentIndex[0] + 1) % accounts.size();
            currentAccount[0] = accounts.get(currentIndex[0]);

            notificationBox.append(">>> Switched to user: "
                    + currentAccount[0].getClientName() + "\n");
            updateUI.run();
        });

        depositButton.addActionListener(e -> {
            Account acc = currentAccount[0];
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (controller.deposit(acc, amount)) {
                    String msg = "Deposit Successful: $" + amount;
                    notificationBox.append(msg + "\n");
                    logTransaction.accept(acc, msg);
                    updateUI.run();
                } else {
                    notificationBox.append("Deposit Failed.\n");
                }
            } catch (NumberFormatException ex) {
                notificationBox.append("Invalid amount.\n");
            }
        });

        withdrawButton.addActionListener(e -> {
            Account acc = currentAccount[0];
            String result = creditFeature.handleWithdrawal(acc, amountField.getText());
            notificationBox.append(result + "\n");
            if (result.toLowerCase().contains("successful")) {
                logTransaction.accept(acc, "Withdrawal: $" + amountField.getText());
            }
            updateUI.run();
        });

        transferButton.addActionListener(e -> {
            Account from = currentAccount[0];

            String targetId = JOptionPane.showInputDialog(
                    frame, "Enter Target Account Number (1001–1004):");

            if (targetId == null || targetId.trim().isEmpty()) {
                return;
            }

            if (targetId.equals(from.getClientId())) {
                notificationBox.append("Cannot transfer to yourself.\n");
                return;
            }

            Account to = accountMap.get(targetId);
            if (to == null) {
                notificationBox.append("Target account not found.\n");
                return;
            }

            try {
                double amount = Double.parseDouble(amountField.getText());

                // Now calling controller.transfer(from, to, amount)
                if (controller.transfer(from, to, amount)) {
                    logTransaction.accept(from,
                            "Transferred $" + amount + " to " + to.getClientName());
                    logTransaction.accept(to,
                            "Received $" + amount + " from " + from.getClientName());

                    notificationBox.append("Transfer completed to " + to.getClientName() + "\n");
                    updateUI.run();
                } else {
                    notificationBox.append("Transfer failed.\n");
                }

            } catch (NumberFormatException ex) {
                notificationBox.append("Invalid amount.\n");
            }
        });

        // Statement
        viewStatementButton.addActionListener(e -> {
            Account acc = currentAccount[0];
            notificationBox.append("\n--- Statement ---\n");
            notificationBox.append("Name: " + acc.getClientName() + "\n");
            notificationBox.append("Acc#: " + acc.getClientId() + "\n");
            notificationBox.append("Status: " + acc.getStatus() + "\n");
            notificationBox.append("Balance: $" + acc.getBalance() + "\n");
            notificationBox.append("Transactions:\n");

            List<String> logs = historyMap.get(acc.getClientId());
            if (logs.isEmpty()) {
                notificationBox.append(" (No transactions)\n");
            } else {
                logs.forEach(log -> notificationBox.append(" - " + log + "\n"));
            }
            notificationBox.append("-----------------\n");
        });

        frame.setVisible(true);
    }
}
