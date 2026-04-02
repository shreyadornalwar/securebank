# Quick Start Guide - Java Spring Boot Backend

## 🚀 Get Started in 2 Minutes

### Step 1: Build the Project
```bash
cd backend
mvn clean install
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

### Step 3: Verify It's Running
Open your browser and visit:
```
http://localhost:8080/api/auth/verify
```

You should see a message about authentication.

---

## 📝 Quick API Testing

### Login Request
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@securebank.com",
    "password": "admin123"
  }'
```

### Get All Accounts
```bash
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Deposit Money
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC001",
    "amount": 5000,
    "description": "Test deposit"
  }'
```

---

## 👥 Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@securebank.com | admin123 |
| Staff | staff@securebank.com | staff123 |
| Customer | customer@securebank.com | customer123 |

---

## 📚 API Endpoints by Category

### Authentication
- `POST /api/auth/login` - User login
- `GET /api/auth/verify` - Verify token

### Accounts
- `GET /api/accounts` - List all
- `GET /api/accounts/{id}` - Get details

### Transactions
- `GET /api/transactions` - List all
- `POST /api/transactions/deposit` - Make deposit
- `POST /api/transactions/withdraw` - Make withdrawal

### Analytics
- `GET /api/analytics/summary` - Dashboard stats
- `GET /api/analytics/weekly-trend` - Weekly data

### Notifications
- `GET /api/notifications` - Get all
- `PUT /api/notifications/{id}/read` - Mark as read

### Loans
- `GET /api/loans` - List all loans
- `POST /api/loans` - Apply for loan

### Beneficiaries
- `GET /api/beneficiaries` - List all
- `POST /api/beneficiaries` - Add new

### Admin
- `GET /api/admin/users` - List all users
- `POST /api/admin/users` - Create user
- `DELETE /api/admin/users/{id}` - Delete user

---

## 🔧 Project Structure

```
src/main/java/com/bank/
├── Main.java (Main entry point)
├── config/
│   ├── CorsConfig.java (CORS setup)
│   ├── SecurityConfig.java
│   └── JwtAuthFilter.java
├── controller/
│   ├── AuthController.java
│   ├── AccountController.java
│   ├── TransactionController.java
│   ├── CustomerController.java
│   ├── StaffController.java
│   ├── LoanController.java
│   ├── BeneficiaryController.java
│   ├── CardController.java
│   ├── NotificationController.java
│   └── SseController.java
├── model/
│   ├── User.java
│   ├── Account.java
│   ├── Transaction.java
│   └── LoginRequest.java
├── service/
├── repository/
└── database/
```

---

## ⚙️ Configuration

Default configuration in `application.properties`:
- **Port**: 8080
- **JWT Expiration**: 24 hours
- **Database**: H2 (in-memory)
- **Kafka**: localhost:9092 (optional)

To change port:
```properties
server.port=9090
```

---

## 🐛 Troubleshooting

### Build fails?
```bash
mvn clean install -DskipTests
```

### Can't start on port 8080?
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Or change port in application.properties
server.port=8081
```

### Frontend can't connect?
- Ensure backend is running: `http://localhost:8080`
- Check frontend's `api.js` has correct API_BASE_URL
- Verify CORS is enabled in `CorsConfig.java`

---

## 📦 Maven Commands

| Command | Purpose |
|---------|---------|
| `mvn clean` | Remove build files |
| `mvn compile` | Compile source code |
| `mvn test` | Run tests |
| `mvn package` | Build JAR file |
| `mvn spring-boot:run` | Run application directly |
| `mvn clean install` | Clean + Compile + Test + Package |

---

## 🌐 Frontend Integration

Frontend expects:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

If you're using a different port, update `frontend/public/api.js`:
```javascript
const API_BASE_URL = 'http://localhost:YOUR_PORT/api';
```

---

## ✨ Key Features

✅ 8 REST Controllers
✅ 40+ API Endpoints
✅ JWT Authentication
✅ Mock Database (3 Users, 3 Accounts, 4 Transactions)
✅ Role-Based Access (Admin, Staff, Customer)
✅ Comprehensive Error Handling
✅ CORS Enabled for Frontend
✅ Fully Documented APIs

---

## 📖 Complete Documentation

See `JAVA_BACKEND_SETUP.md` for comprehensive setup and deployment guide.

---

## 🎯 Next Steps

1. ✅ Backend Created
2. 📋 Run `mvn clean install`
3. 🚀 Run `mvn spring-boot:run`
4. 🧪 Test endpoints with provided credentials
5. 🔗 Connect frontend to backend
6. 📊 Access admin dashboard at `http://localhost:3000/admin.html`

Happy Banking! 🏦💳

