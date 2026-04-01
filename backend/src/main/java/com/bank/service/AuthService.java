package com.bank.service;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.bank.model.User;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> authenticate(String email, String password, String role) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, first_name, last_name, email, password, role, account_id FROM users WHERE LOWER(email) = LOWER(?) AND password = ? AND LOWER(role) = LOWER(?)",
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setAccountId(rs.getInt("account_id"));
                    return u;
                }, email, password, role);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(int id) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, first_name, last_name, email, password, role, account_id FROM users WHERE id = ?",
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setAccountId(rs.getInt("account_id"));
                    return u;
                }, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User findByName(String name) {
        if (name == null) return null;
        List<User> users = jdbcTemplate.query(
                "SELECT id, first_name, last_name, email, password, role, account_id FROM users WHERE LOWER(first_name || ' ' || last_name) = LOWER(?)",
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setAccountId(rs.getInt("account_id"));
                    return u;
                }, name);
        return users.isEmpty() ? null : users.get(0);
    }
}
