package com.alcoradar.alcoholshop.application.service;

import com.alcoradar.alcoholshop.domain.exception.InvalidTokenException;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
        ReflectionTestUtils.setField(securityService, "jwtSecret", "test-secret-key-for-testing-at-least-256-bits-long-enough-for-hmac-sha256");
    }

    @Test
    void shouldGenerateAccessTokenWithClaims() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .role(Role.USER)
            .build();

        String token = securityService.generateAccessToken(user, 900);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        Claims claims = securityService.validateAccessToken(token);
        assertThat(claims.get("userId", String.class)).isEqualTo(user.getId().toString());
        assertThat(claims.get("username", String.class)).isEqualTo("testuser");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
    }

    @Test
    void shouldGenerateRefreshTokenWithClaims() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .role(Role.USER)
            .build();

        String token = securityService.generateRefreshToken(user, 604800);

        assertThat(token).isNotNull();

        Claims claims = securityService.validateRefreshToken(token);
        assertThat(claims.get("userId", String.class)).isEqualTo(user.getId().toString());
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        assertThat(claims.get("role")).isNull(); // Refresh token doesn't have role
    }

    @Test
    void shouldValidateValidAccessToken() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("admin")
            .role(Role.ADMIN)
            .build();

        String token = securityService.generateAccessToken(user, 900);

        Claims claims = securityService.validateAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
    }

    @Test
    void shouldThrowExceptionWhenValidatingRefreshTokenAsAccess() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .role(Role.USER)
            .build();

        String refreshToken = securityService.generateRefreshToken(user, 604800);

        assertThatThrownBy(() -> securityService.validateAccessToken(refreshToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("not an access token");
    }

    @Test
    void shouldThrowExceptionWhenValidatingAccessTokenAsRefresh() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .role(Role.USER)
            .build();

        String accessToken = securityService.generateAccessToken(user, 900);

        assertThatThrownBy(() -> securityService.validateRefreshToken(accessToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("not a refresh token");
    }

    @Test
    void shouldHashPassword() {
        String rawPassword = "TestPassword123!";
        String hashedPassword = securityService.hashPassword(rawPassword);

        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(rawPassword);
        assertThat(hashedPassword).startsWith("$2a$"); // BCrypt hash prefix
    }

    @Test
    void shouldCheckPasswordCorrectly() {
        String rawPassword = "TestPassword123!";
        String hashedPassword = securityService.hashPassword(rawPassword);

        boolean matches = securityService.checkPassword(rawPassword, hashedPassword);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldReturnFalseForWrongPassword() {
        String rawPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = securityService.hashPassword(rawPassword);

        boolean matches = securityService.checkPassword(wrongPassword, hashedPassword);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldExtractUserIdFromClaims() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .role(Role.USER)
            .build();

        String token = securityService.generateAccessToken(user, 900);
        Claims claims = securityService.validateAccessToken(token);

        UUID userId = securityService.getUserIdFromToken(claims);

        assertThat(userId).isEqualTo(user.getId());
    }

    @Test
    void shouldExtractRoleFromClaims() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .role(Role.ADMIN)
            .build();

        String token = securityService.generateAccessToken(user, 900);
        Claims claims = securityService.validateAccessToken(token);

        Role role = securityService.getRoleFromToken(claims);

        assertThat(role).isEqualTo(Role.ADMIN);
    }
}
