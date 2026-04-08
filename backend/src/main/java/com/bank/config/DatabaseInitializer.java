package com.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DATABASE INITIALIZER STARTING ===");
        try {
            // Create users table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'customer', account_id INT)");
            log.info("Users table created");
            
            // Create accounts table  
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, balance DECIMAL(15,2) NOT NULL DEFAULT 0.00, " +
                "type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS', status VARCHAR(20) DEFAULT 'ACTIVE')");
            log.info("Accounts table created");
            
            // Create transactions table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(50) PRIMARY KEY, account_id INT, type VARCHAR(20) NOT NULL, " +
                "amount DECIMAL(15,2) NOT NULL, date VARCHAR(10) NOT NULL, time VARCHAR(10) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'completed', description TEXT)");
            log.info("Transactions table created");
            
            // Insert default users
            try {
                jdbcTemplate.execute("INSERT INTO users (first_name, last_name, email, password, role, account_id) " +
                    "SELECT 'John', 'Doe', 'john@example.com', 'password123', 'customer', 1 " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john@example.com')");
                log.info("User John Doe inserted");
            } catch (Exception e) {
                log.warn("Insert user John failed: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("INSERT INTO users (first_name, last_name, email, password, role, account_id) " +
                    "SELECT 'Jane', 'Smith', 'jane@example.com', 'password123', 'customer', 2 " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'jane@example.com')");
                log.info("User Jane Smith inserted");
            } catch (Exception e) {
                log.warn("Insert user Jane failed: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("INSERT INTO users (first_name, last_name, email, password, role, account_id) " +
                    "SELECT 'Admin', 'User', 'admin@securebank.com', 'admin123', 'admin', NULL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@securebank.com')");
                log.info("Admin user inserted");
            } catch (Exception e) {
                log.warn("Insert admin failed: {}", e.getMessage());
            }
            
            // Insert default accounts
            try {
                jdbcTemplate.execute("INSERT INTO accounts (id, name, type, balance, status) " +
                    "SELECT 1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE' " +
                    "WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 1)");
                log.info("Account 1 inserted");
            } catch (Exception e) {
                log.warn("Insert account 1 failed: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("INSERT INTO accounts (id, name, type, balance, status) " +
                    "SELECT 2, 'Jane Smith', 'CHECKING', 2500.00, 'ACTIVE' " +
                    "WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 2)");
                log.info("Account 2 inserted");
            } catch (Exception e) {
                log.warn("Insert account 2 failed: {}", e.getMessage());
            }
            
            log.info("=== DATABASE INITIALIZED SUCCESSFULLY ===");
        } catch (Exception e) {
            log.error("Database init FAILED: {}", e.getMessage(), e);
        }
    }
}