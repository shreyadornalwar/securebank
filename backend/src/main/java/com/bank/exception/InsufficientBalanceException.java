package com.bank.exception;

public class InsufficientBalanceException extends RuntimeException {

    private final int accountId;
    private final double requested;
    private final double available;

    public InsufficientBalanceException(String message) {
        super(message);
        this.accountId = 0;
        this.requested = 0;
        this.available = 0;
    }

    public InsufficientBalanceException(int accountId, double requested, double available) {
        super(String.format("Account ACC%03d: Requested %.2f but only %.2f available",
                accountId, requested, available));
        this.accountId = accountId;
        this.requested = requested;
        this.available = available;
    }

    public int getAccountId() { return accountId; }
    public double getRequested() { return requested; }
    public double getAvailable() { return available; }
}
