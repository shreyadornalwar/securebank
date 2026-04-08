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
            // Initialize schema
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'customer', account_id INTEGER)");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, balance DECIMAL(15,2) NOT NULL DEFAULT 0.00, " +
                "type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS', status VARCHAR(20) DEFAULT 'ACTIVE')");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(50) PRIMARY KEY, account_id INTEGER, type VARCHAR(20) NOT NULL, " +
                "amount DECIMAL(15,2) NOT NULL, date VARCHAR(10) NOT NULL, time VARCHAR(10) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'completed', description TEXT)");
            
            // Insert default data if not exists
            try {
                jdbcTemplate.execute("MERGE INTO users (first_name, last_name, email, password, role, account_id) " +
                    "KEY(email) VALUES ('John', 'Doe', 'john@example.com', 'password123', 'customer', 1)");
            } catch (Exception e) {}
            
            try {
                jdbcTemplate.execute("MERGE INTO accounts (id, name, type, balance, status) " +
                    "KEY(id) VALUES (1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE')");
            } catch (Exception e) {}
            
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            System.err.println("Database init error: " + e.getMessage());
        }
    }
}