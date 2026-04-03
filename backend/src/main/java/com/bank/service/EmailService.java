package com.bank.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.JdbcAccountRepository;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final double DEFAULT_LOW_BALANCE_THRESHOLD = 500.0;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private JdbcAccountRepository repository;

    // ==================== EXISTING: Transaction email after each operation ====================

    @Async
    public void sendTransactionEmail(int accountId, String transactionType, double amount, double newBalance) {
        if (mailSender == null) {
            log.debug("[EMAIL] Mail not configured, skipping transaction email for account {}", accountId);
            return;
        }
        try {
            String email = repository.findEmailByAccountId(accountId);
            if (email == null || email.isBlank()) {
                log.warn("[EMAIL] No email found for account ID: {}", accountId);
                return;
            }

            Account account = repository.findById(accountId);
            String accountName = account != null ? account.getName() : "Customer";
            String formattedAccountId = String.format("ACC%03d", accountId);

            String subject = buildSubject(transactionType);
            String body = buildBody(accountName, formattedAccountId, transactionType, amount, newBalance);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@securebank.com");

            mailSender.send(message);
            log.info("[EMAIL] Transaction email SENT to {} for account {} [{}]", email, formattedAccountId, transactionType);
        } catch (Exception e) {
            log.error("[EMAIL] FAILED to send transaction email for account {}: {} - CHECK application.properties credentials", accountId, e.getMessage());
        }
    }

    // ==================== NEW: Account Created Email ====================

    @Async
    public void sendAccountCreatedEmail(int accountId, String name, String email, double initialBalance) {
        if (mailSender == null) {
            log.debug("[EMAIL] Mail not configured, skipping account created email");
            return;
        }
        try {
            if (email == null || email.isBlank()) {
                log.warn("[EMAIL] No email provided for new account ID: {}", accountId);
                return;
            }

            String formattedAccountId = String.format("ACC%03d", accountId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String subject = "SecureBank - Welcome! Account " + formattedAccountId + " Created";
            String body = String.format(
                    "Dear %s,\n\n" +
                    "Welcome to SecureBank! Your account has been successfully created.\n\n" +
                    "========================================\n" +
                    "ACCOUNT DETAILS\n" +
                    "========================================\n" +
                    "Account ID: %s\n" +
                    "Account Holder: %s\n" +
                    "Initial Balance: ₹%.2f\n" +
                    "Created On: %s\n\n" +
                    "You can now log in to your SecureBank account to:\n" +
                    "- Make deposits and withdrawals\n" +
                    "- Transfer funds\n" +
                    "- View transaction history\n" +
                    "- Apply for loans and cards\n\n" +
                    "If you did not create this account, please contact our support immediately.\n\n" +
                    "Regards,\n" +
                    "SecureBank Team",
                    name, formattedAccountId, name, initialBalance, timestamp
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@securebank.com");

            mailSender.send(message);
            log.info("[EMAIL] Account created email SENT to {} for account {}", email, formattedAccountId);
        } catch (Exception e) {
            log.error("[EMAIL] FAILED to send account created email to {}: {} - CHECK application.properties credentials", email, e.getMessage());
        }
    }

    // ==================== NEW: Balance Alert ====================

    @Async
    public void sendBalanceAlert(int accountId) {
        if (mailSender == null) {
            log.debug("[EMAIL] Mail not configured, skipping balance alert for account {}", accountId);
            return;
        }
        try {
            String email = repository.findEmailByAccountId(accountId);
            if (email == null || email.isBlank()) {
                log.warn("No email found for account ID: {}", accountId);
                return;
            }

            Account account = repository.findById(accountId);
            if (account == null) {
                log.warn("Account not found: {}", accountId);
                return;
            }

            String accountName = account.getName();
            String formattedAccountId = String.format("ACC%03d", accountId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String subject = "SecureBank - Balance Alert for Account " + formattedAccountId;
            String body = String.format(
                    "Dear %s,\n\n" +
                    "This is a balance alert notification for your account.\n\n" +
                    "Account ID: %s\n" +
                    "Current Balance: ₹%.2f\n" +
                    "Account Type: %s\n" +
                    "Account Status: %s\n" +
                    "Alert Generated: %s\n\n" +
                    "If you have any questions about your account balance, please contact our support team.\n\n" +
                    "Regards,\n" +
                    "SecureBank Team",
                    accountName, formattedAccountId, account.getBalance(),
                    account.getType(), account.getStatus(), timestamp
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@securebank.com");

            mailSender.send(message);
            log.info("Balance alert sent to {} for account {}", email, formattedAccountId);
        } catch (Exception e) {
            log.error("Failed to send balance alert for account ID {}: {}", accountId, e.getMessage());
        }
    }

    // ==================== NEW: Low Balance Alert ====================

    @Async
    public void sendLowBalanceAlert(int accountId, double currentBalance, double threshold) {
        if (mailSender == null) {
            log.debug("[EMAIL] Mail not configured, skipping low balance alert for account {}", accountId);
            return;
        }
        try {
            String email = repository.findEmailByAccountId(accountId);
            if (email == null || email.isBlank()) {
                log.warn("No email found for account ID: {}", accountId);
                return;
            }

            Account account = repository.findById(accountId);
            String accountName = account != null ? account.getName() : "Customer";
            String formattedAccountId = String.format("ACC%03d", accountId);

            String subject = "SecureBank - LOW BALANCE WARNING for Account " + formattedAccountId;
            String body = String.format(
                    "Dear %s,\n\n" +
                    "WARNING: Your account balance has fallen below the minimum threshold.\n\n" +
                    "Account ID: %s\n" +
                    "Current Balance: ₹%.2f\n" +
                    "Minimum Threshold: ₹%.2f\n" +
                    "Shortfall: ₹%.2f\n\n" +
                    "Please deposit funds to avoid any service interruptions.\n\n" +
                    "Regards,\n" +
                    "SecureBank Team",
                    accountName, formattedAccountId, currentBalance, threshold, threshold - currentBalance
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@securebank.com");

            mailSender.send(message);
            log.info("Low balance alert sent to {} for account {} (balance={}, threshold={})",
                    email, formattedAccountId, currentBalance, threshold);
        } catch (Exception e) {
            log.error("Failed to send low balance alert for account ID {}: {}", accountId, e.getMessage());
        }
    }

    // ==================== NEW: Transaction Summary Email ====================

    @Async
    public void sendTransactionSummary(int accountId) {
        if (mailSender == null) {
            log.debug("[EMAIL] Mail not configured, skipping transaction summary for account {}", accountId);
            return;
        }
        try {
            String email = repository.findEmailByAccountId(accountId);
            if (email == null || email.isBlank()) {
                log.warn("No email found for account ID: {}", accountId);
                return;
            }

            Account account = repository.findById(accountId);
            if (account == null) {
                log.warn("Account not found: {}", accountId);
                return;
            }

            List<Transaction> transactions = repository.findTransactionsByAccountId(accountId);
            String accountName = account.getName();
            String formattedAccountId = String.format("ACC%03d", accountId);

            double totalDeposits = 0;
            double totalWithdrawals = 0;
            int depositCount = 0;
            int withdrawalCount = 0;
            int transferCount = 0;

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
                        transferCount++;
                        break;
                    case "TRANSFER_OUT":
                        totalWithdrawals += Math.abs(t.getAmount());
                        transferCount++;
                        break;
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            StringBuilder txnList = new StringBuilder();
            int maxRows = Math.min(transactions.size(), 10);
            for (int i = 0; i < maxRows; i++) {
                Transaction t = transactions.get(i);
                txnList.append(String.format("  %s | %-12s | ₹%10.2f | %s\n",
                        t.getDate(), t.getType(), t.getAmount(), t.getStatus()));
            }
            if (transactions.size() > 10) {
                txnList.append(String.format("  ... and %d more transactions\n", transactions.size() - 10));
            }

            String subject = "SecureBank - Transaction Summary for Account " + formattedAccountId;
            String body = String.format(
                    "Dear %s,\n\n" +
                    "Here is your transaction summary report.\n\n" +
                    "========================================\n" +
                    "ACCOUNT SUMMARY\n" +
                    "========================================\n" +
                    "Account ID: %s\n" +
                    "Current Balance: ₹%.2f\n" +
                    "Total Transactions: %d\n" +
                    "Report Generated: %s\n\n" +
                    "========================================\n" +
                    "TRANSACTION BREAKDOWN\n" +
                    "========================================\n" +
                    "Deposits:    %d transactions totaling ₹%.2f\n" +
                    "Withdrawals: %d transactions totaling ₹%.2f\n" +
                    "Transfers:   %d transactions\n" +
                    "Net Flow:    ₹%.2f\n\n" +
                    "========================================\n" +
                    "RECENT TRANSACTIONS\n" +
                    "========================================\n" +
                    "%s\n" +
                    "For full details, log in to your SecureBank account.\n\n" +
                    "Regards,\n" +
                    "SecureBank Team",
                    accountName, formattedAccountId, account.getBalance(),
                    transactions.size(), timestamp,
                    depositCount, totalDeposits,
                    withdrawalCount, totalWithdrawals,
                    transferCount,
                    totalDeposits - totalWithdrawals,
                    txnList.toString()
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@securebank.com");

            mailSender.send(message);
            log.info("Transaction summary sent to {} for account {}", email, formattedAccountId);
        } catch (Exception e) {
            log.error("Failed to send transaction summary for account ID {}: {}", accountId, e.getMessage());
        }
    }

    // ==================== Helper methods ====================

    private String buildSubject(String transactionType) {
        return switch (transactionType) {
            case "DEPOSIT" -> "SecureBank - Amount Credited to Your Account";
            case "WITHDRAWAL" -> "SecureBank - Amount Debited from Your Account";
            case "TRANSFER_IN" -> "SecureBank - Amount Received in Your Account";
            case "TRANSFER_OUT" -> "SecureBank - Amount Transferred from Your Account";
            default -> "SecureBank - Transaction Notification";
        };
    }

    private String buildBody(String accountName, String accountId, String transactionType,
                             double amount, double newBalance) {
        String action = switch (transactionType) {
            case "DEPOSIT" -> "credited to";
            case "WITHDRAWAL" -> "debited from";
            case "TRANSFER_IN" -> "received in";
            case "TRANSFER_OUT" -> "transferred from";
            default -> "processed for";
        };

        return String.format(
                "Dear %s,\n\n" +
                "A transaction has been processed on your account.\n\n" +
                "Account ID: %s\n" +
                "Transaction Type: %s\n" +
                "Amount: ₹%.2f %s your account\n" +
                "Current Balance: ₹%.2f\n\n" +
                "If you did not authorize this transaction, please contact our support immediately.\n\n" +
                "Regards,\n" +
                "SecureBank Team",
                accountName, accountId, transactionType.replace("_", " "),
                Math.abs(amount), action, newBalance
        );
    }

    public double getDefaultLowBalanceThreshold() {
        return DEFAULT_LOW_BALANCE_THRESHOLD;
    }
}
