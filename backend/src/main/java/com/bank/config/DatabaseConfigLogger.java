package com.bank.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigLogger implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        String dbUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        
        System.out.println("========================================");
        System.out.println("DATABASE CONFIGURATION:");
        System.out.println("DB URL: " + (dbUrl != null ? dbUrl : "NOT SET (using default)"));
        System.out.println("DB User: " + (dbUser != null ? dbUser : "NOT SET (using default)"));
        System.out.println("========================================");
    }
}
