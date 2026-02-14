package com.alcoradar.alcoholshop.application.service;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.exception.ExpiredTokenException;
import com.alcoradar.alcoholshop.domain.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class SecurityService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    public String generateAccessToken(User user, int expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (long) expirationSeconds * 1000);

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
        Date expiry = new Date(now.getTime() + (long) expirationSeconds * 1000);

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
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            if (!"access".equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new InvalidTokenException("Token is not an access token");
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

            if (!"refresh".equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new InvalidTokenException("Token is not a refresh token");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Refresh token expired. Please login again");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or malformed JWT token: " + e.getMessage());
        }
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
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
