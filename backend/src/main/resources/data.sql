-- SecureBank Initial Data
-- This script populates the database with sample users and accounts

-- Insert default users (passwords are plain text for development - use bcrypt in production)
MERGE INTO users (first_name, last_name, email, password, role, account_id) 
KEY(email)
VALUES 
    ('John', 'Doe', 'john@example.com', 'password123', 'customer', 1),
    ('Jane', 'Smith', 'jane@example.com', 'password123', 'customer', 2),
    ('Customer', 'User', 'customer@securebank.com', 'customer123', 'customer', 3),
    ('Admin', 'User', 'admin@securebank.com', 'admin123', 'admin', NULL),
    ('Staff', 'Member', 'staff@securebank.com', 'staff123', 'staff', NULL);

-- Insert accounts for the customers
MERGE INTO accounts (id, name, type, balance, status) 
KEY(id)
VALUES 
    (1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE'),
    (2, 'Jane Smith', 'CHECKING', 2500.00, 'ACTIVE'),
    (3, 'Customer User', 'SAVINGS', 1000.00, 'ACTIVE');

-- Insert some sample transactions
MERGE INTO transactions (id, account_id, type, amount, date, time, description, status)
KEY(id)
VALUES 
    ('TXN001', 1, 'TRANSFER', 500.00, '2024-01-15', '10:30:00', 'Payment for services', 'completed'),
    ('TXN002', 1, 'WITHDRAWAL', 200.00, '2024-01-14', '14:45:00', 'ATM withdrawal', 'completed'),
    ('TXN003', 2, 'DEPOSIT', 1000.00, '2024-01-13', '09:00:00', 'Salary deposit', 'completed');

-- Insert sample beneficiaries
MERGE INTO beneficiaries (user_id, beneficiary_account_number, beneficiary_name, beneficiary_bank, nickname)
KEY(beneficiary_account_number, user_id)
VALUES 
    (1, 'ACC002', 'Jane Smith', 'SecureBank', 'Jane''s Account'),
    (2, 'ACC001', 'John Doe', 'SecureBank', 'John''s Account');

-- Insert sample cards
MERGE INTO cards (user_id, card_number, card_type, expiry_date, cvv, status)
KEY(card_number)
VALUES 
    (1, '4532015112830366', 'debit', '12/26', '123', 'active'),
    (2, '4532015112830367', 'debit', '11/25', '456', 'active');
