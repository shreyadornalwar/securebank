package com.bank.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Message classes
    public static class NotificationMessage {
        private String message;

        public NotificationMessage() {}

        public NotificationMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class BalanceUpdate {
        private String accountId;
        private double newBalance;

        public BalanceUpdate() {}

        public BalanceUpdate(String accountId, double newBalance) {
            this.accountId = accountId;
            this.newBalance = newBalance;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public double getNewBalance() {
            return newBalance;
        }

        public void setNewBalance(double newBalance) {
            this.newBalance = newBalance;
        }
    }

    public static class SecurityAlert {
        private String alertType;
        private String description;

        public SecurityAlert() {}

        public SecurityAlert(String alertType, String description) {
            this.alertType = alertType;
            this.description = description;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
