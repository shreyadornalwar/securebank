package com.bank.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.controller.SseController;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.JdbcAccountRepository;
import com.bank.util.FileLogger;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private JdbcAccountRepository repository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SseController sseController;

    public List<Transaction> getAllTransactions() {
        log.info("TransactionService.getAllTransactions() called");
        try {
            return repository.findAllTransactions();
        } catch (Exception e) {
            log.error("Error in getAllTransactions: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<Transaction> getTransactions(int accountId) {
        log.info("TransactionService.getTransactions({}) called", accountId);
        try {
            return repository.findTransactionsByAccountId(accountId);
        } catch (Exception e) {
            log.error("Error in getTransactions: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional
    public Map<String, Object> deposit(int accountId, double amount) {
        accountService.deposit(accountId, amount);
        Transaction txn = recordTransaction(accountId, "DEPOSIT", amount);
        FileLogger.logTransaction(txn);
        Account account = repository.findById(accountId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("transaction", txn);
        
        if (account != null) {
            try {
                emailService.sendTransactionEmail(accountId, "DEPOSIT", amount, account.getBalance());
                result.put("emailSent", true);
            } catch (Exception e) {
                log.warn("Email send failed for deposit: {}", e.getMessage());
                result.put("emailSent", false);
                result.put("emailError", e.getMessage());
            }
            broadcastTransactionUpdate(txn, account);
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> withdraw(int accountId, double amount) {
        accountService.withdraw(accountId, amount);
        Transaction txn = recordTransaction(accountId, "WITHDRAWAL", -amount);
        FileLogger.logTransaction(txn);
        Account account = repository.findById(accountId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("transaction", txn);
        
        if (account != null) {
            try {
                emailService.sendTransactionEmail(accountId, "WITHDRAWAL", amount, account.getBalance());
                result.put("emailSent", true);
            } catch (Exception e) {
                log.warn("Email send failed for withdrawal: {}", e.getMessage());
                result.put("emailSent", false);
                result.put("emailError", e.getMessage());
            }
            checkAndSendLowBalanceAlert(account);
            broadcastTransactionUpdate(txn, account);
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> transfer(int fromId, int toId, double amount) {
        accountService.transfer(fromId, toId, amount);
        Transaction fromTxn = recordTransaction(fromId, "TRANSFER_OUT", -amount);
        Transaction toTxn = recordTransaction(toId, "TRANSFER_IN", amount);
        FileLogger.logTransaction(fromTxn);
        FileLogger.logTransaction(toTxn);

        Account fromAccount = repository.findById(fromId);
        Account toAccount = repository.findById(toId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("fromTransaction", fromTxn);
        result.put("toTransaction", toTxn);
        boolean emailSent = false;

        if (fromAccount != null) {
            try {
                emailService.sendTransactionEmail(fromId, "TRANSFER_OUT", amount, fromAccount.getBalance());
                emailSent = true;
            } catch (Exception e) {
                log.warn("Email send failed for transfer out: {}", e.getMessage());
            }
            checkAndSendLowBalanceAlert(fromAccount);
            broadcastTransactionUpdate(fromTxn, fromAccount);
        }
        if (toAccount != null) {
            try {
                emailService.sendTransactionEmail(toId, "TRANSFER_IN", amount, toAccount.getBalance());
                emailSent = true;
            } catch (Exception e) {
                log.warn("Email send failed for transfer in: {}", e.getMessage());
            }
            broadcastTransactionUpdate(toTxn, toAccount);
        }
        
        result.put("emailSent", emailSent);
        return result;
    }

    private Transaction recordTransaction(int accountId, String type, double amount) {
        LocalDateTime now = LocalDateTime.now();
        Transaction t = new Transaction();
        t.setId("TXN" + System.currentTimeMillis());
        t.setAccountId(accountId);
        t.setType(type);
        t.setAmount(amount);
        t.setDate(now.toLocalDate().toString());
        t.setTime(now.toLocalTime().withNano(0).toString());
        t.setStatus("COMPLETED");
        t.setDescription(type.replace("_", " "));
        repository.saveTransaction(t);
        return t;
    }

    private void checkAndSendLowBalanceAlert(Account account) {
        try {
            double threshold = emailService.getDefaultLowBalanceThreshold();
            if (account.getBalance() < threshold) {
                emailService.sendLowBalanceAlert(account.getId(), account.getBalance(), threshold);
            }
        } catch (Exception e) {
            System.err.println("Failed to check/send low balance alert: " + e.getMessage());
        }
    }

    private void broadcastTransactionUpdate(Transaction txn, Account account) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("transaction", Map.of(
                "id", txn.getId(),
                "accountId", String.format("ACC%03d", txn.getAccountId()),
                "type", txn.getType(),
                "amount", txn.getAmount(),
                "date", txn.getDate(),
                "time", txn.getTime(),
                "status", txn.getStatus(),
                "description", txn.getDescription()
            ));
            data.put("account", Map.of(
                "id", String.format("ACC%03d", account.getId()),
                "owner", account.getName(),
                "balance", account.getBalance(),
                "type", account.getType(),
                "status", account.getStatus()
            ));
            sseController.broadcastTransaction(data);
        } catch (Exception e) {
            System.err.println("Failed to broadcast transaction update: " + e.getMessage());
        }
    }

    /**
     * Generates a transaction report for a specific account.
     * Returns transaction history and a summary with totals.
     */
    public Map<String, Object> generateAccountReport(int accountId) {
        List<Transaction> transactions = repository.findTransactionsByAccountId(accountId);
        Account account = repository.findById(accountId);

        double totalDeposits = 0;
        double totalWithdrawals = 0;
        int depositCount = 0;
        int withdrawalCount = 0;
        int transferInCount = 0;
        int transferOutCount = 0;

        for (Transaction t : transactions) {
            switch (t.getType()) {
                case "DEPOSIT":
                    totalDeposits += t.getAmount();
                    depositCount++;
                    break;
                case "WITHDRAWAL":
                    totalWithdrawals += Math.abs(t.getAmount());
                    withdrawalCount++;
                    break;
                case "TRANSFER_IN":
                    totalDeposits += t.getAmount();
                    transferInCount++;
                    break;
                case "TRANSFER_OUT":
                    totalWithdrawals += Math.abs(t.getAmount());
                    transferOutCount++;
                    break;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("accountId", String.format("ACC%03d", accountId));
        summary.put("accountHolder", account != null ? account.getName() : "Unknown");
        summary.put("currentBalance", account != null ? account.getBalance() : 0);
        summary.put("totalTransactions", transactions.size());
        summary.put("depositCount", depositCount);
        summary.put("withdrawalCount", withdrawalCount);
        summary.put("transferInCount", transferInCount);
        summary.put("transferOutCount", transferOutCount);
        summary.put("totalDeposits", totalDeposits);
        summary.put("totalWithdrawals", totalWithdrawals);
        summary.put("netFlow", totalDeposits - totalWithdrawals);

        FileLogger.logSummary(String.format("Report for ACC%03d: %d transactions, deposits=%.2f, withdrawals=%.2f, net=%.2f",
                accountId, transactions.size(), totalDeposits, totalWithdrawals, totalDeposits - totalWithdrawals));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("transactions", transactions);
        return report;
    }

    /**
     * Generates a summary report across all accounts.
     */
    public Map<String, Object> generateBankSummaryReport() {
        List<Transaction> allTransactions = repository.findAllTransactions();
        List<Account> allAccounts = repository.findAll();

        double totalDeposits = 0;
        double totalWithdrawals = 0;
        int totalTransactionCount = allTransactions.size();

        for (Transaction t : allTransactions) {
            if ("DEPOSIT".equals(t.getType()) || "TRANSFER_IN".equals(t.getType())) {
                totalDeposits += t.getAmount();
            } else if ("WITHDRAWAL".equals(t.getType()) || "TRANSFER_OUT".equals(t.getType())) {
                totalWithdrawals += Math.abs(t.getAmount());
            }
        }

        double totalBalance = 0;
        for (Account a : allAccounts) {
            totalBalance += a.getBalance();
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAccounts", allAccounts.size());
        summary.put("totalTransactions", totalTransactionCount);
        summary.put("totalDeposits", totalDeposits);
        summary.put("totalWithdrawals", totalWithdrawals);
        summary.put("netFlow", totalDeposits - totalWithdrawals);
        summary.put("totalBankBalance", totalBalance);

        FileLogger.logSummary(String.format("Bank Summary: %d accounts, %d transactions, totalBalance=%.2f",
                allAccounts.size(), totalTransactionCount, totalBalance));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("recentTransactions", allTransactions.size() > 20
                ? allTransactions.subList(0, 20) : allTransactions);
        return report;
    }
}
