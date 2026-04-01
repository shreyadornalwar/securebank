# How to Add Operations in the Bank System

This guide explains how to add new operations (endpoints, services, and functionality) to the Java/Spring Boot bank system.

## 🏗️ Architecture Overview

The bank system follows a layered architecture:

```
Frontend (HTML/JS) → REST API → Controllers → Services → Repository → Database
```

### Key Components

1. **Controllers** (`/controller/`) - Handle HTTP requests and responses
2. **Services** (`/service/`) - Business logic and operations
3. **Models** (`/model/`) - Data structures and request/response objects
4. **Repository** (`/repository/`) - Data access layer
5. **Database** (`/database/`) - H2 in-memory database

## 📋 Step-by-Step: Adding a New Operation

Let's add a **Loan Management** operation as an example.

### Step 1: Create Model Classes

Create request/response models in `/src/main/java/com/bank/model/`:

```java
// Loan.java - Data model
package com.bank.model;

public class Loan {
    private String id;
    private int accountId;
    private double amount;
    private double interestRate;
    private int termMonths;
    private String status;
    private String type;
    
    // Constructors, getters, setters
}

// CreateLoanRequest.java - Request DTO
package com.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateLoanRequest {
    @NotNull(message = "Account ID is required")
    private Integer accountId;
    
    @NotNull(message = "Loan amount is required")
    @Min(value = 1000, message = "Minimum loan amount is $1000")
    private Double amount;
    
    @NotNull(message = "Interest rate is required")
    @Min(value = 0.01, message = "Interest rate must be positive")
    private Double interestRate;
    
    @NotNull(message = "Term is required")
    @Min(value = 6, message = "Minimum term is 6 months")
    private Integer termMonths;
    
    // Getters and setters
}
```

### Step 2: Create Service Layer

Create service in `/src/main/java/com/bank/service/`:

```java
// LoanService.java
package com.bank.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Loan;
import com.bank.model.CreateLoanRequest;
import com.bank.repository.JdbcAccountRepository;

@Service
public class LoanService {
    
    @Autowired
    private JdbcAccountRepository repository;
    
    @Autowired
    private AccountService accountService;
    
    @Transactional
    public Loan createLoan(CreateLoanRequest request) {
        // Business logic validation
        if (request.getAmount() > 50000) {
            throw new IllegalArgumentException("Loan amount exceeds limit");
        }
        
        // Create loan record
        Loan loan = new Loan();
        loan.setId("LN" + System.currentTimeMillis());
        loan.setAccountId(request.getAccountId());
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setStatus("PENDING");
        loan.setType("PERSONAL");
        
        repository.saveLoan(loan);
        
        // Update account balance (add loan amount)
        accountService.deposit(request.getAccountId(), request.getAmount());
        
        return loan;
    }
    
    public List<Loan> getLoansByAccountId(int accountId) {
        return repository.findLoansByAccountId(accountId);
    }
    
    public Loan getLoanById(String loanId) {
        return repository.findLoanById(loanId);
    }
}
```

### Step 3: Extend Repository

Add methods to `/src/main/java/com/bank/repository/JdbcAccountRepository.java`:

```java
// Add to JdbcAccountRepository.java

public void saveLoan(Loan loan) {
    String sql = "INSERT INTO loans (id, account_id, amount, interest_rate, term_months, status, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, loan.getId());
        ps.setInt(2, loan.getAccountId());
        ps.setDouble(3, loan.getAmount());
        ps.setDouble(4, loan.getInterestRate());
        ps.setInt(5, loan.getTermMonths());
        ps.setString(6, loan.getStatus());
        ps.setString(7, loan.getType());
        ps.executeUpdate();
    } catch (java.sql.SQLException e) {
        throw new RuntimeException("Failed to save loan", e);
    }
}

public List<Loan> findLoansByAccountId(int accountId) {
    List<Loan> loans = new ArrayList<>();
    String sql = "SELECT id, account_id, amount, interest_rate, term_months, status, type FROM loans WHERE account_id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, accountId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getString("id"));
                loan.setAccountId(rs.getInt("account_id"));
                loan.setAmount(rs.getDouble("amount"));
                loan.setInterestRate(rs.getDouble("interest_rate"));
                loan.setTermMonths(rs.getInt("term_months"));
                loan.setStatus(rs.getString("status"));
                loan.setType(rs.getString("type"));
                loans.add(loan);
            }
        }
    } catch (java.sql.SQLException e) {
        throw new RuntimeException("Failed to find loans", e);
    }
    
    return loans;
}
```

