# JWT + RBAC Security Design Document

**Date:** 2025-02-14
**Project:** AlcoholShop Service
**Author:** AlcoRadar Team
**Status:** Approved

---

## Overview

This document describes the design for implementing JWT (JSON Web Token) authentication with Role-Based Access Control (RBAC) for the AlcoholShop REST API service.

### Goals

1. **Secure protected endpoints** - Require authentication for shop creation
2. **Public read access** - Allow anonymous access to GET endpoints
3. **User management** - Admin-only user creation with role-based permissions
4. **Token-based authentication** - Stateless JWT tokens with refresh mechanism
5. **Password security** - BCrypt hashing with complexity validation

### Security Requirements

| Requirement | Description |
|-------------|-------------|
| **Public endpoints** | GET /api/shops, GET /api/shops/{id} |
| **Protected endpoints** | POST /api/shops, POST /api/users |
| **Roles** | USER (can create shops), ADMIN (create shops + manage users) |
| **Token expiration** | Access token: 15 minutes, Refresh token: 7 days |
| **Password rules** | Min 8 chars, uppercase, lowercase, digit, special char |
| **Hashing** | BCrypt with work factor 10 |

---

## Architecture

### Approach: Simplified Controller-Based Security

This design uses a custom aspect-based approach rather than Spring Security filter chains for simplicity and maintainability.

### Flow Diagram

```
HTTP Request
  ↓
@RestController (AlcoholShopController)
  ↓
@RequireAuth(roles = {ADMIN}) ← Custom annotation
  ↓
AuthenticationAspect (intercepts annotated methods)
  ↓
- Validates JWT from Authorization header
- Checks user has required role
- Sets SecurityContext
  ↓
AlcoholShopUseCase (business logic)
```

### Key Design Decisions

- **No Spring Security filter chain** - Avoids complex configuration
- **Custom `@RequireAuth` annotation** - Explicit security on controller methods
- **JWT in Authorization header** - Standard `Bearer <token>` format
- **Aspect-based interception** - Uses Spring AOP for clean separation
- **Role-based permissions** - Enum-based (USER, ADMIN)

---

## Components

### Domain Layer

#### User Entity
```java
package com.alcoradar.alcoholshop.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String password; // BCrypt hashed
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method
    public static User create(String username, String hashedPassword, Role role);

    // Business methods
    public void updateInformation(String username);
    public void changePassword(String newHashedPassword);
    public boolean isPasswordMatch(String rawPassword, HashingService hashingService);

    // Getters
    public UUID getId();
    public String getUsername();
    public Role getRole();
}
```

#### Role Enum
```java
package com.alcoradar.alcoholshop.domain.model;

public enum Role {
    USER,
    ADMIN
}
```

#### User Repository Port
```java
package com.alcoradar.alcoholshop.domain.repository;

import com.alcoradar.alcoholshop.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID id);
    boolean existsByUsername(String username);
}
```

#### Domain Exceptions
```java
package com.alcoradar.alcoholshop.domain.exception;

// Base exception
public abstract class AuthenticationException extends DomainException {
    public AuthenticationException(String message) {
        super(message);
    }
}

// Specific exceptions
public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

public class ExpiredTokenException extends InvalidTokenException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}

public class AccessDeniedException extends AuthenticationException {
    private final Role requiredRole;
    private final Role userRole;

    public AccessDeniedException(Role requiredRole, Role userRole) {
        super(String.format("Access denied: Required role: %s, User role: %s",
            requiredRole, userRole));
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }
}

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID id) {
        super(String.format("User not found with id: %s", id));
    }
}

public class UsernameAlreadyExistsException extends DomainException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("Username already exists: %s", username));
    }
}
```

### Application Layer

