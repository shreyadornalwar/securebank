package com.bank.service;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAccountException;
import com.bank.model.Account;
import com.bank.repository.JdbcAccountRepository;

public class AccountServiceTest {

    private AccountService accountService;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        String uniqueDbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + uniqueDbName + ";DB_CLOSE_DELAY=-1";

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "id INT PRIMARY KEY, name VARCHAR(100), balance DOUBLE, " +
                "type VARCHAR(20) DEFAULT 'SAVINGS', status VARCHAR(20) DEFAULT 'ACTIVE')");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(50) PRIMARY KEY, account_id INT, type VARCHAR(50), " +
                "amount DOUBLE, date VARCHAR(50), time VARCHAR(50), " +
                "status VARCHAR(50), description VARCHAR(200))");

        JdbcAccountRepository repository = new JdbcAccountRepository(jdbcTemplate);
        accountService = new AccountService(repository);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS transactions");
        jdbcTemplate.execute("DROP TABLE IF EXISTS accounts");
    }

    @Test
    void createAndFetchAccount() {
        accountService.createAccount(1, "Alice", 100.0, "SAVINGS");

        Account account = accountService.getAccountById(1);

        assertNotNull(account);
        assertEquals(1, account.getId());
        assertEquals("Alice", account.getName());
        assertEquals(100.0, account.getBalance());
    }

    @Test
    void updateAccountAndBalance() {
        accountService.createAccount(2, "Bob", 50.0, "SAVINGS");

        accountService.updateAccount(2, "Robert", 75.0);
        Account updated = accountService.getAccountById(2);
        assertNotNull(updated);
        assertEquals("Robert", updated.getName());
        assertEquals(75.0, updated.getBalance());

        accountService.updateBalance(2, 150.0);
        Account balanceUpdated = accountService.getAccountById(2);
        assertNotNull(balanceUpdated);
        assertEquals(150.0, balanceUpdated.getBalance());
    }

    @Test
    void deleteAccountRemovesIt() {
        accountService.createAccount(3, "Charlie", 30.0, "CHECKING");
        assertNotNull(accountService.getAccountById(3));

        accountService.deleteAccount(3);
        assertNull(accountService.getAccountById(3));
    }

    @Test
    void transferBetweenAccounts() {
        accountService.createAccount(10, "Don", 200.0, "SAVINGS");
        accountService.createAccount(11, "Eva", 50.0, "SAVINGS");

        accountService.transfer(10, 11, 25.0);

        assertEquals(175.0, accountService.getAccountById(10).getBalance());
        assertEquals(75.0, accountService.getAccountById(11).getBalance());
    }

    @Test
    void depositIncreasesBalance() {
        accountService.createAccount(20, "Frank", 100.0, "SAVINGS");
        accountService.deposit(20, 50.0);
        assertEquals(150.0, accountService.getAccountById(20).getBalance());
    }

    @Test
    void withdrawDecreasesBalance() {
        accountService.createAccount(21, "Grace", 200.0, "SAVINGS");
        accountService.withdraw(21, 75.0);
        assertEquals(125.0, accountService.getAccountById(21).getBalance());
    }

    @Test
    void invalidAccountThrowsCustomException() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> accountService.deposit(999, 50.0));
        assertTrue(ex.getMessage().contains("not found"));
        assertEquals(999, ex.getAccountId());
    }

    @Test
    void insufficientBalanceThrowsCustomException() {
        accountService.createAccount(22, "Hank", 30.0, "SAVINGS");

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> accountService.withdraw(22, 100.0));
        assertTrue(ex.getMessage().contains("only"));
        assertEquals(22, ex.getAccountId());
        assertEquals(100.0, ex.getRequested());
        assertEquals(30.0, ex.getAvailable());
    }

    @Test
    void negativeAmountThrowsIllegalArgument() {
        accountService.createAccount(23, "Ivy", 100.0, "SAVINGS");
        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(23, -10.0));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(23, -5.0));
    }

    @Test
    void transferToSameAccountThrows() {
        accountService.createAccount(24, "Jack", 100.0, "SAVINGS");
        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(24, 24, 10.0));
    }

    @Test
    void updateNonExistentAccountThrows() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> accountService.updateAccount(999, "Nobody", 0));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
