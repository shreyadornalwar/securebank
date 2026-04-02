package com.bank.model;

public class Account {

    private int id;
    private String name;
    private double balance;
    private String type;
    private String status;

    public Account() {
        this.id = 0;
        this.name = "";
        this.balance = 0.0;
        this.type = "SAVINGS";
        this.status = "ACTIVE";
    }

    public Account(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = "SAVINGS";
        this.status = "ACTIVE";
    }

    public Account(int id, String name, double balance, String type, String status) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type != null ? type : "SAVINGS";
        this.status = status != null ? status : "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountId() {
        return String.format("ACC%03d", id);
    }

    public void setAccountId(String accountId) {
        if (accountId != null && accountId.startsWith("ACC")) {
            try {
                this.id = Integer.parseInt(accountId.substring(3));
            } catch (NumberFormatException e) {
                // Keep current id if parsing fails
            }
        }
    }

    public String getOwner() {
        return name;
    }

    public void setOwner(String owner) {
        this.name = owner;
    }
}
