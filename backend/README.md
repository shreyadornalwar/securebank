# Banking System - Full Stack Application

This is a complete banking system with a **Java Spring Boot backend** and a **Node.js Express frontend server** with an HTML/JavaScript interface.

## Architecture

```
Frontend (HTML/JavaScript) 
    ↓
Node.js Express Server (port 3000) - API Proxy
    ↓
Java Spring Boot Backend (port 8080) - REST API
    ↓
H2 In-Memory Database
```

- ✅ Create bank accounts
- ✅ List all accounts
- ✅ Get account by ID
- ✅ Update account balance
- ✅ Transfer money between accounts
- ✅ Delete accounts
- ✅ RESTful API endpoints
- ✅ Docker containerization

## Technologies

- Java 17 with Spring Boot
- H2 Database (in-memory)
- Maven for build management
- Docker for containerization

## API Endpoints

### Create Account
```
POST /api/accounts
Content-Type: application/json

{
  "id": 1,
  "name": "Rahul",
  "balance": 5000.0
}
```

### Get All Accounts
```
GET /api/accounts
```

### Get Account by ID
```
GET /api/accounts/{id}
```

### Update Balance
```
PUT /api/accounts/{id}/balance?balance=6000
```

### Transfer Money
```
POST /api/accounts/transfer
Content-Type: application/json

{
  "fromId": 1,
  "toId": 2,
  "amount": 1000.0
}
```

### Delete Account
```
DELETE /api/accounts/{id}
```

## How to Run

### Local Development
1. Ensure Java 17 and Maven are installed
2. Clone/download the project
3. Run `mvn spring-boot:run`
4. API available at `http://localhost:8080`

### Docker (Recommended)
1. Ensure Docker is installed and running
2. Build the image: `docker build -t banking-backend .`
3. Run the container: `docker run -p 8080:8080 --rm banking-backend`
4. API available at `http://localhost:8080`

## Testing

Run the unit tests with:
```bash
mvn test
```

## Database

Uses H2 in-memory database that resets on each run. For production, configure a persistent database like MySQL or PostgreSQL.

### H2 Console
When running locally, access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:bankdb`
- Username: `sa`
- Password: (leave empty)