#### Authentication Use Case
```java
package com.alcoradar.alcoholshop.application.usecase;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.exception.*;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;

@RequiredArgsConstructor
public class AuthenticationUseCase {
    private final UserRepository userRepository;
    private final SecurityService securityService;

    public LoginResponse login(String username, String password) {
        // Find user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Verify password
        if (!securityService.checkPassword(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Generate tokens
        String accessToken = securityService.generateAccessToken(user, 15 * 60); // 15 minutes
        String refreshToken = securityService.generateRefreshToken(user, 7 * 24 * 60 * 60); // 7 days

        return LoginResponse.of(accessToken, refreshToken, UserResponse.from(user));
    }

    public RefreshResponse refreshAccessToken(String refreshToken) {
        // Validate refresh token
        Claims claims = securityService.validateRefreshToken(refreshToken);
        UUID userId = claims.get("userId", UUID.class);

        // Load user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Generate new access token
        String newAccessToken = securityService.generateAccessToken(user, 15 * 60);

        return RefreshResponse.of(newAccessToken);
    }

    public UserResponse createUser(CreateUserRequest request, Role currentRole) {
        // Only ADMIN can create users
        if (currentRole != Role.ADMIN) {
            throw new AccessDeniedException(Role.ADMIN, currentRole);
        }

        // Check if username exists
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        // Hash password
        String hashedPassword = securityService.hashPassword(request.password());

        // Create user
        User user = User.create(request.username(), hashedPassword, request.role());
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.from(user);
    }
}
```

#### DTOs
```java
// LoginRequest.java
public record LoginRequest(
    @NotBlank(message = "Username must not be blank")
    String username,

    @NotBlank(message = "Password must not be blank")
    String password
) {}

// LoginResponse.java
public record LoginResponse(
    String accessToken,
    String refreshToken,
    UserResponse user
) {
    public static LoginResponse of(String accessToken, String refreshToken, UserResponse user) {
        return new LoginResponse(accessToken, refreshToken, user);
    }
}

// RefreshRequest.java
public record RefreshRequest(
    @NotBlank(message = "Refresh token must not be blank")
    String refreshToken
) {}

// RefreshResponse.java
public record RefreshResponse(
    String accessToken
) {
    public static RefreshResponse of(String accessToken) {
        return new RefreshResponse(accessToken);
    }
}

// CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
             message = "Password must contain uppercase, lowercase, digit, and special character")
    String password,

    @NotNull(message = "Role must not be null")
    Role role
) {}

// UserResponse.java
public record UserResponse(
    UUID id,
    String username,
    Role role,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
```

#### Security Service
```java
package com.alcoradar.alcoholshop.application.service;

import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class SecurityService {
    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    public String generateAccessToken(User user, int expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_USER_ID, user.getId().toString())
            .claim(CLAIM_USERNAME, user.getUsername())
            .claim(CLAIM_ROLE, user.getRole().name())
            .claim(CLAIM_TYPE, "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(User user, int expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_USER_ID, user.getId().toString())
            .claim(CLAIM_TYPE, "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public Claims validateAccessToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        if (!"access".equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new InvalidTokenException("Token is not an access token");
        }

        return claims;
    }

    public Claims validateRefreshToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        if (!"refresh".equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        return claims;
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    public UUID getUserIdFromToken(Claims claims) {
        return UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
    }

    public Role getRoleFromToken(Claims claims) {
        return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Infrastructure Layer

#### User Entity (JPA)
```java
package com.alcoradar.alcoholshop.infrastructure.persistence.entity;

import com.alcoradar.alcoholshop.domain.model.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_username", columnList = "username"))
public class UserEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Converters
    public User toDomain() {
        return User.builder()
            .id(id)
            .username(username)
            .password(password)
            .role(role)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPasswordHash());
        entity.setRole(user.getRole());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
```

#### Spring Data User Repository
```java
package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

#### User Repository Implementation
```java
package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.infrastructure.persistence.entity.UserEntity;
import com.alcoradar.alcoholshop.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = springDataUserRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username)
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
            .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }
}
```

### Interfaces Layer

#### @RequireAuth Annotation
```java
package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.domain.model.Role;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAuth {
    Role[] roles() default {};
}
```

