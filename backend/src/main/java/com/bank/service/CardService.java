package com.bank.service;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final JdbcTemplate jdbcTemplate;

    public CardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String applyCard(Long userId, String type, double limitAmount) {
        String cardId = "CD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cardNumber = generateCardNumber();
        String expiryDate = generateExpiryDate();

        jdbcTemplate.update(
            "INSERT INTO cards (id, user_id, number, type, expiry_date, holder_name, limit_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')",
            cardId, userId, cardNumber, type, expiryDate, getCardHolderName(userId), limitAmount
        );

        return cardId;
    }

    public List<CardDTO> getCardsByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT id, user_id, number, type, expiry_date, holder_name, limit_amount, status, created_at " +
            "FROM cards WHERE user_id = ? ORDER BY created_at DESC",
            (rs, rowNum) -> new CardDTO(
                rs.getString("id"),
                rs.getLong("user_id"),
                rs.getString("number"),
                rs.getString("type"),
                rs.getString("expiry_date"),
                rs.getString("holder_name"),
                rs.getDouble("limit_amount"),
                rs.getString("status"),
                rs.getString("created_at")
            )
        );
    }

    @Transactional
    public void updateCardStatus(String cardId, Long userId, String status) {
        jdbcTemplate.update("UPDATE cards SET status = ? WHERE id = ? AND user_id = ?", status, cardId, userId);
    }

    public List<CardDTO> getAllCards() {
        return jdbcTemplate.query(
            "SELECT c.id, c.user_id, c.number, c.type, c.expiry_date, c.holder_name, c.limit_amount, c.status, c.created_at, " +
            "u.first_name, u.last_name, u.email " +
            "FROM cards c JOIN users u ON c.user_id = u.id " +
            "ORDER BY c.created_at DESC",
            (rs, rowNum) -> {
                CardDTO card = new CardDTO(
                    rs.getString("id"),
                    rs.getLong("user_id"),
                    rs.getString("number"),
                    rs.getString("type"),
                    rs.getString("expiry_date"),
                    rs.getString("holder_name"),
                    rs.getDouble("limit_amount"),
                    rs.getString("status"),
                    rs.getString("created_at")
                );
                card.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                card.setCustomerEmail(rs.getString("email"));
                return card;
            }
        );
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append(" ");
            int num = 1000 + (int)(Math.random() * 9000);
            sb.append(num);
        }
        return sb.toString();
    }

    private String generateExpiryDate() {
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear() + 5;
        return String.format("%02d/%02d", month, year % 100);
    }

    private String getCardHolderName(Long userId) {
        List<String> names = jdbcTemplate.queryForList(
            "SELECT CONCAT(first_name, ' ', last_name) as name FROM users WHERE id = ?",
            String.class, userId
        );
        return names.isEmpty() ? "Card Holder" : names.get(0);
    }

    public static class CardDTO {
        private final String id;
        private final Long userId;
        private final String number;
        private final String type;
        private final String expiryDate;
        private final String holderName;
        private final double limitAmount;
        private final String status;
        private final String createdAt;
        private String customerName;
        private String customerEmail;

        public CardDTO(String id, Long userId, String number, String type, String expiryDate, 
                      String holderName, double limitAmount, String status, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.number = number;
            this.type = type;
            this.expiryDate = expiryDate;
            this.holderName = holderName;
            this.limitAmount = limitAmount;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public Long getUserId() { return userId; }
        public String getNumber() { return number; }
        public String getType() { return type; }
        public String getExpiryDate() { return expiryDate; }
        public String getHolderName() { return holderName; }
        public double getLimitAmount() { return limitAmount; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    }
}