### Step 4: Create Controller

Create controller in `/src/main/java/com/bank/controller/`:

```java
// LoanController.java
package com.bank.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.model.Loan;
import com.bank.model.CreateLoanRequest;
import com.bank.service.LoanService;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping
    public ResponseEntity<?> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        try {
            Loan loan = loanService.createLoan(request);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Loan>> getLoansByUser(@PathVariable int userId) {
        try {
            List<Loan> loans = loanService.getLoansByAccountId(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoanById(@PathVariable String loanId) {
        try {
            Loan loan = loanService.getLoanById(loanId);
            if (loan != null) {
                return ResponseEntity.ok(loan);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

### Step 5: Update Database Schema

Add table creation to the repository constructor:

```java
// In JdbcAccountRepository constructor, add:
String loanDdl = "CREATE TABLE IF NOT EXISTS loans (" +
    "id VARCHAR(50) PRIMARY KEY, " +
    "account_id INT, " +
    "amount DOUBLE, " +
    "interest_rate DOUBLE, " +
    "term_months INT, " +
    "status VARCHAR(50), " +
    "type VARCHAR(50)" +
")";

try (Connection conn = DBConnection.getConnection();
     Statement stmt = conn.createStatement()) {
    stmt.execute(loanDdl);
    // ... existing code
}
```

## 🔧 Common Operation Patterns

### 1. CRUD Operations

```java
// Create
@PostMapping
public ResponseEntity<?> create(@RequestBody RequestDTO request) {
    Entity entity = service.create(request);
    return ResponseEntity.ok(entity);
}

// Read
@GetMapping("/{id}")
public ResponseEntity<Entity> getById(@PathVariable String id) {
    Entity entity = service.getById(id);
    return entity != null ? ResponseEntity.ok(entity) : ResponseEntity.notFound().build();
}

// Update
@PutMapping("/{id}")
public ResponseEntity<?> update(@PathVariable String id, @RequestBody RequestDTO request) {
    service.update(id, request);
    return ResponseEntity.ok("Updated successfully");
}

// Delete
@DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.ok("Deleted successfully");
}
```

### 2. List Operations

```java
@GetMapping
public ResponseEntity<List<Entity>> getAll() {
    List<Entity> entities = service.getAll();
    return ResponseEntity.ok(entities);
}

