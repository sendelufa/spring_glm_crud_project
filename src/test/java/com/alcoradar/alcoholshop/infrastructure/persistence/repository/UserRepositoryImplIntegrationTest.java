package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for UserRepositoryImpl.
 * <p>
 * Uses Testcontainers to run a real PostgreSQL database in a Docker container.
 * This ensures that the repository works correctly with a real database,
 * not just with H2 in-memory.
 * </p>
 * <p>
 * Tests the full CRUD cycle with conversion between Domain Entity and JPA Entity.
 * </p>
 */
@SpringBootTest
@Testcontainers
class UserRepositoryImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:16-alpine"
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and assign ID")
    void shouldSaveUser() {
        // Given: create user via factory method
        User user = User.create("testuser", "$2a$10$hashedpassword", Role.USER);

        // When: save to database
        User savedUser = userRepository.save(user);

        // Then: verify user is saved with assigned ID
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given: save a user
        User user = User.create("uniqueuser", "$2a$10$hash", Role.USER);
        userRepository.save(user);

        // When: find by username
        var foundUser = userRepository.findByUsername("uniqueuser");

        // Then: verify user is found
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("uniqueuser");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When: find by non-existent username
        var foundUser = userRepository.findByUsername("nonexistent");

        // Then: should return empty Optional
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Given: save a user
        User user = User.create("idtest", "$2a$10$hash", Role.ADMIN);
        User savedUser = userRepository.save(user);

        // When: find by ID
        var foundUser = userRepository.findById(savedUser.getId());

        // Then: verify user is found
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo("idtest");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should return empty when ID not found")
    void shouldReturnEmptyWhenIdNotFound() {
        // Given: non-existent UUID
        UUID nonExistentId = UUID.randomUUID();

        // When: find by non-existent ID
        var foundUser = userRepository.findById(nonExistentId);

        // Then: should return empty Optional
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckUsernameExists() {
        // Given: save a user
        User user = User.create("exists", "$2a$10$hash", Role.USER);
        userRepository.save(user);

        // When: check existence
        boolean existsForExistingUsername = userRepository.existsByUsername("exists");
        boolean existsForNonExistentUsername = userRepository.existsByUsername("notexists");

        // Then: verify results
        assertThat(existsForExistingUsername).isTrue();
        assertThat(existsForNonExistentUsername).isFalse();
    }

    @Test
    @DisplayName("Should update existing user")
    void shouldUpdateExistingUser() {
        // Given: save a user
        User user = User.create("updatetest", "$2a$10$oldhash", Role.USER);
        User savedUser = userRepository.save(user);
        UUID savedId = savedUser.getId();

        // When: update user information
        savedUser.changePassword("$2a$10$newhash");
        User updatedUser = userRepository.save(savedUser);

        // Then: verify update
        assertThat(updatedUser.getId()).isEqualTo(savedId);
        assertThat(updatedUser.getUsername()).isEqualTo("updatetest");
        assertThat(updatedUser.getPasswordHash()).isEqualTo("$2a$10$newhash");

        // Verify through findById
        var foundUser = userRepository.findById(savedId);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPasswordHash()).isEqualTo("$2a$10$newhash");
    }

    @Test
    @DisplayName("Should save and find user with ADMIN role")
    void shouldSaveAndFindAdminUser() {
        // Given: create admin user
        User admin = User.create("adminuser", "$2a$10$adminhash", Role.ADMIN);

        // When: save and find
        User savedAdmin = userRepository.save(admin);
        var foundAdmin = userRepository.findByUsername("adminuser");

        // Then: verify admin user
        assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getRole()).isEqualTo(Role.ADMIN);
    }
}
