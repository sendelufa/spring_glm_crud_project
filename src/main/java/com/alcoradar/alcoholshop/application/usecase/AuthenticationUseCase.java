package com.alcoradar.alcoholshop.application.usecase;

import com.alcoradar.alcoholshop.application.dto.*;
import com.alcoradar.alcoholshop.domain.exception.*;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.application.service.SecurityService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for authentication operations.
 * Handles login, token refresh, and user creation following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationUseCase {

    private final UserRepository userRepository;
    private final SecurityService securityService;

    /**
     * Authenticate a user with username and password.
     *
     * @param request the login request containing username and password
     * @return LoginResponse containing access token, refresh token, and user info
     * @throws InvalidCredentialsException if username doesn't exist or password doesn't match
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!securityService.checkPassword(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = securityService.generateAccessToken(user, 900);
        String refreshToken = securityService.generateRefreshToken(user, 604800);

        return LoginResponse.of(accessToken, refreshToken, UserResponse.from(user));
    }

    /**
     * Refresh an access token using a valid refresh token.
     *
     * @param request the refresh request containing the refresh token
     * @return RefreshResponse containing the new access token
     * @throws InvalidTokenException if token is invalid or user not found
     * @throws ExpiredTokenException if token has expired
     */
    public RefreshResponse refreshToken(RefreshRequest request) {
        Claims claims = securityService.validateRefreshToken(request.refreshToken());
        UUID userId = securityService.getUserIdFromToken(claims);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("User not found for token"));

        String newAccessToken = securityService.generateAccessToken(user, 900);

        return RefreshResponse.of(newAccessToken);
    }

    /**
     * Create a new user with hashed password.
     *
     * @param request the create user request containing username, password, and role
     * @return UserResponse containing the created user info
     * @throws UsernameAlreadyExistsException if username already exists
     */
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        String hashedPassword = securityService.hashPassword(request.password());

        User user = User.create(
            request.username(),
            hashedPassword,
            request.role()
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}
