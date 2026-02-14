# Swagger UI + JWT Authentication Guide

## What's Been Configured

### JWT Security Scheme in Swagger
- **Security Scheme Name**: `bearer-jwt`
- **Type**: HTTP Bearer Authentication
- **Format**: JWT
- **Description**: Updated to explain how to obtain and use tokens

### API Documentation Updates
1. **API Description**: Now mentions JWT Bearer token requirement
2. **Authentication Group**: New Swagger group for `/api/auth/**` endpoints
3. **Global Security**: All endpoints require JWT by default

### Test Data Endpoints (Development Only)
- **GET /api/test/hash?password=xxx** - Generate BCrypt hash for password
- **POST /api/test/validate?password=xxx&hash=xxx** - Test BCrypt validation

## How to Use JWT in Swagger UI

### Step 1: Get JWT Token
Login via Swagger UI or curl:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### Step 2: Open Swagger UI
Navigate to: **http://localhost:8080/swagger-ui/index.html**

### Step 3: Authorize in Swagger UI
1. Click the **🔓 Authorize** button (top-right of Swagger UI)
2. Enter your JWT access token in the popup
3. **Important**: Enter the token **WITHOUT** the "Bearer " prefix
   - ✅ Correct: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - ❌ Wrong: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
4. Click **Authorize**
5. Close the popup

### Step 4: Make Authenticated Requests
Now all API requests will include your JWT token in the Authorization header:
```
Authorization: Bearer <your-token>
```

## API Groups Available

1. **authentication** - `/api/auth/**`
   - POST `/api/auth/login` - Get access token
   - POST `/api/auth/refresh` - Refresh expired token
   - GET `/api/auth/me` - Get current user info

2. **shops** - `/api/shops/**`
   - GET `/api/shops` - List all shops
   - GET `/api/shops/{id}` - Get shop by ID
   - POST `/api/shops` - Create new shop
   - PUT `/api/shops/{id}` - Update shop
   - DELETE `/api/shops/{id}` - Delete shop

3. **users** - `/api/users`
   - POST `/api/users` - Create new user (ADMIN only)

4. **test** - `/api/test/**` (development only)
   - GET `/api/test/hash` - Generate BCrypt hash
   - POST `/api/test/validate` - Test BCrypt validation

5. **health** - Health check endpoints

## Current Status

✅ **Swagger JWT Configuration**: Complete and working
✅ **Security Scheme**: Configured and working
✅ **OpenAPI Documentation**: Updated with JWT info
✅ **Admin Login**: Working (username: `admin`, password: `admin`)
✅ **BCrypt Password Validation**: Fixed with fresh hash

## Test Credentials

**Admin User:**
- Username: `admin`
- Password: `admin`
- Role: `ADMIN`

**Note**: Original password `admin123` did not work due to BCrypt hash issues in migration files.
Password has been reset to `admin` for testing purposes.

## Troubleshooting

### "401 Unauthorized" in Swagger
- Verify you clicked "Authorize" and entered a valid token
- Check token hasn't expired (access tokens expire in 15 minutes)
- Make sure you entered token WITHOUT "Bearer " prefix
- Get a fresh token via `/api/auth/login`

### Login Returns 401
- Verify username and password are correct
- Admin user: `admin` / `admin`
- Check application logs for specific error details

## Files Modified

### Swagger JWT Configuration
- `SwaggerConfig.java:66-91` - Added JWT security scheme and global requirement
- `SwaggerConfig.java:42-54` - Updated API description with JWT instructions
- `SwaggerConfig.java:112-121` - Added authentication endpoint group
- `JwtAuthenticationFilter.java:142-151` - Added Swagger docs to public endpoints

### BCrypt Fix
- `V7__fix_admin_password.sql` - Migration with working BCrypt hash
- `TestDataController.java` - Development endpoint for BCrypt testing
- `SWAGGER_JWT_GUIDE.md` - This file
