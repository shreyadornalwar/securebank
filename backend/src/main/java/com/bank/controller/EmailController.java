package com.bank.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.model.Account;
import com.bank.service.AccountService;
import com.bank.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final AccountService accountService;

    public EmailController(EmailService emailService, AccountService accountService) {
        this.emailService = emailService;
        this.accountService = accountService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEmail() {
        try {
            emailService.sendAccountCreatedEmail(999, "Test User", "shreyadornalwar@gmail.com", 1000.0);
            return ResponseEntity.ok(Map.of("success", true, "message", "Test email triggered"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/email/balance-alert
     * Sends a balance alert email for a specific account.
     * Body: { "accountId": 1 }
     */
    @PostMapping("/balance-alert")
    public ResponseEntity<?> sendBalanceAlert(@RequestBody Map<String, Object> request) {
        try {
            Integer accountId = toInt(request.get("accountId"));
            Account account = accountService.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "Account not found: " + accountId));
            }

            emailService.sendBalanceAlert(accountId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance alert email sent for account " + String.format("ACC%03d", accountId),
                    "currentBalance", account.getBalance()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/email/low-balance-check
     * Checks a specific account (or all accounts) against a threshold and sends
     * low balance alerts for those below the threshold.
     * Body: { "accountId": 1, "threshold": 500 } (accountId is optional)
     */
    @PostMapping("/low-balance-check")
    public ResponseEntity<?> checkLowBalance(@RequestBody Map<String, Object> request) {
        try {
            double threshold = request.containsKey("threshold")
                    ? toDouble(request.get("threshold"))
                    : emailService.getDefaultLowBalanceThreshold();

            Integer accountId = request.containsKey("accountId") ? toInt(request.get("accountId")) : null;
            List<Account> accounts;

            if (accountId != null) {
                Account account = accountService.getAccountById(accountId);
                if (account == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false, "message", "Account not found: " + accountId));
                }
                accounts = List.of(account);
            } else {
                accounts = accountService.listAccounts();
            }

            int alertsSent = 0;
            List<Map<String, Object>> details = new java.util.ArrayList<>();

            for (Account acc : accounts) {
                if (acc.getBalance() < threshold) {
                    emailService.sendLowBalanceAlert(acc.getId(), acc.getBalance(), threshold);
                    alertsSent++;
                    details.add(Map.of(
                            "accountId", String.format("ACC%03d", acc.getId()),
                            "balance", acc.getBalance(),
                            "threshold", threshold,
                            "shortfall", threshold - acc.getBalance(),
                            "alertSent", true));
                } else {
                    details.add(Map.of(
                            "accountId", String.format("ACC%03d", acc.getId()),
                            "balance", acc.getBalance(),
                            "threshold", threshold,
                            "alertSent", false));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "threshold", threshold,
                    "accountsChecked", accounts.size(),
                    "alertsSent", alertsSent,
                    "details", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/email/transaction-summary
     * Sends a transaction summary email for a specific account.
     * Body: { "accountId": 1 }
     */
    @PostMapping("/transaction-summary")
    public ResponseEntity<?> sendTransactionSummary(@RequestBody Map<String, Object> request) {
        try {
            Integer accountId = toInt(request.get("accountId"));
            Account account = accountService.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "Account not found: " + accountId));
            }

            emailService.sendTransactionSummary(accountId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction summary email sent for account " + String.format("ACC%03d", accountId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/email/balance/{accountId}
     * Returns current balance info and optionally triggers a balance alert.
     */
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<?> getBalance(@PathVariable int accountId,
                                        @RequestParam(defaultValue = "false") boolean sendAlert) {
        try {
            Account account = accountService.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "Account not found: " + accountId));
            }

            Map<String, Object> balanceInfo = new LinkedHashMap<>();
            balanceInfo.put("accountId", String.format("ACC%03d", accountId));
            balanceInfo.put("accountHolder", account.getName());
            balanceInfo.put("balance", account.getBalance());
            balanceInfo.put("type", account.getType());
            balanceInfo.put("status", account.getStatus());
            balanceInfo.put("belowThreshold", account.getBalance() < emailService.getDefaultLowBalanceThreshold());
            balanceInfo.put("threshold", emailService.getDefaultLowBalanceThreshold());

            if (sendAlert) {
                emailService.sendBalanceAlert(accountId);
                balanceInfo.put("alertEmailSent", true);
            }

            return ResponseEntity.ok(Map.of("success", true, "data", balanceInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/email/balance-check-all
     * Returns balance status for all accounts with low balance indicators.
     */
    @GetMapping("/balance-check-all")
    public ResponseEntity<?> getAllBalances() {
        try {
            List<Account> accounts = accountService.listAccounts();
            double threshold = emailService.getDefaultLowBalanceThreshold();

            List<Map<String, Object>> balanceList = new java.util.ArrayList<>();
            int lowBalanceCount = 0;

            for (Account acc : accounts) {
                boolean isLow = acc.getBalance() < threshold;
                if (isLow) lowBalanceCount++;
                balanceList.add(Map.of(
                        "accountId", String.format("ACC%03d", acc.getId()),
                        "accountHolder", acc.getName(),
                        "balance", acc.getBalance(),
                        "type", acc.getType(),
                        "status", acc.getStatus(),
                        "belowThreshold", isLow));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "threshold", threshold,
                    "totalAccounts", accounts.size(),
                    "lowBalanceAccounts", lowBalanceCount,
                    "accounts", balanceList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        String s = val.toString();
        if (s.startsWith("ACC")) {
            return Integer.parseInt(s.substring(3));
        }
        return Integer.parseInt(s);
    }

    private double toDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }
}
