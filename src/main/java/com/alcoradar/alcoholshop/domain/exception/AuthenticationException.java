package com.alcoradar.alcoholshop.domain.exception;

/**
 * Base class for all authentication-related exceptions in the AlcoholShop application.
 * <p>
 * This exception hierarchy provides type-safe error handling for authentication failures,
 * enabling precise exception handling in controllers and services.
 * <p>
 * All authentication exceptions should extend this class to maintain consistent error handling
 * across the application and allow for granular catch blocks in upper layers.
 * <p>
 * Examples of authentication errors include invalid credentials, expired tokens,
 * insufficient permissions, and authentication service failures.
 *
 * @author AlcoRadar Team
 * @since 1.0
 */
public abstract class AuthenticationException extends DomainException {

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message the detail message explaining the authentication error
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     * <p>
     * Use this constructor when wrapping lower-level exceptions or when the authentication
     * error is caused by another underlying exception.
     *
     * @param message the detail message explaining the authentication error
     * @param cause   the cause of the authentication error
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
