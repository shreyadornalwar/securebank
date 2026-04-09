package com.bank;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Main {

    public static void main(String[] args) {
        String mailUser = System.getenv("MAIL_USERNAME");
        String mailPass = System.getenv("MAIL_PASSWORD");
        if (mailUser != null && !mailUser.isBlank()) {
            System.setProperty("MAIL_USERNAME", mailUser);
        }
        if (mailPass != null && !mailPass.isBlank()) {
            System.setProperty("MAIL_PASSWORD", mailPass);
        }
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        executor.initialize();
        return executor;
    }
}
