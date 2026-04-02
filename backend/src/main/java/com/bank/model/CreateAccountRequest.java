package com.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAccountRequest {

    @NotNull(message = "ID is required")
    private Integer id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Balance is required")
    @Min(value = 0, message = "Balance must be non-negative")
    private Double balance;

    public CreateAccountRequest() {}

    public CreateAccountRequest(Integer id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}