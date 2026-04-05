-- SecureBank Initial Data
-- This script populates the database with sample users and accounts

-- Insert default users (passwords are plain text for development - use bcrypt in production)
INSERT INTO users (first_name, last_name, email, password, role, account_id) 
VALUES 
    ('John', 'Doe', 'john@example.com', 'password123', 'customer', 1),
    ('Jane', 'Smith', 'jane@example.com', 'password123', 'customer', 2),
    ('Admin', 'User', 'admin@securebank.com', 'admin123', 'admin', NULL),
    ('Staff', 'Member', 'staff@securebank.com', 'staff123', 'staff', NULL)
ON CONFLICT (email) DO NOTHING;

-- Insert accounts for the customers
INSERT INTO accounts (id, name, type, balance, status) 
VALUES 
    (1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE'),
    (2, 'Jane Smith', 'CHECKING', 2500.00, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Update users with their account IDs
UPDATE users SET account_id = 1 WHERE email = 'john@example.com';
UPDATE users SET account_id = 2 WHERE email = 'jane@example.com';

-- Insert some sample transactions
INSERT INTO transactions (id, account_id, type, amount, date, time, description, status)
VALUES 
    ('TXN001', 1, 'TRANSFER', 500.00, '2024-01-15', '10:30:00', 'Payment for services', 'completed'),
    ('TXN002', 1, 'WITHDRAWAL', 200.00, '2024-01-14', '14:45:00', 'ATM withdrawal', 'completed'),
    ('TXN003', 2, 'DEPOSIT', 1000.00, '2024-01-13', '09:00:00', 'Salary deposit', 'completed')
ON CONFLICT (id) DO NOTHING;

-- Insert sample beneficiaries
INSERT INTO beneficiaries (user_id, beneficiary_account_number, beneficiary_name, beneficiary_bank, nickname)
VALUES 
    (1, 'ACC002', 'Jane Smith', 'SecureBank', 'Jane''s Account'),
    (2, 'ACC001', 'John Doe', 'SecureBank', 'John''s Account')
ON CONFLICT DO NOTHING;

-- Insert sample cards
INSERT INTO cards (user_id, card_number, card_type, expiry_date, cvv, status)
VALUES 
    (1, '4532015112830366', 'debit', '12/26', '123', 'active'),
    (2, '4532015112830367', 'debit', '11/25', '456', 'active')
ON CONFLICT (card_number) DO NOTHING;