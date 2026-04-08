-- SecureBank Initial Data (H2 compatible)

-- Insert default users
INSERT INTO users (first_name, last_name, email, password, role, account_id) 
SELECT 'John', 'Doe', 'john@example.com', 'password123', 'customer', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john@example.com');

INSERT INTO users (first_name, last_name, email, password, role, account_id) 
SELECT 'Jane', 'Smith', 'jane@example.com', 'password123', 'customer', 2
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'jane@example.com');

INSERT INTO users (first_name, last_name, email, password, role, account_id) 
SELECT 'Customer', 'User', 'customer@securebank.com', 'customer123', 'customer', 3
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@securebank.com');

INSERT INTO users (first_name, last_name, email, password, role, account_id) 
SELECT 'Admin', 'User', 'admin@securebank.com', 'admin123', 'admin', NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@securebank.com');

INSERT INTO users (first_name, last_name, email, password, role, account_id) 
SELECT 'Staff', 'Member', 'staff@securebank.com', 'staff123', 'staff', NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'staff@securebank.com');

-- Insert accounts
INSERT INTO accounts (id, name, type, balance, status) 
SELECT 1, 'John Doe', 'SAVINGS', 5000.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 1);

INSERT INTO accounts (id, name, type, balance, status) 
SELECT 2, 'Jane Smith', 'CHECKING', 2500.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 2);

INSERT INTO accounts (id, name, type, balance, status) 
SELECT 3, 'Customer User', 'SAVINGS', 1000.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE id = 3);

-- Insert transactions
INSERT INTO transactions (id, account_id, type, amount, date, time, description, status) 
SELECT 'TXN001', 1, 'TRANSFER', 500.00, '2024-01-15', '10:30:00', 'Payment for services', 'completed'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE id = 'TXN001');

INSERT INTO transactions (id, account_id, type, amount, date, time, description, status) 
SELECT 'TXN002', 1, 'WITHDRAWAL', 200.00, '2024-01-14', '14:45:00', 'ATM withdrawal', 'completed'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE id = 'TXN002');

INSERT INTO transactions (id, account_id, type, amount, date, time, description, status) 
SELECT 'TXN003', 2, 'DEPOSIT', 1000.00, '2024-01-13', '09:00:00', 'Salary deposit', 'completed'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE id = 'TXN003');