#### Authentication Aspect
```java
package com.alcoradar.alcoholshop.interfaces.security;

import com.alcoradar.alcoholshop.application.service.SecurityService;
import com.alcoradar.alcoholshop.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationAspect {
    private final SecurityService securityService;

    @Around("@annotation(requireAuth)")
    public Object authenticate(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header from {}", request.getRemoteAddr());
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token
            Claims claims = securityService.validateAccessToken(token);
            UUID userId = securityService.getUserIdFromToken(claims);
            Role userRole = securityService.getRoleFromToken(claims);

            // Check role requirements
            Role[] requiredRoles = requireAuth.roles();
            if (requiredRoles.length > 0 && !Arrays.asList(requiredRoles).contains(userRole)) {
                log.warn("Access denied for user {} with role {} to protected resource", userId, userRole);
                throw new AccessDeniedException(requiredRoles[0], userRole);
            }

            // Set user context in request attributes for use cases
            request.setAttribute("userId", userId);
            request.setAttribute("userRole", userRole);

            // Proceed with method execution
            return joinPoint.proceed();

        } catch (ExpiredJwtException e) {
            log.warn("Expired token from {}", request.getRemoteAddr());
            throw new ExpiredTokenException("Access token expired. Please refresh your token");
        } catch (JwtException e) {
            log.warn("Invalid token from {}: {}", request.getRemoteAddr(), e.getMessage());
            throw new InvalidTokenException("Invalid or malformed JWT token");
        }
    }

    private HttpServletRequest getCurrentRequest() {
        return ((org.springframework.web.context.request.RequestAttributes)
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
            .getCurrentRequest();
    }
}
```

#### Authentication Controller
```java
package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.application.usecase.AuthenticationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "authentication", description = "Authentication endpoints")
public class AuthenticationController {

    private final AuthenticationUseCase authenticationUseCase;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationUseCase.login(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authenticationUseCase.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        UserResponse response = authenticationUseCase.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }
}
```

#### User Management Controller
```java
package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.application.usecase.AuthenticationUseCase;
import com.alcoradar.alcoholshop.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "users", description = "User management endpoints (ADMIN only)")
public class UserController {

    private final AuthenticationUseCase authenticationUseCase;

    @PostMapping
    @RequireAuth(roles = {Role.ADMIN})
    @Operation(summary = "Create new user", description = "Create a new user (ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request,
        HttpServletRequest httpRequest
    ) {
        Role currentRole = (Role) httpRequest.getAttribute("userRole");
        UserResponse response = authenticationUseCase.createUser(request, currentRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @RequireAuth(roles = {Role.ADMIN})
    @Operation(summary = "Get user by ID", description = "Get user information (ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse response = authenticationUseCase.getCurrentUser(id);
        return ResponseEntity.ok(response);
    }
}
```

#### Updated AlcoholShopController
```java
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class AlcoholShopController {

    private final AlcoholShopUseCase useCase;

    @PostMapping
    @RequireAuth(roles = {Role.USER, Role.ADMIN})  // ← ADD THIS
    public ResponseEntity<AlcoholShopResponse> create(@Valid @RequestBody CreateAlcoholShopRequest request) {
        AlcoholShopResponse response = useCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    // Public endpoint - no annotation
    public ResponseEntity<AlcoholShopResponse> findById(@PathVariable UUID id) {
        AlcoholShopResponse response = useCase.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    // Public endpoint - no annotation
    public ResponseEntity<PageResponse<AlcoholShopResponse>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sortBy
    ) {
        PageResponse<AlcoholShopResponse> response = useCase.findAll(page, size, sortBy);
        return ResponseEntity.ok(response);
    }
}
```

---

## Database Schema

### Migration: V3__create_users_table.sql
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_username ON users(username);

COMMENT ON TABLE users IS 'Application users for authentication';
COMMENT ON COLUMN users.id IS 'Unique user identifier (UUID)';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password';
COMMENT ON COLUMN users.role IS 'User role: USER or ADMIN';
COMMENT ON COLUMN users.created_at IS 'Account creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Last update timestamp';
```

### Migration: V4__seed_admin_user.sql
```sql
-- Password: Admin123!
-- BCrypt hash generated with work factor 10
INSERT INTO users (
    id,
    username,
    password,
    role,
    created_at,
    updated_at
) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhW', -- BCrypt("Admin123!")
    'ADMIN',
    '2025-02-14 00:00:00',
    '2025-02-14 00:00:00'
);

