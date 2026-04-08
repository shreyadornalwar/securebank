package com.bank.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAccountException;
import com.bank.model.Account;
import com.bank.repository.JdbcAccountRepository;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final JdbcAccountRepository repository;

    public AccountService(JdbcAccountRepository repository) {
        this.repository = repository;
    }

    public void createAccount(int id, String name, double balance, String type) {
        Account account = new Account(id, name, balance, type, "ACTIVE");
        repository.create(account);
    }

    public void createAccount(int id, String name, double balance, String type, String email) {
        Account account = new Account(id, name, balance, type, "ACTIVE");
        repository.create(account);
        repository.createUserForAccount(id, name, email);
    }

    public void createAccount(int id, String name, double balance, String type, String email, String password) {
        Account account = new Account(id, name, balance, type, "ACTIVE");
        repository.create(account);
        repository.createUserForAccount(id, name, email, password);
    }

    public List<Account> listAccounts() {
        log.info("AccountService.listAccounts() called");
        return repository.findAll();
    }

    public Account getAccountById(int id) {
        log.info("AccountService.getAccountById({}) called", id);
        return repository.findById(id);
    }

    public void updateAccount(int id, String name, double balance) {
        Account account = repository.findById(id);
        if (account == null) {
            throw new InvalidAccountException(id);
        }
        account.setName(name);
        account.setBalance(balance);
        repository.update(account);
    }

    public void updateBalance(int id, double balance) {
        Account account = repository.findById(id);
        if (account == null) {
            throw new InvalidAccountException(id);
        }
        account.setBalance(balance);
        repository.update(account);
    }

    @Transactional
    public void deposit(int accountId, double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = repository.findById(accountId);
        if (account == null)
            throw new InvalidAccountException(accountId);

        account.setBalance(account.getBalance() + amount);
        repository.update(account);
    }

    @Transactional
    public void withdraw(int accountId, double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = repository.findById(accountId);
        if (account == null)
            throw new InvalidAccountException(accountId);

        if (account.getBalance() < amount)
            throw new InsufficientBalanceException(accountId, amount, account.getBalance());

        account.setBalance(account.getBalance() - amount);
        repository.update(account);
    }

    @Transactional
    public void transfer(int fromId, int toId, double amount) {
        if (fromId == toId)
            throw new IllegalArgumentException("Cannot transfer to same account");

        withdraw(fromId, amount);
        deposit(toId, amount);
    }

    public void deleteAccount(int id) {
        repository.delete(id);
    }

    public Account findById(int id) {
        return repository.findById(id);
    }
}
