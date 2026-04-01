package com.bank.repository;

import java.util.List;

import com.bank.model.Account;

public interface AccountRepository {

    void create(Account account);

    List<Account> findAll();

    Account findById(int id);

    void update(Account account);

    void delete(int id);
}
