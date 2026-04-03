package com.bank.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection
            jdbcTemplate.execute("SELECT 1");
            response.put("status", "UP");
            response.put("database", "connected");
            response.put("message", "Backend is healthy");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            response.put("message", "Database connection failed");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/db")
    public ResponseEntity<?> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection and query
            String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
            response.put("status", "UP");
            response.put("database", "connected");
            response.put("version", dbVersion);
            
            // Check if tables exist
            int userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            response.put("users_count", userCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}