COMMENT ON COLUMN users.password IS 'For admin: Admin123!';
```

---

## Configuration

### application.yml Additions
```yaml
# Security Configuration
security:
  jwt:
    secret: ${JWT_SECRET:your-super-secret-jwt-key-change-this-in-production-at-least-256-bits}
    access-token-expiration: 900  # 15 minutes in seconds
    refresh-token-expiration: 604800  # 7 days in seconds

# Logging for security events
logging:
  level:
    com.alcoradar.alcoholshop.interfaces.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Environment Variables
| Variable | Description | Default |
|-----------|-------------|---------|
| JWT_SECRET | Secret key for JWT signing | **CHANGE IN PRODUCTION** |
| DB_URL | Database connection string | jdbc:postgresql://localhost:5432/alcoholshop |
| DB_USERNAME | Database user | postgres |
| DB_PASSWORD | Database password | postgres |
| SERVER_PORT | Application port | 8080 |

---

## Data Flows

### Login Flow
```
1. Client: POST /api/auth/login
   { "username": "admin", "password": "Admin123!" }

2. AuthenticationController.login()

3. AuthenticationUseCase.login()
   → UserRepository.findByUsername("admin")
   → SecurityService.checkPassword(rawPassword, hashedPassword)
   → SecurityService.generateAccessToken(user, 15min)
   → SecurityService.generateRefreshToken(user, 7days)

4. Return LoginResponse
   { "accessToken": "...", "refreshToken": "...", "user": {...} }
```

### Protected Request Flow
```
1. Client: POST /api/shops
   Headers: Authorization: Bearer eyJhbGci...

2. AuthenticationAspect intercepts (@RequireAuth)
   → Extract token from Authorization header
   → SecurityService.validateAccessToken(token)
   → SecurityService.getUserIdFromToken(claims)
   → SecurityService.getRoleFromToken(claims)
   → Check role matches @RequireAuth(roles={USER, ADMIN})
   → Set request attributes: userId, userRole

3. AlcoholShopController.create() executes

4. Return 201 Created
```

### Public Request Flow
```
1. Client: GET /api/shops/123

2. AlcoholShopController.findById()
   → No @RequireAuth annotation
   → No authentication aspect interception

3. AlcoholShopUseCase.findById(id)

4. Return shop data
```

---

## Error Handling

### Exception Hierarchy
```
DomainException (abstract)
├── AuthenticationException (abstract)
│   ├── InvalidCredentialsException → 401 Unauthorized
│   ├── InvalidTokenException → 401 Unauthorized
│   ├── ExpiredTokenException → 401 Unauthorized
│   ├── AccessDeniedException → 403 Forbidden
│   └── UserNotFoundException → 404 Not Found
└── ValidationException
    └── UsernameAlreadyExistsException → 409 Conflict
```

### Error Responses

#### 401 Unauthorized
```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: Required role: ADMIN, User role: USER"
}
```

#### 409 Conflict
```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username already exists: admin"
}
```

