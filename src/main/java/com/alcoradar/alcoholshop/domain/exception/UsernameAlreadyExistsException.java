package com.alcoradar.alcoholshop.domain.exception;

public class UsernameAlreadyExistsException extends DomainException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("Username already exists: %s", username));
    }
}
