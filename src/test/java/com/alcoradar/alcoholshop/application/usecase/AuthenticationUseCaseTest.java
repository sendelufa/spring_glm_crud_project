package com.alcoradar.alcoholshop.application.usecase;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.domain.exception.*;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.application.service.SecurityService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    private AuthenticationUseCase authenticationUseCase;

    @BeforeEach
    void setUp() {
        authenticationUseCase = new AuthenticationUseCase(userRepository, securityService);
    }

    @Test
    void shouldLoginUser() {
        String rawPassword = "rawPassword";
        String hashedPassword = "$2a$10$hash";

        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .passwordHash(hashedPassword)
            .role(Role.USER)
            .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(securityService.checkPassword(rawPassword, hashedPassword)).thenReturn(true);
        when(securityService.generateAccessToken(user, 900)).thenReturn("access-token");
        when(securityService.generateRefreshToken(user, 604800)).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest("testuser", rawPassword);
        LoginResponse response = authenticationUseCase.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().username()).isEqualTo("testuser");
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        String rawPassword = "wrongPassword";

        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .passwordHash("$2a$10$hash")
            .role(Role.USER)
            .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(securityService.checkPassword(rawPassword, "$2a$10$hash")).thenReturn(false);

        LoginRequest request = new LoginRequest("testuser", rawPassword);

        assertThatThrownBy(() -> authenticationUseCase.login(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent", "password");

        assertThatThrownBy(() -> authenticationUseCase.login(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRefreshAccessToken() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .role(Role.USER)
            .build();

        Claims mockClaims = mock(Claims.class);

        when(securityService.validateRefreshToken("old-refresh")).thenReturn(mockClaims);
        when(securityService.getUserIdFromToken(mockClaims)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(securityService.generateAccessToken(user, 900)).thenReturn("new-access");

        RefreshRequest request = new RefreshRequest("old-refresh");
        RefreshResponse response = authenticationUseCase.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
    }

    @Test
    void shouldThrowExceptionWhenRefreshWithInvalidToken() {
        when(securityService.validateRefreshToken("invalid-token"))
            .thenThrow(new InvalidTokenException("Invalid token"));

        RefreshRequest request = new RefreshRequest("invalid-token");

        assertThatThrownBy(() -> authenticationUseCase.refreshToken(request))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldThrowExceptionWhenRefreshWithExpiredToken() {
        when(securityService.validateRefreshToken("expired-token"))
            .thenThrow(new ExpiredTokenException("Token expired"));

        RefreshRequest request = new RefreshRequest("expired-token");

        assertThatThrownBy(() -> authenticationUseCase.refreshToken(request))
            .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    void shouldCreateUser() {
        String rawPassword = "NewPassword123!";
        String hashedPassword = "$2a$10$hash";

        User savedUser = User.create("newuser", hashedPassword, Role.USER);

        when(securityService.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(savedUser);

        CreateUserRequest request = new CreateUserRequest("newuser", rawPassword, Role.USER);

        UserResponse response = authenticationUseCase.createUser(request);

        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithExistingUsername() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest("existing", "NewPassword123!", Role.USER);

        assertThatThrownBy(() -> authenticationUseCase.createUser(request))
            .isInstanceOf(UsernameAlreadyExistsException.class);
    }
}
