package com.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUrlFixer implements CommandLineRunner {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    public void run(String... args) {
        if (datasourceUrl != null && !datasourceUrl.startsWith("jdbc:")) {
            System.setProperty("spring.datasource.url", "jdbc:" + datasourceUrl);
            System.out.println("Fixed JDBC URL: jdbc:" + datasourceUrl);
        }
    }
}
