# SecureBank Backend Fix Guide

## Problem Summary
The backend was returning **503 Service Unavailable** on the `/health` endpoint because:
1. No database schema existed (missing `schema.sql`)
2. No initial data was being loaded (missing `data.sql`)
3. SQL initialization was disabled (`spring.sql.init.mode=never`)

## What Was Fixed

### 1. Created Database Schema (`backend/src/main/resources/schema.sql`)
- Added all required tables: users, accounts, transactions, beneficiaries, cards, loans, notifications
- Added performance indexes for faster queries
- Used `IF NOT EXISTS` to prevent errors on re-deployment

### 2. Created Initial Data (`backend/src/main/resources/data.sql`)
- Added sample users with different roles (customer, admin, staff)
- Created sample accounts with balances
- Added sample transactions, beneficiaries, and cards
- Used `ON CONFLICT DO NOTHING` to prevent duplicate errors

### 3. Updated Configuration (`backend/src/main/resources/application.properties`)
- Changed `spring.sql.init.mode=never` to `always`
- Added `spring.sql.init.continue-on-error=false` to fail fast on errors
- Added `spring.jpa.defer-datasource-initialization=true` to ensure schema is created before data

## Test Credentials

### Customer Login
- **Email:** john@example.com
- **Password:** password123
- **Role:** customer

### Admin Login
- **Email:** admin@securebank.com
- **Password:** admin123
- **Role:** admin

### Staff Login
- **Email:** staff@securebank.com
- **Password:** staff123
- **Role:** staff

## Deployment Steps

### For Render.com Deployment

1. **Commit and Push Changes**
   ```bash
   git add .
   git commit -m "Fix backend: add database schema and initialization"
   git push origin main
   ```

2. **Render will automatically:**
   - Detect the changes
   - Rebuild the backend
   - Deploy the new version
   - Initialize the database schema and data

3. **Wait 2-3 minutes** for the deployment to complete

4. **Test the backend:**
   - Visit: `https://securebank-backend-rc4i.onrender.com/health`
   - Should return: `{"status":"UP","service":"Banking System","database":"Connected"}`

5. **Test the frontend:**
   - Open your website
   - Try logging in with the test credentials above
   - All operations should now work

### For Local Development

1. **Set up PostgreSQL database:**
   ```bash
   # Create database
   createdb bankdb
   
   # Or using psql
   psql -U postgres -c "CREATE DATABASE bankdb;"
   ```

2. **Set environment variables:**
   ```bash
   # Windows (Command Prompt)
   set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bankdb
   set SPRING_DATASOURCE_USERNAME=postgres
   set SPRING_DATASOURCE_PASSWORD=your_password
   
   # Windows (PowerShell)
   $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/bankdb"
   $env:SPRING_DATASOURCE_USERNAME="postgres"
   $env:SPRING_DATASOURCE_PASSWORD="your_password"
   
   # Linux/Mac
   export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/bankdb"
   export SPRING_DATASOURCE_USERNAME="postgres"
   export SPRING_DATASOURCE_PASSWORD="your_password"
   ```

3. **Run the application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. **Test locally:**
   - Visit: `http://localhost:8080/health`
   - Should return: `{"status":"UP","service":"Banking System","database":"Connected"}`

## Verification Checklist

After deployment, verify these endpoints:

- [ ] `GET https://securebank-backend-rc4i.onrender.com/` - Should return API info
- [ ] `GET https://securebank-backend-rc4i.onrender.com/health` - Should return status UP
- [ ] `POST https://securebank-backend-rc4i.onrender.com/api/auth/login` - Should accept test credentials
- [ ] Frontend login page - Should accept test credentials
- [ ] Customer dashboard - Should load account data
- [ ] All CRUD operations - Should work without errors

## Troubleshooting

### If backend still shows 503:

1. **Check Render Logs:**
   - Go to Render Dashboard → Your Service → Logs
   - Look for database connection errors
   - Check if schema.sql and data.sql were executed

2. **Verify Database Connection:**
   - Ensure Render database is properly linked to your service
   - Check that environment variables are set in Render

3. **Test Database Directly:**
   - Use Render's database connection info
   - Connect with a tool like DBeaver or pgAdmin
   - Verify tables were created

4. **Common Issues:**
   - **Connection timeout:** Render free tier databases may be slow to start
   - **Missing credentials:** Check Render environment variables
   - **Schema errors:** Check logs for SQL syntax errors

### If frontend still doesn't work:

1. **Clear browser cache and cookies**
2. **Check browser console for errors** (F12 → Console)
3. **Verify API URL** in browser console should show: `https://securebank-backend-rc4i.onrender.com`
4. **Try incognito/private browsing mode**

## Next Steps

Once the backend is working:

1. **Change default passwords** in data.sql for production
2. **Implement password hashing** (BCrypt) instead of plain text
3. **Add more comprehensive error handling**
4. **Set up monitoring and alerts**
5. **Consider database backups**

## Support

If you continue experiencing issues:
1. Check the Render deployment logs
2. Review the backend logs for specific error messages
3. Verify all environment variables are correctly set
4. Test the database connection independently

---

**Status:** ✅ Backend schema and initialization files created
**Next Action:** Deploy changes to Render and test