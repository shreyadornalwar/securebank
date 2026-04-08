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
@RequestMapping("/api/staff")
public class StaffController {

    private final JdbcTemplate jdbcTemplate;

    public StaffController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getStaff() {
        try {
            List<Map<String, Object>> staff = jdbcTemplate.query(
                    "SELECT u.id, u.first_name || ' ' || u.last_name as name, u.email, u.role, 'Operations' as department, 'ACTIVE' as status FROM users u WHERE u.role = 'staff'",
                    (rs, rowNum) -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("id", rs.getInt("id"));
                        s.put("name", rs.getString("name"));
                        s.put("email", rs.getString("email"));
                        s.put("role", rs.getString("role"));
                        s.put("department", rs.getString("department"));
                        s.put("status", rs.getString("status"));
                        return s;
                    });
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.List.of());
        }
    }
}
