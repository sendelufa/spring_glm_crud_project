package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.model.ShopType;
import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для AlcoholShopRepositoryImpl
 *
 * <p>Использует Testcontainers для запуска реальной PostgreSQL базы данных
 * в Docker контейнере. Это гарантирует, что repository работает корректно
 * с реальной базой данных, а не с H2 in-memory.</p>
 *
 * <p>Тестируется полный цикл CRUD операций с конвертацией между
 * Domain Entity и JPA Entity.</p>
 */
@SpringBootTest
@Testcontainers
class AlcoholShopRepositoryIntegrationTest {

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
    private AlcoholShopRepositoryImpl repository;

    @Test
    @DisplayName("Должен сохранить алкомаркет и найти по ID")
    void shouldSaveAndFindById() {
        // Given: создаём доменную сущность через factory method
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("10:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет №1",
            "Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // When: сохраняем в базу данных
        AlcoholShop savedShop = repository.save(shop);

        // Then: проверяем, что сущность сохранена с присвоенным ID
        assertThat(savedShop).isNotNull();
        assertThat(savedShop.getId()).isNotNull();
        assertThat(savedShop.getName()).isEqualTo("Алкомаркет №1");
        assertThat(savedShop.getAddress()).isEqualTo("Москва, ул. Тверская, д. 1");
        assertThat(savedShop.getShopType()).isEqualTo(ShopType.SUPERMARKET);
        assertThat(savedShop.getCoordinates()).isNotNull();
        assertThat(savedShop.getCoordinates().getLatitude()).isEqualTo(55.7558);
        assertThat(savedShop.getCoordinates().getLongitude()).isEqualTo(37.6173);
        assertThat(savedShop.getPhoneNumber()).isNotNull();
        assertThat(savedShop.getPhoneNumber().getValue()).isEqualTo("+74951234567");
        assertThat(savedShop.getWorkingHours()).isNotNull();
        assertThat(savedShop.getWorkingHours().getValue()).isEqualTo("10:00-22:00");
        assertThat(savedShop.getCreatedAt()).isNotNull();

        // When: ищем по ID
        UUID savedId = savedShop.getId();
        Optional<AlcoholShop> foundShop = repository.findById(savedId);

        // Then: проверяем, что найденный магазин совпадает с сохранённым
        assertThat(foundShop).isPresent();
        assertThat(foundShop.get().getId()).isEqualTo(savedId);
        assertThat(foundShop.get().getName()).isEqualTo("Алкомаркет №1");
        assertThat(foundShop.get().getAddress()).isEqualTo("Москва, ул. Тверская, д. 1");
        assertThat(foundShop.get().getShopType()).isEqualTo(ShopType.SUPERMARKET);
        assertThat(foundShop.get().getCoordinates()).isNotNull();
        assertThat(foundShop.get().getCoordinates().getLatitude()).isEqualTo(55.7558);
        assertThat(foundShop.get().getCoordinates().getLongitude()).isEqualTo(37.6173);
        assertThat(foundShop.get().getPhoneNumber()).isNotNull();
        assertThat(foundShop.get().getPhoneNumber().getValue()).isEqualTo("+74951234567");
        assertThat(foundShop.get().getWorkingHours()).isNotNull();
        assertThat(foundShop.get().getWorkingHours().getValue()).isEqualTo("10:00-22:00");
        assertThat(foundShop.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен вернуть Optional.empty для несуществующего ID")
    void shouldReturnEmptyForNonExistentId() {
        // Given: несуществующий UUID
        UUID nonExistentId = UUID.randomUUID();

        // When: ищем по несуществующему ID
        Optional<AlcoholShop> foundShop = repository.findById(nonExistentId);

        // Then: получаем пустой Optional
        assertThat(foundShop).isEmpty();
    }

    @Test
    @DisplayName("Должен найти все сохранённые алкомаркеты")
    void shouldFindAllSavedShops() {
        // Given: создаём и сохраняем несколько алкомаркетов
        Coordinates coords1 = new Coordinates(55.7558, 37.6173);
        PhoneNumber phone1 = new PhoneNumber("+74951234567");
        WorkingHours hours1 = new WorkingHours("10:00-22:00");

        AlcoholShop shop1 = AlcoholShop.create(
            "Алкомаркет №1",
            "Москва, ул. Тверская, д. 1",
            coords1,
            phone1,
            hours1,
            ShopType.SUPERMARKET
        );

        Coordinates coords2 = new Coordinates(59.9343, 30.3351);
        PhoneNumber phone2 = new PhoneNumber("+78121234567");
        WorkingHours hours2 = new WorkingHours("09:00-21:00");

        AlcoholShop shop2 = AlcoholShop.create(
            "Алкомаркет №2",
            "Санкт-Петербург, Невский пр., д. 1",
            coords2,
            phone2,
            hours2,
            ShopType.SPECIALTY
        );

        repository.save(shop1);
        repository.save(shop2);

        // When: получаем все алкомаркеты
        List<AlcoholShop> allShops = repository.findAll();

        // Then: проверяем, что найдены оба сохранённых магазина
        assertThat(allShops).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allShops)
            .extracting(AlcoholShop::getName)
            .contains("Алкомаркет №1", "Алкомаркет №2");
    }

    @Test
    @DisplayName("Должен проверить существование алкомаркета по ID")
    void shouldCheckExistenceById() {
        // Given: создаём и сохраняем алкомаркет
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("10:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет для проверки существования",
            "Москва, ул. Арбат, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        AlcoholShop savedShop = repository.save(shop);
        UUID existingId = savedShop.getId();
        UUID nonExistentId = UUID.randomUUID();

        // When: проверяем существование
        boolean existsForExistingId = repository.existsById(existingId);
        boolean existsForNonExistentId = repository.existsById(nonExistentId);

        // Then: проверяем результаты
        assertThat(existsForExistingId).isTrue();
        assertThat(existsForNonExistentId).isFalse();
    }

    @Test
    @DisplayName("Должен обновить существующий алкомаркет")
    void shouldUpdateExistingShop() {
        // Given: создаём и сохраняем алкомаркет
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("10:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет для обновления",
            "Москва, ул. Ленинградский пр., д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        AlcoholShop savedShop = repository.save(shop);
        UUID savedId = savedShop.getId();

        // When: обновляем название и сохраняем
        savedShop.updateName("Обновлённый Алкомаркет");
        AlcoholShop updatedShop = repository.save(savedShop);

        // Then: проверяем, что название обновилось
        assertThat(updatedShop.getId()).isEqualTo(savedId);
        assertThat(updatedShop.getName()).isEqualTo("Обновлённый Алкомаркет");
        assertThat(updatedShop.getAddress()).isEqualTo("Москва, ул. Ленинградский пр., д. 1");

        // Проверяем через findById
        Optional<AlcoholShop> foundShop = repository.findById(savedId);
        assertThat(foundShop).isPresent();
        assertThat(foundShop.get().getName()).isEqualTo("Обновлённый Алкомаркет");
    }

    @Test
    @DisplayName("Должен удалить алкомаркет по ID")
    void shouldDeleteById() {
        // Given: создаём и сохраняем алкомаркет
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("10:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет для удаления",
            "Москва, ул. Садовая, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.DUTY_FREE
        );

        AlcoholShop savedShop = repository.save(shop);
        UUID savedId = savedShop.getId();

        // When: удаляем по ID
        repository.deleteById(savedId);

        // Then: проверяем, что алкомаркет удалён
        Optional<AlcoholShop> foundShop = repository.findById(savedId);
        assertThat(foundShop).isEmpty();

        // Проверяем через existsById
        boolean exists = repository.existsById(savedId);
        assertThat(exists).isFalse();
    }
}
