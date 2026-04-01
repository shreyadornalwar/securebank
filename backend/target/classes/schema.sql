-- Database Schema for SecureBank
-- MySQL Database Initialization Script

-- Drop tables if they exist (in reverse order of foreign key dependencies)
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS loan_activities;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS beneficiaries;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    phone VARCHAR(20),
    account_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Accounts table
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    type VARCHAR(50) NOT NULL DEFAULT 'SAVINGS',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Transactions table
CREATE TABLE transactions (
    id VARCHAR(50) PRIMARY KEY,
    account_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    date VARCHAR(20) NOT NULL,
    time VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    description VARCHAR(500),
    recipient_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB;

-- Beneficiaries table
CREATE TABLE beneficiaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    ifsc_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Loans table
CREATE TABLE loans (
    id VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    tenure_months INT NOT NULL,
    emi DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    applied_date VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Loan Activities table (for tracking)
CREATE TABLE loan_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id VARCHAR(50) NOT NULL,
    status VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    activity_date VARCHAR(20) NOT NULL,
    activity_time VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id)
) ENGINE=InnoDB;

-- Cards table
CREATE TABLE cards (
    id VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    number VARCHAR(19) NOT NULL,
    type VARCHAR(50) NOT NULL,
    expiry_date VARCHAR(7) NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    cvv VARCHAR(4),
    limit_amount DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Staff table
CREATE TABLE staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'STAFF',
    department VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Create indexes for better performance
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_account ON users(account_id);
CREATE INDEX idx_beneficiaries_user ON beneficiaries(user_id);
CREATE INDEX idx_loans_user ON loans(user_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_cards_user ON cards(user_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_staff_email ON staff(email);
CREATE INDEX idx_staff_status ON staff(status);

-- Insert default demo users for login
INSERT INTO users (first_name, last_name, email, password, role, account_id) VALUES 
('Admin', 'User', 'admin@securebank.com', 'admin123', 'ADMIN', 1),
('Staff', 'User', 'staff@securebank.com', 'staff123', 'STAFF', 2),
('Customer', 'User', 'customer@securebank.com', 'customer123', 'CUSTOMER', 3);

-- Insert default accounts for demo users
INSERT INTO accounts (id, name, balance, type, status) VALUES 
(1, 'Admin User', 100000.00, 'SAVINGS', 'ACTIVE'),
(2, 'Staff User', 50000.00, 'CHECKING', 'ACTIVE'),
(3, 'Customer User', 15000.00, 'SAVINGS', 'ACTIVE');

-- Insert default staff members
INSERT INTO staff (name, email, role, department, status) VALUES 
('John Smith', 'john.smith@securebank.com', 'MANAGER', 'Operations', 'ACTIVE'),
('Jane Doe', 'jane.doe@securebank.com', 'TELLER', 'Customer Service', 'ACTIVE'),
('Bob Wilson', 'bob.wilson@securebank.com', 'ADVISOR', 'Loans', 'ACTIVE');
