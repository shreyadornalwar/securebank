package com.bank.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database initializer that runs after application startup.
 * Note: Schema and initial data are loaded via schema.sql and data.sql
 * This class is kept for any runtime initialization needs.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Schema and data are initialized via schema.sql and data.sql
        // This method can be used for any additional runtime initialization
        System.out.println("SecureBank application started successfully!");
        System.out.println("Database: MySQL");
        System.out.println("Access the application at: http://localhost:8080");
    }
}