# SecureBank WebSocket Integration

This document explains how to set up and use the real-time WebSocket functionality that connects the backend and frontend with live data updates.

## Overview

The SecureBank application now supports real-time communication between the backend and frontend using WebSocket technology. This enables:

- **Live transaction notifications** - Instant updates when transactions occur
- **Real-time balance updates** - Immediate balance changes reflected across all connected clients
- **Security alerts** - Instant security notifications
- **System notifications** - Real-time system messages

## Architecture

### Backend Components

1. **WebSocket Configuration** (`WebSocketConfig.java`)
   - Configures WebSocket endpoints and message brokers
   - Uses STOMP over WebSocket for structured messaging
   - Endpoint: `/ws` with SockJS fallback

2. **WebSocket Service** (`WebSocketNotificationService.java`)
   - Handles sending messages to connected clients
   - Provides methods for different message types:
     - Notifications (`/topic/notifications`)
     - Transactions (`/topic/transactions`)
     - Balances (`/topic/balances`)
     - Security alerts (`/topic/security`)

3. **WebSocket Controller** (`WebSocketController.java`)
   - REST endpoints for testing WebSocket functionality
   - `/api/ws/test-*` endpoints for sending test messages

4. **Enhanced Transaction Controller**
   - Automatically sends WebSocket notifications on transaction events
   - Integrates with `WebSocketNotificationService`

### Frontend Components

1. **WebSocket Client** (`public/websocket.js`)
   - `SecureBankWebSocket` class for managing WebSocket connections
   - Automatic reconnection logic
   - Event handlers for different message types
   - Real-time UI updates

2. **Customer Dashboard Integration**
   - Auto-connects when user is authenticated
   - Real-time balance updates
   - Live transaction list updates
   - Notification popups

3. **Test Page** (`test-websocket.html`)
   - Interactive testing interface
   - Manual message sending capabilities
   - Connection status monitoring

## Setup Instructions

### 1. Backend Setup

1. **Add WebSocket Dependencies**
   The `pom.xml` has been updated with:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-websocket</artifactId>
   </dependency>
   ```

2. **Start the Spring Boot Application**
   ```bash
   cd backend/springboot-jwt-kafka-project
   mvn spring-boot:run
   ```
   
   The application will start on `http://localhost:8080`

### 2. Frontend Setup

1. **Start the Frontend Server**
   ```bash
   cd frontend
   node server.js
   ```
   
   The frontend will be available at `http://localhost:3000`

2. **Access the Application**
   - Main dashboard: `http://localhost:3000/customer-dashboard.html`
   - WebSocket test page: `http://localhost:3000/test-websocket.html`

## Usage

### Automatic Real-time Updates

Once logged in, the customer dashboard automatically:
- Connects to the WebSocket server
- Receives real-time transaction updates
- Updates balance displays instantly
- Shows notification popups
- Updates transaction lists in real-time

### Manual Testing

Use the test page (`test-websocket.html`) to:
1. Connect to the WebSocket server
2. Send test notifications, transactions, balance updates, and security alerts
3. See real-time message handling

### API Endpoints for Testing

The backend provides REST endpoints for testing WebSocket functionality:

- **POST** `/api/ws/test-notification` - Send test notification
- **POST** `/api/ws/test-transaction` - Send test transaction update
- **POST** `/api/ws/test-balance` - Send test balance update
- **POST** `/api/ws/test-security` - Send test security alert

Example request:
```bash
curl -X POST http://localhost:8080/api/ws/test-notification \
  -H "Content-Type: application/json" \
  -d '{"message": "Test notification from API"}'
```

## Message Types

### Notification Messages
```json
{
  "type": "notification",
  "message": "Your transaction was successful",
  "timestamp": 1647987654321
}
```

### Transaction Messages
```json
{
  "type": "transaction",
  "accountId": "ACC001",
  "transactionType": "Deposit",
  "amount": 1000.0,
  "timestamp": 1647987654321
}
```

### Balance Messages
```json
{
  "type": "balance",
  "accountId": "ACC001",
  "newBalance": 15000.0,
  "timestamp": 1647987654321
}
```

### Security Messages
```json
{
  "type": "security",
  "alertType": "Suspicious Login",
  "description": "Unusual login detected from new location",
  "timestamp": 1647987654321
}
```

## Frontend Event Handlers

The WebSocket client provides event handlers that you can customize:

```javascript
// Set up event handlers
secureBankWS.onNotification = function(data) {
    console.log('Notification received:', data);
    // Custom notification handling
};

secureBankWS.onTransaction = function(data) {
    console.log('Transaction update:', data);
    // Custom transaction handling
};

secureBankWS.onBalance = function(data) {
    console.log('Balance update:', data);
    // Custom balance handling
};

secureBankWS.onSecurity = function(data) {
    console.log('Security alert:', data);
    // Custom security handling
};

secureBankWS.onConnect = function() {
    console.log('WebSocket connected');
    // Custom connection handling
};

secureBankWS.onDisconnect = function() {
    console.log('WebSocket disconnected');
    // Custom disconnection handling
};
```

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Ensure backend is running on port 8080
   - Check browser console for CORS errors
   - Verify WebSocket endpoint URL

2. **Messages Not Received**
   - Check if WebSocket is connected
   - Verify message format
   - Check browser network tab for WebSocket frames

3. **CORS Issues**
   - Backend CORS configuration is set to allow all origins
   - Check if frontend and backend ports are correct

### Debugging

1. **Browser Console**
   - Open browser developer tools
   - Check Console tab for WebSocket connection logs
   - Monitor Network tab for WebSocket frames

2. **Backend Logs**
   - Check Spring Boot application logs
   - Look for WebSocket connection messages
   - Monitor for any errors

3. **Test Page**
   - Use `test-websocket.html` to verify functionality
   - Test individual message types
   - Verify connection status

## Security Considerations

- WebSocket connections use the same authentication as REST APIs
- Messages are sent to all connected clients (broadcast model)
- Consider implementing user-specific channels for production
- Use HTTPS in production environments

## Future Enhancements

- User-specific WebSocket channels
- Message persistence and history
- Reconnection with state synchronization
- Performance optimization for high-frequency updates
- Integration with Kafka for message queuing

## Files Modified

### Backend
- `pom.xml` - Added WebSocket dependency
- `WebSocketConfig.java` - WebSocket configuration
- `WebSocketNotificationService.java` - Message sending service
- `WebSocketController.java` - Test endpoints
- `TransactionController.java` - Integrated WebSocket notifications
- `Transaction.java` - Enhanced model

### Frontend
- `public/websocket.js` - WebSocket client implementation
- `customer-dashboard.html` - Integrated WebSocket functionality
- `test-websocket.html` - Testing interface

This WebSocket integration provides a solid foundation for real-time features in the SecureBank application, enabling instant updates and notifications across all connected clients.