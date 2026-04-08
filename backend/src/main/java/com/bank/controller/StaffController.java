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
@RequestMapping("/api/staff")
public class StaffController {

    private static final Logger log = LoggerFactory.getLogger(StaffController.class);
    private final JdbcTemplate jdbcTemplate;

    public StaffController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getStaff() {
        try {
            log.info("Fetching staff from database");
            List<Map<String, Object>> staff = jdbcTemplate.query(
                    "SELECT id, first_name, last_name, email, role FROM users WHERE role = 'staff'",
                    (rs, rowNum) -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("id", rs.getInt("id"));
                        s.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                        s.put("email", rs.getString("email"));
                        s.put("role", rs.getString("role"));
                        s.put("department", "Operations");
                        s.put("status", "ACTIVE");
                        return s;
                    });
            log.info("Found {} staff members", staff.size());
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            log.error("Error fetching staff: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