### Global Exception Handler Extensions
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Existing handlers...

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }
}
```

---

## Testing Strategy

### Unit Tests (Target: 90%+ Domain Coverage)

#### Domain Layer
- `UserTest.java` - 10 tests
- `RoleTest.java` - 2 tests
- Exception tests

#### Application Layer
- `AuthenticationUseCaseTest.java` - 15 tests
- `SecurityServiceTest.java` - 12 tests

### Integration Tests (Target: 85%+ Coverage)

#### Security Integration Tests
- `AuthenticationControllerIntegrationTest.java` - 8 tests
- `UserControllerIntegrationTest.java` - 6 tests
- `AlcoholShopControllerSecurityIntegrationTest.java` - 4 tests

#### Test Coverage Targets
| Layer | Target |
|-------|--------|
| Domain | 90%+ |
| Application | 85%+ |
| Infrastructure | 80%+ |
| Interfaces | 75%+ |
| **Overall** | **82%+** |

---

## Security Considerations

### Best Practices Implemented
1. **BCrypt hashing** with work factor 10 for passwords
2. **Short-lived access tokens** (15 minutes) reduce exposure
3. **Separate refresh tokens** with longer expiration (7 days)
4. **JWT signed with HMAC** using secret key
5. **Role-based access control** with explicit annotations
6. **Audit logging** for security events
7. **No password plaintext storage** ever
8. **Complex password requirements** enforced

### Production Deployment Checklist
- [ ] Change JWT_SECRET to strong random key (256+ bits)
- [ ] Enable HTTPS/TLS for all endpoints
- [ ] Set secure cookie flags if using cookies
- [ ] Implement rate limiting on login endpoint
- [ ] Configure CORS properly for frontend
- [ ] Enable Spring Security headers (X-Frame-Options, etc.)
- [ ] Set up log aggregation for security events
- [ ] Regular security audits and penetration testing
- [ ] Implement account lockout after failed login attempts
- [ ] Consider multi-factor authentication for admin users

---

## Dependencies to Add

### pom.xml Additions
```xml
<!-- JWT (JJWT) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- BCrypt for password hashing -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.6.0</version>
</dependency>

<!-- Spring AOP (already included with spring-boot-starter-web) -->
```

---

## Implementation Phases

### Phase 1: Domain & Security Infrastructure (3-4 hours)
1. Create Role enum
2. Create User domain entity
3. Create UserRepository port
4. Create security domain exceptions
5. Add JWT and BCrypt dependencies
6. Implement SecurityService with JWT generation/validation
7. Unit tests for domain layer
8. Unit tests for SecurityService

### Phase 2: Database & Infrastructure (2-3 hours)
1. Create V3__create_users_table.sql migration
2. Create V4__seed_admin_user.sql migration
3. Create UserEntity JPA entity
4. Create SpringDataUserRepository
5. Implement UserRepositoryImpl
6. Integration tests for repository layer

### Phase 3: Application Layer (2-3 hours)
1. Create authentication DTOs
2. Implement AuthenticationUseCase
3. Unit tests for AuthenticationUseCase
4. Add password validation logic

### Phase 4: Interfaces Layer (2-3 hours)
1. Create @RequireAuth annotation
2. Implement AuthenticationAspect
3. Create AuthenticationController
4. Create UserController
5. Update AlcoholShopController with @RequireAuth
6. Extend GlobalExceptionHandler for security exceptions

### Phase 5: Testing & Documentation (1-2 hours)
1. Write integration tests for all controllers
2. Write security-specific integration tests
3. Update README.md with authentication examples
4. Update Swagger documentation with security schemes
5. Verify test coverage targets met

### Phase 6: Final Testing & Verification (1 hour)
1. Manual testing of all authentication flows
2. Token expiration testing
3. Role-based access testing
4. Error response validation
5. Performance testing of JWT validation

**Total Estimated Time: 11-16 hours**

---

## API Documentation Updates

### Swagger Security Scheme
```java
@OpenAPIDefinition(
    info = @Info(title = "AlcoholShop API", version = "1.0"),
    security = @SecurityRequirement(name = "Bearer Authentication")
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
```

### API Examples

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'
```

#### Create Shop (Authenticated)
```bash
curl -X POST http://localhost:8080/api/shops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "name": "New Shop",
    "address": "Test Address",
    ...
  }'
```

---

## Glossary

| Term | Definition |
|------|------------|
| **JWT** | JSON Web Token - compact, URL-safe means of representing claims |
| **Access Token** | Short-lived JWT (15 min) for API authentication |
| **Refresh Token** | Long-lived JWT (7 days) for obtaining new access tokens |
| **BCrypt** | Password hashing algorithm with work factor |
| **RBAC** | Role-Based Access Control - authorization based on user roles |
| **Aspect** | Spring AOP construct for intercepting method calls |
| **@RequireAuth** | Custom annotation marking methods requiring authentication |

---

## References

- [Spring Boot 3.3 Documentation](https://docs.spring.io/spring-boot/docs/3.3.6/reference/html/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

---

**Document Version:** 1.0
**Last Updated:** 2025-02-14
**Status:** Approved and ready for implementation planning
