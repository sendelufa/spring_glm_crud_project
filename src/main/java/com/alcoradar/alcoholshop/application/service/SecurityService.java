package com.alcoradar.alcoholshop.application.service;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.exception.ExpiredTokenException;
import com.alcoradar.alcoholshop.domain.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Security service for JWT token generation/validation and BCrypt password hashing.
 *
 * <p>Provides:
 * <ul>
 *   <li>JWT access token generation with user claims (userId, username, role)</li>
 *   <li>JWT refresh token generation (without role claim for security)</li>
 *   <li>Token validation with type checking (access vs refresh)</li>
 *   <li>BCrypt password hashing with configurable work factor</li>
 *   <li>Password verification</li>
 * </ul>
 *
 * @throws InvalidTokenException for invalid or malformed tokens
 * @throws ExpiredTokenException for expired tokens
 */
@Slf4j
@Service
public class SecurityService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.password.bcrypt-strength:10}")
    private int bcryptStrength;
    private BCryptPasswordEncoder passwordEncoder;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 characters)");
        }
        this.passwordEncoder = new BCryptPasswordEncoder(bcryptStrength);
    }

    public String generateAccessToken(User user, int expirationSeconds) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null || user.getUsername() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User must have id, username, and role");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Expiration seconds must be positive");
        }

        Date[] dates = createExpirationDates(expirationSeconds);
        Date now = dates[0];
        Date expiry = dates[1];

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
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User must have id and username");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Expiration seconds must be positive");
        }

        Date[] dates = createExpirationDates(expirationSeconds);
        Date now = dates[0];
        Date expiry = dates[1];

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_USER_ID, user.getId().toString())
            .claim(CLAIM_TYPE, "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    private Date[] createExpirationDates(int expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (long) expirationSeconds * 1000);
        return new Date[]{now, expiry};
    }

    public Claims validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String tokenType = claims.get(CLAIM_TYPE, String.class);
            if (tokenType == null || !"access".equals(tokenType)) {
                throw new InvalidTokenException("Token is missing or has invalid type claim (expected 'access')");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Access token expired. Please refresh your token");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or malformed JWT token: " + e.getMessage());
        }
    }

    public Claims validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String tokenType = claims.get(CLAIM_TYPE, String.class);
            if (tokenType == null || !"refresh".equals(tokenType)) {
                throw new InvalidTokenException("Token is missing or has invalid type claim (expected 'refresh')");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Refresh token expired. Please login again");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or malformed JWT token: " + e.getMessage());
        }
    }

    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            throw new IllegalArgumentException("Passwords cannot be null");
        }
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public UUID getUserIdFromToken(Claims claims) {
        try {
            String userIdStr = claims.get(CLAIM_USER_ID, String.class);
            if (userIdStr == null) {
                throw new InvalidTokenException("Token missing userId claim");
            }
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid userId in token: " + e.getMessage());
        }
    }

    public Role getRoleFromToken(Claims claims) {
        try {
            String roleStr = claims.get(CLAIM_ROLE, String.class);
            if (roleStr == null) {
                throw new InvalidTokenException("Token missing role claim");
            }
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid role in token: " + e.getMessage());
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
