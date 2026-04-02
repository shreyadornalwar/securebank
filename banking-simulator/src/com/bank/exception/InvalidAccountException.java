package com.bank.exception;

public class InvalidAccountException extends Exception {

    public InvalidAccountException(String message) {
        super(message);
    }

    public InvalidAccountException(String accountId) {
        super("Invalid account ID: " + accountId);
    }
}
