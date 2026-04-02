package com.bank.exception;

public class InvalidAccountException extends RuntimeException {

    private final int accountId;

    public InvalidAccountException(String message) {
        super(message);
        this.accountId = 0;
    }

    public InvalidAccountException(int accountId) {
        super("Account not found: ACC" + String.format("%03d", accountId));
        this.accountId = accountId;
    }

    public int getAccountId() { return accountId; }
}
