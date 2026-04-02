package com.bank.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bank.model.Transaction;

public class FileLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/transactions.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logTransaction(Transaction txn) {
        try {
            new File(LOG_DIR).mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                String timestamp = LocalDateTime.now().format(FORMATTER);
                pw.printf("%-20s | %-16s | Account: ACC%03d | %-12s | Amount: %10.2f | Status: %-10s | %s%n",
                        timestamp,
                        txn.getId(),
                        txn.getAccountId(),
                        txn.getType(),
                        txn.getAmount(),
                        txn.getStatus(),
                        txn.getDescription());
            }
        } catch (IOException e) {
            System.err.println("[FileLogger] Failed to write transaction log: " + e.getMessage());
        }
    }

    public static void logSummary(String line) {
        try {
            new File(LOG_DIR).mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                String timestamp = LocalDateTime.now().format(FORMATTER);
                pw.printf("%-20s | [REPORT] %s%n", timestamp, line);
            }
        } catch (IOException e) {
            System.err.println("[FileLogger] Failed to write summary log: " + e.getMessage());
        }
    }
}
