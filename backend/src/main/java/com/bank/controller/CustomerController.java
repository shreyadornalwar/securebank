package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final JdbcTemplate jdbcTemplate;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getCustomers() {
        try {
            log.info("Fetching customers from database");
            List<Map<String, Object>> customers = jdbcTemplate.query(
                    "SELECT id, first_name, last_name, email, account_id FROM users WHERE role = 'customer'",
                    (rs, rowNum) -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("id", rs.getInt("id"));
                        c.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                        c.put("email", rs.getString("email"));
                        c.put("phone", "N/A");
                        c.put("status", "ACTIVE");
                        c.put("accounts", 1);
                        return c;
                    });
            log.info("Found {} customers", customers.size());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error fetching customers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
