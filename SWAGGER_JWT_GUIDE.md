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

## How to Use JWT in Swagger UI

### Step 1: Open Swagger UI
Navigate to: **http://localhost:8080/swagger-ui/index.html**

### Step 2: Get Your JWT Token
You'll need a valid JWT access token. Due to a known BCrypt password validation issue, you may need to:
- Use a token you've already obtained
- Fix the BCrypt issue first (see AUTHENTICATION_STATUS.md)
- Create a new user once BCrypt is fixed

### Step 3: Authorize in Swagger UI
1. Click the **🔓 Authorize** button (top-right of Swagger UI)
2. Enter your JWT token in the popup
3. **Important**: Enter the token WITHOUT the "Bearer " prefix
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

4. **health** - Health check endpoints
5. **Actuator** - Spring Boot monitoring endpoints

## Current Status

✅ **Swagger JWT Configuration**: Complete
✅ **Security Scheme**: Configured and working
✅ **OpenAPI Documentation**: Updated with JWT info
⚠️ **BCrypt Password Validation**: Known issue (see AUTHENTICATION_STATUS.md)

## Troubleshooting

### "401 Unauthorized" in Swagger
- Verify you clicked "Authorize" and entered a valid token
- Check token hasn't expired (access tokens expire in 15 minutes)
- Make sure you entered token WITHOUT "Bearer " prefix

### Can't Get Token (Login Fails)
This is the BCrypt password issue:
- Admin user (admin/admin123) fails to login
- Test users also fail with correct passwords
- Root cause: BCrypt hash validation issue
- Workaround: None currently - needs investigation

### Next Steps
1. Fix BCrypt password validation issue
2. Use Swagger UI with working authentication
3. Test all protected endpoints

## Files Modified

- `SwaggerConfig.java:66-91` - Added JWT security scheme and global requirement
- `SwaggerConfig.java:42-54` - Updated API description with JWT instructions
- `SwaggerConfig.java:106-117` - Added authentication endpoint group
- `JwtAuthenticationFilter.java:88-101` - Removed duplicate variable declaration

