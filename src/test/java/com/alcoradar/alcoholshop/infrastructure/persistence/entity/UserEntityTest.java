package com.alcoradar.alcoholshop.infrastructure.persistence.entity;

import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserEntityTest {

    @Test
    void shouldConvertToDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername("testuser");
        entity.setPassword("$2a$10$hashed");
        entity.setRole(Role.USER);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        User domain = entity.toDomain();

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUsername()).isEqualTo("testuser");
        assertThat(domain.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(domain.getRole()).isEqualTo(Role.USER);
        assertThat(domain.getCreatedAt()).isEqualTo(now);
        assertThat(domain.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldConvertFromDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User domain = User.builder()
            .id(id)
            .username("testuser")
            .passwordHash("$2a$10$hashed")
            .role(Role.ADMIN)
            .createdAt(now)
            .updatedAt(now)
            .build();

        UserEntity entity = UserEntity.fromDomain(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(entity.getRole()).isEqualTo(Role.ADMIN);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetTimestampsOnPersist() {
        UserEntity entity = new UserEntity();
        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() {
        UserEntity entity = new UserEntity();
        entity.onCreate();
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

        try {
            Thread.sleep(10); // Small delay to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entity.onUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);
    }
}
