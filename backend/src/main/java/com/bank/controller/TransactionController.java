package com.bank.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.model.Transaction;
import com.bank.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<?> listTransactions(@RequestParam(required = false) Integer accountId) {
        try {
            log.info("Fetching transactions, accountId: {}", accountId);
            List<Transaction> tx;
            if (accountId != null) {
                tx = transactionService.getTransactions(accountId);
            } else {
                tx = transactionService.getAllTransactions();
            }
            log.info("Found {} transactions", tx.size());
            return ResponseEntity.ok().body(Map.of("data", tx));
        } catch (Exception e) {
            log.error("Error fetching transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request) {
        try {
            Integer accountId = toInt(request.get("accountId"));
            double amount = toDouble(request.get("amount"));
            transactionService.deposit(accountId, amount);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> request) {
        try {
            Integer accountId = toInt(request.get("accountId"));
            double amount = toDouble(request.get("amount"));
            transactionService.withdraw(accountId, amount);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> request) {
        try {
            Integer fromId = toInt(request.get("accountId"));
            Integer toId = toInt(request.get("recipientId"));
            double amount = toDouble(request.get("amount"));
            transactionService.transfer(fromId, toId, amount);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/report/{accountId}")
    public ResponseEntity<?> getAccountReport(@PathVariable int accountId) {
        try {
            Map<String, Object> report = transactionService.generateAccountReport(accountId);
            return ResponseEntity.ok(Map.of("success", true, "report", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/report")
    public ResponseEntity<?> getBankSummaryReport() {
        try {
            Map<String, Object> report = transactionService.generateBankSummaryReport();
            return ResponseEntity.ok(Map.of("success", true, "report", report));
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
