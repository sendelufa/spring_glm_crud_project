package com.alcoradar.alcoholshop.domain.exception;

/**
 * Base class for all domain-specific exceptions in the AlcoholShop application.
 * <p>
 * This exception hierarchy provides type-safe error handling for domain layer errors,
 * enabling precise exception handling in controllers and services.
 * <p>
 * All domain exceptions should extend this class to maintain consistent error handling
 * across the application and allow for granular catch blocks in upper layers.
 *
 * @author AlcoRadar Team
 * @since 1.0
 */
public class DomainException extends RuntimeException {

    /**
     * Constructs a new domain exception with the specified detail message.
     *
     * @param message the detail message explaining the domain error
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Constructs a new domain exception with the specified detail message and cause.
     * <p>
     * Use this constructor when wrapping lower-level exceptions or when the domain
     * error is caused by another underlying exception.
     *
     * @param message the detail message explaining the domain error
     * @param cause   the cause of the domain error
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
