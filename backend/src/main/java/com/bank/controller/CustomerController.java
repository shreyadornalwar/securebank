package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final JdbcTemplate jdbcTemplate;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getCustomers() {
        try {
            List<Map<String, Object>> customers = jdbcTemplate.query(
                    "SELECT u.id, u.first_name, u.last_name, u.email, " +
                    "(SELECT COUNT(*) FROM accounts a WHERE a.id = u.account_id) as accounts " +
                    "FROM users u WHERE u.role = 'customer'",
                    (rs, rowNum) -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("id", rs.getInt("id"));
                        c.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                        c.put("email", rs.getString("email"));
                        c.put("phone", "N/A");
                        c.put("status", "ACTIVE");
                        c.put("accounts", rs.getInt("accounts"));
                        return c;
                    });
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.List.of());
        }
    }
}
