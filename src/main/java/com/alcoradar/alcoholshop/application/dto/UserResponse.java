package com.alcoradar.alcoholshop.application.dto;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    Role role,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
