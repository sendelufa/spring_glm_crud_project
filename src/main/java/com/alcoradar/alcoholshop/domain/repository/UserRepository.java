package com.alcoradar.alcoholshop.domain.repository;

import com.alcoradar.alcoholshop.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for User aggregate persistence.
 * This interface defines the contract for user data access operations.
 * Implementations will be provided in the infrastructure layer.
 */
public interface UserRepository {

    /**
     * Save a user entity (create or update).
     *
     * @param user the user entity to save
     * @return the saved user entity
     */
    User save(User user);

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by ID.
     *
     * @param id the user ID to search for
     * @return Optional containing the user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Check if a user exists by username.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);
}
