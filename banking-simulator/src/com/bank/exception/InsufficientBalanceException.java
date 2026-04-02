package com.bank.exception;

public class InsufficientBalanceException extends Exception {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String accountId, double requested, double available) {
        super(String.format("Account %s: Requested %.2f but only %.2f available", accountId, requested, available));
    }
}
