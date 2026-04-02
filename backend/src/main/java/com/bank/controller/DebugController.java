package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    public DebugController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDb() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, email, role FROM users LIMIT 10");
            response.put("status", "success");
            response.put("users", users);
            response.put("userCount", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : "unknown");
            return ResponseEntity.status(500).body(response);
        }
    }
}
