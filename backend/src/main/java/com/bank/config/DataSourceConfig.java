package com.bank.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/bankdb}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = jdbcUrl;
        
        // If URL doesn't start with jdbc:, add the prefix
        // But also check for "postgresql://" and convert to full JDBC URL
        if (!url.startsWith("jdbc:")) {
            if (url.startsWith("postgresql://")) {
                // Convert postgresql://host:port/db to jdbc:postgresql://host:port/db
                url = "jdbc:" + url;
            } else {
                // Just add jdbc: prefix
                url = "jdbc:" + url;
            }
        }
        
        System.out.println(">>> Using JDBC URL: " + url);
        
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }
}
