package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.model.Account;
import com.bank.service.AccountService;
import com.bank.service.EmailService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final SseController sseController;
    private final EmailService emailService;
    private int nextId = 100;

    public AccountController(AccountService accountService, SseController sseController, EmailService emailService) {
        this.accountService = accountService;
        this.sseController = sseController;
        this.emailService = emailService;
        // Initialize nextId based on existing accounts
        try {
            List<Account> existing = accountService.listAccounts();
            for (Account a : existing) {
                if (a.getId() >= nextId) {
                    nextId = a.getId() + 1;
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody Map<String, Object> request) {
        try {
            String owner = (String) request.get("owner");
            String type = (String) request.get("type");
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            double balance = toDouble(request.get("balance"));

            if (owner == null || owner.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Owner name is required"));
            }

            int id;
            if (request.containsKey("id") && request.get("id") != null) {
                id = toInt(request.get("id"));
            } else {
                id = nextId++;
            }

            if (email != null && !email.isBlank()) {
                accountService.createAccount(id, owner, balance, type != null ? type : "SAVINGS", email, password);
                emailService.sendAccountCreatedEmail(id, owner, email, balance);
            } else {
                accountService.createAccount(id, owner, balance, type != null ? type : "SAVINGS");
            }
            
            // Broadcast account creation to all connected clients
            try {
                Map<String, Object> accountData = new HashMap<>();
                accountData.put("id", String.format("ACC%03d", id));
                accountData.put("owner", owner);
                accountData.put("type", type != null ? type : "SAVINGS");
                accountData.put("balance", balance);
                accountData.put("status", "ACTIVE");
                sseController.broadcastAccountUpdate(accountData);
            } catch (Exception e) {
                // Log but don't fail the request
                System.err.println("Failed to broadcast account creation: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Account created successfully"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) {
                msg = "An account with this email already exists";
            }
            System.err.println("Account creation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", msg));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        try {
            log.info("Fetching all accounts from database");
            List<Account> accounts = accountService.listAccounts();
            log.info("Found {} accounts", accounts.size());
            List<Map<String, Object>> result = accounts.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching accounts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable int id) {
        try {
            Account account = accountService.getAccountById(id);
            if (account != null) {
                return ResponseEntity.ok(toResponse(account));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable int id,
                                           @RequestParam String name,
                                           @RequestParam double balance) {
        try {
            accountService.updateAccount(id, name, balance);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<?> updateBalance(@PathVariable int id, @RequestParam double balance) {
        try {
            accountService.updateBalance(id, balance);
            return ResponseEntity.ok(Map.of("success", true, "message", "Balance updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable int id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(Account account) {
        Map<String, Object> map = new HashMap<>();
        // Format ID as ACC001, ACC002, etc. for frontend compatibility
        map.put("id", String.format("ACC%03d", account.getId()));
        map.put("owner", account.getName());
        map.put("type", account.getType());
        map.put("balance", account.getBalance());
        map.put("status", account.getStatus());
        map.put("email", account.getEmail());
        return map;
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
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
