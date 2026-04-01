package com.bank.service;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final JdbcTemplate jdbcTemplate;

    public LoanService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String applyLoan(Long userId, String type, double amount, int tenureMonths) {
        String loanId = "LN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double interestRate = getInterestRate(type);
        double emi = calculateEMI(amount, interestRate, tenureMonths);

        jdbcTemplate.update(
            "INSERT INTO loans (id, user_id, type, amount, interest_rate, tenure_months, emi, status, applied_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE())",
            loanId, userId, type, amount, interestRate, tenureMonths, emi, "PENDING"
        );

        // Add initial activity
        jdbcTemplate.update(
            "INSERT INTO loan_activities (loan_id, status, description, activity_date, activity_time) VALUES (?, ?, ?, CURDATE(), CURTIME())",
            loanId, "Application Submitted", "Your " + type + " application for " + amount + " has been submitted."
        );

        return loanId;
    }

    public List<LoanDTO> getLoansByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT l.id, l.user_id, l.type, l.amount, l.interest_rate, l.tenure_months, l.emi, l.status, l.applied_date, l.created_at " +
            "FROM loans l WHERE l.user_id = ? ORDER BY l.created_at DESC",
            (rs, rowNum) -> new LoanDTO(
                rs.getString("id"),
                rs.getLong("user_id"),
                rs.getString("type"),
                rs.getDouble("amount"),
                rs.getDouble("interest_rate"),
                rs.getInt("tenure_months"),
                rs.getDouble("emi"),
                rs.getString("status"),
                rs.getString("applied_date"),
                rs.getString("created_at")
            )
        );
    }

    public List<LoanActivityDTO> getLoanActivities(String loanId) {
        return jdbcTemplate.query(
            "SELECT id, loan_id, status, description, activity_date, activity_time, created_at " +
            "FROM loan_activities WHERE loan_id = ? ORDER BY created_at ASC",
            (rs, rowNum) -> new LoanActivityDTO(
                rs.getLong("id"),
                rs.getString("loan_id"),
                rs.getString("status"),
                rs.getString("description"),
                rs.getString("activity_date"),
                rs.getString("activity_time")
            )
        );
    }

    @Transactional
    public void updateLoanStatus(String loanId, String status, String description) {
        jdbcTemplate.update("UPDATE loans SET status = ? WHERE id = ?", status, loanId);
        
        if (description != null) {
            jdbcTemplate.update(
                "INSERT INTO loan_activities (loan_id, status, description, activity_date, activity_time) VALUES (?, ?, ?, CURDATE(), CURTIME())",
                loanId, status, description
            );
        }
    }

    public List<LoanDTO> getAllLoans() {
        return jdbcTemplate.query(
            "SELECT l.id, l.user_id, l.type, l.amount, l.interest_rate, l.tenure_months, l.emi, l.status, l.applied_date, l.created_at, " +
            "u.first_name, u.last_name, u.email " +
            "FROM loans l JOIN users u ON l.user_id = u.id " +
            "ORDER BY l.created_at DESC",
            (rs, rowNum) -> {
                LoanDTO loan = new LoanDTO(
                    rs.getString("id"),
                    rs.getLong("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getDouble("interest_rate"),
                    rs.getInt("tenure_months"),
                    rs.getDouble("emi"),
                    rs.getString("status"),
                    rs.getString("applied_date"),
                    rs.getString("created_at")
                );
                loan.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                loan.setCustomerEmail(rs.getString("email"));
                return loan;
            }
        );
    }

    private double getInterestRate(String type) {
        switch (type) {
            case "Home Loan": return 8.5;
            case "Car Loan": return 9.0;
            case "Education Loan": return 7.5;
            case "Business Loan": return 11.0;
            default: return 10.5; // Personal Loan
        }
    }

    private double calculateEMI(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 12 / 100;
        if (monthlyRate == 0) return Math.round(principal / months);
        double emi = principal * monthlyRate * Math.pow(1 + monthlyRate, months) / (Math.pow(1 + monthlyRate, months) - 1);
        return Math.round(emi);
    }

    public static class LoanDTO {
        private final String id;
        private final Long userId;
        private final String type;
        private final double amount;
        private final double interestRate;
        private final int tenureMonths;
        private final double emi;
        private final String status;
        private final String appliedDate;
        private final String createdAt;
        private String customerName;
        private String customerEmail;

        public LoanDTO(String id, Long userId, String type, double amount, double interestRate, 
                      int tenureMonths, double emi, String status, String appliedDate, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.type = type;
            this.amount = amount;
            this.interestRate = interestRate;
            this.tenureMonths = tenureMonths;
            this.emi = emi;
            this.status = status;
            this.appliedDate = appliedDate;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public Long getUserId() { return userId; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public double getInterestRate() { return interestRate; }
        public int getTenureMonths() { return tenureMonths; }
        public double getEmi() { return emi; }
        public String getStatus() { return status; }
        public String getAppliedDate() { return appliedDate; }
        public String getCreatedAt() { return createdAt; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    }

    public static class LoanActivityDTO {
        private final Long id;
        private final String loanId;
        private final String status;
        private final String description;
        private final String activityDate;
        private final String activityTime;

        public LoanActivityDTO(Long id, String loanId, String status, String description, 
                              String activityDate, String activityTime) {
            this.id = id;
            this.loanId = loanId;
            this.status = status;
            this.description = description;
            this.activityDate = activityDate;
            this.activityTime = activityTime;
        }

        public Long getId() { return id; }
        public String getLoanId() { return loanId; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public String getActivityDate() { return activityDate; }
        public String getActivityTime() { return activityTime; }
    }
}