package com.bank.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootController {

    private final JdbcTemplate jdbcTemplate;

    public RootController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Banking System API");
        response.put("version", "1.0");
        response.put("endpoints", new String[]{
            "POST /api/auth/login - Authenticate user",
            "GET  /api/accounts - List all accounts",
            "POST /api/accounts - Create account",
            "GET  /api/accounts/{id} - Get account by ID",
            "PUT  /api/accounts/{id} - Update account",
            "PUT  /api/accounts/{id}/balance - Update balance",
            "DELETE /api/accounts/{id} - Delete account",
            "GET  /api/transactions - List transactions",
            "POST /api/transactions/deposit - Deposit",
            "POST /api/transactions/withdraw - Withdraw",
            "POST /api/transactions - Transfer",
            "GET  /api/customers - List customers",
            "GET  /api/staff - List staff",
            "GET  /api/events - Real-time updates (SSE)"
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("status", "UP");
            response.put("service", "Banking System");
            response.put("database", "Connected");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("service", "Banking System");
            response.put("database", "Disconnected");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}
