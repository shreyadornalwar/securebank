package com.bank.config;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() throws URISyntaxException {
        String url = datasourceUrl;
        
        System.out.println(">>> Raw datasource URL: " + url);
        
        // If URL is empty or doesn't start with jdbc:, fail over to local
        if (url == null || url.isBlank() || !url.startsWith("jdbc:")) {
            url = "jdbc:postgresql://localhost:5432/bankdb";
            username = "postgres";
            password = "password";
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
