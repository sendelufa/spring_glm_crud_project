package com.alcoradar.alcoholshop.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID id) {
        super(String.format("User not found with id: %s", id));
    }
}
