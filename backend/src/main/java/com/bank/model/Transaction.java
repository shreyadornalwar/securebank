package com.bank.model;

import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private Integer accountId;
    private String type;
    private double amount;
    private String date;
    private String time;
    private String status;
    private String description;

    public Transaction() {}

    public Transaction(String id, Integer accountId, String type, double amount, String date, String time, String status, String description) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.status = status;
        this.description = description;
    }

    public static Transaction create(Integer accountId, String type, double amount, String status, String description) {
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(
                "TXN" + System.currentTimeMillis(),
                accountId,
                type,
                amount,
                now.toLocalDate().toString(),
                now.toLocalTime().withNano(0).toString(),
                status,
                description
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