@GetMapping("/user/{userId}")
public ResponseEntity<List<Entity>> getByUser(@PathVariable int userId) {
    List<Entity> entities = service.getByUser(userId);
    return ResponseEntity.ok(entities);
}
```

### 3. Action Operations

```java
@PostMapping("/transfer")
public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
    try {
        service.transfer(request.getFromId(), request.getToId(), request.getAmount());
        return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

## 📝 Best Practices

### 1. Validation
- Use `@Valid` annotation on request bodies
- Add validation annotations to request DTOs
- Handle validation errors gracefully

### 2. Error Handling
- Use appropriate HTTP status codes
- Return meaningful error messages
- Log errors for debugging

### 3. Transaction Management
- Use `@Transactional` for operations that modify multiple entities
- Handle exceptions properly to avoid partial updates

### 4. Security
- Add authentication checks where needed
- Validate user permissions
- Use HTTPS in production

### 5. Documentation
- Add JavaDoc comments to public methods
- Document API endpoints with examples
- Update README with new functionality

## 🧪 Testing Your New Operation

### 1. Manual Testing with curl

```bash
# Test create loan
curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -d '{"accountId": 1, "amount": 5000, "interestRate": 0.05, "termMonths": 12}'

# Test get loans
curl http://localhost:8080/api/loans/user/1
```

### 2. Integration Testing

```java
// Add to a test class
@Test
public void testCreateLoan() {
    CreateLoanRequest request = new CreateLoanRequest();
    request.setAccountId(1);
    request.setAmount(5000.0);
    request.setInterestRate(0.05);
    request.setTermMonths(12);
    
    Loan loan = loanService.createLoan(request);
    assertNotNull(loan);
    assertEquals("PENDING", loan.getStatus());
}
```

## 🔄 Updating Frontend

### 1. Update API Client

Add new endpoints to `/frontend/public/api.js`:

```javascript
const api = {
    // ... existing methods
    
    createLoan: async function(request) {
        try {
            const res = await fetch(`${API_BASE_URL}/loans`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem('token') || ''}`
                },
                body: JSON.stringify(request)
            });
            return await safeJson(res);
        } catch (error) {
            return { success: false, message: error.message };
        }
    },
    
    getLoans: async function(userId) {
        try {
            const res = await fetch(`${API_BASE_URL}/loans/user/${userId}`, {
                headers: { 
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem('token') || ''}`
                }
            });
            return await safeJson(res);
        } catch (error) {
            return { success: false, message: error.message };
        }
    }
};
```

### 2. Update HTML Templates

Add forms and displays to relevant HTML files:

```html
<!-- In customer-dashboard.html -->
<div id="loan-section">
    <h3>Apply for Loan</h3>
    <form id="loan-form">
        <input type="number" id="loan-amount" placeholder="Loan Amount" required>
        <input type="number" id="loan-rate" placeholder="Interest Rate" required>
        <input type="number" id="loan-term" placeholder="Term (months)" required>
        <button type="submit">Apply</button>
    </form>
    <div id="loan-results"></div>
</div>
```

### 3. Add JavaScript Logic

```javascript
// In customer-dashboard.html script section
document.getElementById('loan-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const request = {
        accountId: currentUser.id,
        amount: parseFloat(document.getElementById('loan-amount').value),
        interestRate: parseFloat(document.getElementById('loan-rate').value),
        termMonths: parseInt(document.getElementById('loan-term').value)
    };
    
    const result = await api.createLoan(request);
    if (result.success) {
        alert('Loan application submitted successfully!');
        loadLoans();
    } else {
        alert('Error: ' + result.message);
    }
});
```

## 📊 Complete Example: Adding Bill Payment

Here's a complete example of adding a bill payment operation:

### 1. Models

```java
// BillPayment.java
public class BillPayment {
    private String id;
    private int accountId;
    private String payee;
    private double amount;
    private String status;
    private String date;
    // getters/setters
}

// PayBillRequest.java
public class PayBillRequest {
    @NotNull private Integer accountId;
    @NotBlank private String payee;
    @Min(1) private Double amount;
    // getters/setters
}
```

### 2. Service

```java
@Service
public class BillPaymentService {
    @Autowired private AccountService accountService;
    @Autowired private JdbcAccountRepository repository;
    
    @Transactional
    public BillPayment payBill(PayBillRequest request) {
        accountService.withdraw(request.getAccountId(), request.getAmount());
        
        BillPayment payment = new BillPayment();
        payment.setId("BP" + System.currentTimeMillis());
        payment.setAccountId(request.getAccountId());
        payment.setPayee(request.getPayee());
        payment.setAmount(request.getAmount());
        payment.setStatus("COMPLETED");
        payment.setDate(LocalDate.now().toString());
        
        repository.saveBillPayment(payment);
        return payment;
    }
}
```

### 3. Controller

```java
@RestController
@RequestMapping("/api/bill-payments")
public class BillPaymentController {
    @Autowired private BillPaymentService service;
    
    @PostMapping
    public ResponseEntity<?> payBill(@Valid @RequestBody PayBillRequest request) {
        try {
            BillPayment payment = service.payBill(request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
```

## 🎯 Summary Checklist

When adding a new operation:

- [ ] Create model classes (request/response)
- [ ] Implement service layer with business logic
- [ ] Add repository methods for data access
- [ ] Create REST controller with endpoints
- [ ] Update database schema if needed
- [ ] Add validation and error handling
- [ ] Update frontend API client
- [ ] Update HTML templates and JavaScript
- [ ] Test the complete flow
- [ ] Add documentation

## 🚀 Next Steps

1. **Start Simple**: Begin with basic CRUD operations
2. **Follow Patterns**: Use existing controllers as templates
3. **Test Thoroughly**: Test both success and error scenarios
4. **Document**: Keep documentation up to date
5. **Refactor**: Improve code as you learn more about requirements

This architecture makes it easy to add new banking operations while maintaining clean separation of concerns and following Spring Boot best practices.