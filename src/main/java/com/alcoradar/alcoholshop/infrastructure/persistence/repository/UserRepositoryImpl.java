package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.model.User;
import com.alcoradar.alcoholshop.domain.repository.UserRepository;
import com.alcoradar.alcoholshop.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserRepository port using Spring Data JPA.
 * <p>
 * This class serves as the adapter between the domain layer (which defines
 * the UserRepository interface) and the infrastructure layer (which provides
 * the actual persistence mechanism through Spring Data JPA).
 * </p>
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Convert domain User model to JPA UserEntity for persistence</li>
 *   <li>Convert JPA UserEntity back to domain User model after retrieval</li>
 *   <li>Delegate actual database operations to SpringDataUserRepository</li>
 *   <li>Ensure clean separation between domain and infrastructure layers</li>
 * </ul>
 * </p>
 * <p>
 * Uses Lombok's @RequiredArgsConstructor for constructor-based dependency injection,
 * which is the recommended approach in Spring Boot 3.x.
 * </p>
 *
 * @see UserRepository
 * @see SpringDataUserRepository
 * @see UserEntity
 * @see User
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = springDataUserRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username)
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
            .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }
}
