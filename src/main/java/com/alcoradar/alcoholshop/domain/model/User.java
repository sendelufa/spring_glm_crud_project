package com.alcoradar.alcoholshop.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static User create(String username, String passwordHash, Role role) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
            .id(UUID.randomUUID())
            .username(username)
            .passwordHash(passwordHash)
            .role(role)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public void updateInformation(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
