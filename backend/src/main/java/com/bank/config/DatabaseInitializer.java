package com.bank.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'customer', account_id INT)");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, balance DECIMAL(15,2) NOT NULL DEFAULT 0.00, " +
                "type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS', status VARCHAR(20) DEFAULT 'ACTIVE')");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(50) PRIMARY KEY, account_id INT, type VARCHAR(20) NOT NULL, " +
                "amount DECIMAL(15,2) NOT NULL, date VARCHAR(10) NOT NULL, time VARCHAR(10) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'completed', description TEXT)");
            
            try {
                jdbcTemplate.execute("INSERT INTO users (first_name, last_name, email, password, role, account_id) " +
                    "SELECT 'John', 'Doe', 'john@example.com', 'password123', 'customer', 1 " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john@example.com')");
            } catch (Exception e) {}
            
            try {
                jdbcTemplate.execute("INSERT INTO accounts (id, name, type, balance, status) " +
                    "SELECT 1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE' " +
                    "WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 1)");
            } catch (Exception e) {}
            
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            System.err.println("Database init error: " + e.getMessage());
        }
    }
}