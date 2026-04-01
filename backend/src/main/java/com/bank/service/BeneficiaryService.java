package com.bank.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeneficiaryService {

    private final JdbcTemplate jdbcTemplate;

    public BeneficiaryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void addBeneficiary(Long userId, String name, String accountNumber, String bankName, String ifscCode) {
        jdbcTemplate.update(
            "INSERT INTO beneficiaries (user_id, name, account_number, bank_name, ifsc_code) VALUES (?, ?, ?, ?, ?)",
            userId, name, accountNumber, bankName, ifscCode
        );
    }

    public List<BeneficiaryDTO> getBeneficiariesByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT id, user_id, name, account_number, bank_name, ifsc_code, created_at FROM beneficiaries WHERE user_id = ? ORDER BY created_at DESC",
            (rs, rowNum) -> new BeneficiaryDTO(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("account_number"),
                rs.getString("bank_name"),
                rs.getString("ifsc_code"),
                rs.getString("created_at")
            )
        );
    }

    @Transactional
    public void deleteBeneficiary(Long id, Long userId) {
        jdbcTemplate.update(
            "DELETE FROM beneficiaries WHERE id = ? AND user_id = ?",
            id, userId
        );
    }

    public static class BeneficiaryDTO {
        private final Long id;
        private final Long userId;
        private final String name;
        private final String accountNumber;
        private final String bankName;
        private final String ifscCode;
        private final String createdAt;

        public BeneficiaryDTO(Long id, Long userId, String name, String accountNumber, 
                             String bankName, String ifscCode, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.accountNumber = accountNumber;
            this.bankName = bankName;
            this.ifscCode = ifscCode;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getAccountNumber() { return accountNumber; }
        public String getBankName() { return bankName; }
        public String getIfscCode() { return ifscCode; }
        public String getCreatedAt() { return createdAt; }
    }
}