package com.alcoradar.alcoholshop.infrastructure.persistence.entity;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for persistence of User domain model.
 * <p>
 * Maps the User aggregate to the "users" table in the database.
 * Provides bi-directional conversion between JPA entity and domain model.
 * </p>
 * <p>
 * Timestamps are automatically managed through @PrePersist and @PreUpdate lifecycle callbacks.
 * </p>
 *
 * @see User
 * @see Role
 */
@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_username", columnList = "username"))
public class UserEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA.
     * Should not be used in business logic.
     */
    protected UserEntity() {
    }

    /**
     * Lifecycle callback before entity persistence.
     * Sets initial timestamp values for both created_at and updated_at.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback before entity update.
     * Updates the updated_at timestamp while preserving created_at.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Converts JPA entity to domain model.
     * Maps the password field to passwordHash in the domain model.
     *
     * @return domain User instance
     */
    public User toDomain() {
        return User.builder()
            .id(id)
            .username(username)
            .passwordHash(password)
            .role(role)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    /**
     * Static factory method to create JPA entity from domain model.
     * Maps the passwordHash field to password in the entity.
     *
     * @param user domain User instance
     * @return JPA UserEntity instance
     * @throws IllegalArgumentException if user is null
     */
    public static UserEntity fromDomain(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Domain entity cannot be null");
        }

        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPasswordHash());
        entity.setRole(user.getRole());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserEntity{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", role=" + role +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
