package com.alcoradar.alcoholshop.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

class UserTest {

    @Test
    void shouldCreateUserWithFactoryMethod() {
        User user = User.create("testuser", "$2a$10$hashed", Role.USER);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUsername() {
        User user = User.create("testuser", "$2a$10$hashed", Role.USER);
        user.updateInformation("newusername");

        assertThat(user.getUsername()).isEqualTo("newusername");
        assertThat(user.getUpdatedAt()).isAfter(user.getCreatedAt());
    }

    @Test
    void shouldChangePassword() {
        User user = User.create("testuser", "$2a$10$old", Role.USER);
        user.changePassword("$2a$10$new");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$new");
        assertThat(user.getUpdatedAt()).isAfter(user.getCreatedAt());
    }

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        User user1 = User.builder().id(id).username("test").role(Role.USER).build();
        User user2 = User.builder().id(id).username("test").role(Role.USER).build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        User user1 = User.create("test1", "$2a$10$hash1", Role.USER);
        User user2 = User.create("test2", "$2a$10$hash2", Role.USER);

        assertThat(user1).isNotEqualTo(user2);
    }
}
