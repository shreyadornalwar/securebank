package com.bank.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        String message = ex.getMessage();
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : "unknown";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", message != null ? message : "null",
                "cause", cause
        ));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<?> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "error", "INSUFFICIENT_BALANCE",
                "message", ex.getMessage(),
                "details", Map.of(
                        "accountId", String.format("ACC%03d", ex.getAccountId()),
                        "requested", ex.getRequested(),
                        "available", ex.getAvailable())
        ));
    }

    @ExceptionHandler(InvalidAccountException.class)
    public ResponseEntity<?> handleInvalidAccount(InvalidAccountException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "INVALID_ACCOUNT",
                "message", ex.getMessage(),
                "details", Map.of(
                        "accountId", String.format("ACC%03d", ex.getAccountId()))
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "error", "INVALID_REQUEST",
                "message", ex.getMessage()
        ));
    }
}
