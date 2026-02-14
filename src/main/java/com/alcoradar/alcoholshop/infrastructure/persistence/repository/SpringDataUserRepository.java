package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserEntity persistence.
 * <p>
 * Provides standard CRUD operations through JpaRepository and custom query methods
 * for user-specific operations such as authentication and registration validation.
 * </p>
 * <p>
 * Spring Data automatically generates implementations for query methods derived from
 * method names, eliminating the need for manual SQL for common operations.
 * </p>
 * <p>
 * Key operations:
 * <ul>
 *   <li>Standard CRUD: save, findById, findAll, delete, etc.</li>
 *   <li>findByUsername: Retrieves user by username for authentication</li>
 *   <li>existsByUsername: Checks username uniqueness for registration</li>
 * </ul>
 * </p>
 *
 * @see UserEntity
 * @see JpaRepository
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find a user by their unique username.
     * <p>
     * Used primarily during authentication to load user details including
     * the password hash for verification against the provided password.
     * </p>
     *
     * @param username the unique username to search for
     * @return Optional containing the UserEntity if found, empty otherwise
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Check if a user exists with the given username.
     * <p>
     * Used during registration to validate username uniqueness before
     * attempting to create a new user account.
     * </p>
     *
     * @param username the username to check for existence
     * @return true if a user with this username exists, false otherwise
     */
    boolean existsByUsername(String username);
}
