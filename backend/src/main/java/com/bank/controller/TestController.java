package com.bank.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final JdbcTemplate jdbcTemplate;

    public TestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Application is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDb() {
        Map<String, Object> response = new HashMap<>();
        try {
            String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
            response.put("status", "ok");
            response.put("database", "connected");
            response.put("version", version);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : "unknown");
        }
        return ResponseEntity.ok(response);
    }
}
