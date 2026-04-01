package com.bank.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bank.model.Account;
import com.bank.model.Transaction;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Account account) {
        jdbcTemplate.update("INSERT INTO accounts (id, name, balance, type, status) VALUES (?, ?, ?, ?, ?)",
                account.getId(), account.getName(), account.getBalance(),
                account.getType() != null ? account.getType() : "SAVINGS",
                account.getStatus() != null ? account.getStatus() : "ACTIVE");
    }

    @Override
    public List<Account> findAll() {
        return jdbcTemplate.query("SELECT id, name, balance, type, status FROM accounts",
                this::mapRowToAccount);
    }

    @Override
    public Account findById(int id) {
        List<Account> results = jdbcTemplate.query(
                "SELECT id, name, balance, type, status FROM accounts WHERE id = ?",
                this::mapRowToAccount, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void update(Account account) {
        int updated = jdbcTemplate.update(
                "UPDATE accounts SET name = ?, balance = ?, type = ?, status = ? WHERE id = ?",
                account.getName(), account.getBalance(),
                account.getType(), account.getStatus(), account.getId());
        if (updated == 0) {
            throw new IllegalArgumentException("No account found with id=" + account.getId());
        }
    }

    @Override
    public void delete(int id) {
        int deleted = jdbcTemplate.update("DELETE FROM accounts WHERE id = ?", id);
        if (deleted == 0) {
            throw new IllegalArgumentException("No account found with id=" + id);
        }
    }

    public void saveTransaction(Transaction transaction) {
        jdbcTemplate.update(
                "INSERT INTO transactions (id, account_id, type, amount, date, time, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                transaction.getId(), transaction.getAccountId(), transaction.getType(),
                transaction.getAmount(), transaction.getDate(), transaction.getTime(),
                transaction.getStatus(), transaction.getDescription());
    }

    public List<Transaction> findTransactionsByAccountId(int accountId) {
        return jdbcTemplate.query(
                "SELECT id, account_id, type, amount, date, time, status, description FROM transactions WHERE account_id = ? ORDER BY date DESC, time DESC",
                this::mapRowToTransaction, accountId);
    }

    public List<Transaction> findAllTransactions() {
        return jdbcTemplate.query(
                "SELECT id, account_id, type, amount, date, time, status, description FROM transactions ORDER BY date DESC, time DESC",
                this::mapRowToTransaction);
    }

    public String findEmailByAccountId(int accountId) {
        List<String> results = jdbcTemplate.query(
                "SELECT email FROM users WHERE account_id = ?",
                (rs, rowNum) -> rs.getString("email"), accountId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void createUserForAccount(int accountId, String name, String email) {
        createUserForAccount(accountId, name, email, "changeme");
    }

    public void createUserForAccount(int accountId, String name, String email, String password) {
        if (email == null || email.isBlank()) return;

        String firstName = name;
        String lastName = "";
        if (name != null && name.contains(" ")) {
            int idx = name.lastIndexOf(" ");
            firstName = name.substring(0, idx);
            lastName = name.substring(idx + 1);
        }

        // Check if email already exists
        Integer existingId = jdbcTemplate.query(
                "SELECT id FROM users WHERE email = ?",
                (rs, rowNum) -> rs.getInt("id"), email)
                .stream().findFirst().orElse(null);

        if (existingId != null) {
            // Update existing user with new account link and password
            jdbcTemplate.update(
                    "UPDATE users SET first_name = ?, last_name = ?, password = ?, account_id = ?, role = ? WHERE id = ?",
                    firstName, lastName, password != null ? password : "changeme", accountId, "CUSTOMER", existingId);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO users (first_name, last_name, email, password, role, account_id) VALUES (?, ?, ?, ?, ?, ?)",
                    firstName, lastName, email, password != null ? password : "changeme", "CUSTOMER", accountId);
        }
    }

    private Account mapRowToAccount(ResultSet rs, int rowNum) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("balance"),
                rs.getString("type"),
                rs.getString("status"));
    }

    private Transaction mapRowToTransaction(ResultSet rs, int rowNum) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getString("id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getDouble("amount"));
        t.setDate(rs.getString("date"));
        t.setTime(rs.getString("time"));
        t.setStatus(rs.getString("status"));
        t.setDescription(rs.getString("description"));
        return t;
    }
}
