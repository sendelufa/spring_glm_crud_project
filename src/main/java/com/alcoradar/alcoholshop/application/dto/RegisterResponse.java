package com.alcoradar.alcoholshop.application.dto;

import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for user registration.
 */
@AllArgsConstructor
public class RegisterResponse {

    private final UUID id;
    private final String username;
    private final String role;
}
