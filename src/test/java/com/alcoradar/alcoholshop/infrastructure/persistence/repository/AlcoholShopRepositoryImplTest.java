package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.exception.AlcoholShopNotFoundException;
import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.model.ShopType;
import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;
import com.alcoradar.alcoholshop.infrastructure.persistence.entity.AlcoholShopEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тест для AlcoholShopRepositoryImpl
 *
 * <p>Тестирует логику конвертации между Domain Entity и JPA Entity,
 * а также делегирование вызовов в Spring Data JPA.</p>
 *
 * <p>Использует Mockito для мокания Spring Data JPA repository,
 * что позволяет тестировать бизнес-логику без подключения к реальной базе данных.</p>
 */
@ExtendWith(MockitoExtension.class)
class AlcoholShopRepositoryImplTest {

    @Mock
    private SpringDataAlcoholShopRepository springDataRepository;

    @InjectMocks
    private AlcoholShopRepositoryImpl repository;

    @Test
    @DisplayName("Должен сохранить новый алкомаркет через Spring Data JPA")
    void shouldSaveNewShop() {
        // Given: создаём доменную сущность
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

        UUID generatedId = UUID.randomUUID();
        AlcoholShopEntity entityToSave = AlcoholShopEntity.fromDomain(shop);
        entityToSave.setId(generatedId);

        // Мокаем поведение Spring Data JPA
        when(springDataRepository.save(any(AlcoholShopEntity.class)))
            .thenAnswer(invocation -> {
                AlcoholShopEntity entity = invocation.getArgument(0);
                entity.setId(generatedId);
                return entity;
            });

        // When: сохраняем через наш repository
        AlcoholShop savedShop = repository.save(shop);

        // Then: проверяем, что save был вызван с правильной Entity
        verify(springDataRepository, times(1)).save(any(AlcoholShopEntity.class));

        // Проверяем результат
        assertThat(savedShop).isNotNull();
        assertThat(savedShop.getName()).isEqualTo("Алкомаркет №1");
        assertThat(savedShop.getAddress()).isEqualTo("Москва, ул. Тверская, д. 1");
    }

    @Test
    @DisplayName("Должен найти алкомаркет по ID")
    void shouldFindById() {
        // Given: создаём Entity с существующим ID
        UUID existingId = UUID.randomUUID();
        AlcoholShopEntity entity = new AlcoholShopEntity(
            existingId,
            "Алкомаркет для поиска",
            "Москва, ул. Арбат, д. 1",
            55.7558,
            37.6173,
            "+74951234567",
            "10:00-22:00",
            ShopType.SUPERMARKET,
            java.time.LocalDateTime.now()
        );

        when(springDataRepository.findById(existingId))
            .thenReturn(Optional.of(entity));

        // When: ищем по ID
        Optional<AlcoholShop> foundShop = repository.findById(existingId);

        // Then: проверяем результат
        assertThat(foundShop).isPresent();
        assertThat(foundShop.get().getId()).isEqualTo(existingId);
        assertThat(foundShop.get().getName()).isEqualTo("Алкомаркет для поиска");

        verify(springDataRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("Должен вернуть Optional.empty если алкомаркет не найден")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: несуществующий ID
        UUID nonExistentId = UUID.randomUUID();

        when(springDataRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        // When: ищем по несуществующему ID
        Optional<AlcoholShop> foundShop = repository.findById(nonExistentId);

        // Then: получаем пустой Optional
        assertThat(foundShop).isEmpty();

        verify(springDataRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Должен найти все алкомаркеты")
    void shouldFindAll() {
        // Given: создаём список Entity
        AlcoholShopEntity entity1 = new AlcoholShopEntity(
            UUID.randomUUID(),
            "Алкомаркет №1",
            "Москва, ул. Тверская, д. 1",
            55.7558,
            37.6173,
            "+74951234567",
            "10:00-22:00",
            ShopType.SUPERMARKET,
            java.time.LocalDateTime.now()
        );

        AlcoholShopEntity entity2 = new AlcoholShopEntity(
            UUID.randomUUID(),
            "Алкомаркет №2",
            "Санкт-Петербург, Невский пр., д. 1",
            59.9343,
            30.3351,
            "+78121234567",
            "09:00-21:00",
            ShopType.SPECIALTY,
            java.time.LocalDateTime.now()
        );

        when(springDataRepository.findAll())
            .thenReturn(List.of(entity1, entity2));

        // When: получаем все алкомаркеты
        List<AlcoholShop> allShops = repository.findAll();

        // Then: проверяем результат
        assertThat(allShops).hasSize(2);
        assertThat(allShops)
            .extracting(AlcoholShop::getName)
            .containsExactly("Алкомаркет №1", "Алкомаркет №2");

        verify(springDataRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен проверить существование алкомаркета по ID")
    void shouldCheckExistence() {
        // Given: существующий ID
        UUID existingId = UUID.randomUUID();

        when(springDataRepository.existsById(existingId))
            .thenReturn(true);

        // When: проверяем существование
        boolean exists = repository.existsById(existingId);

        // Then: проверяем результат
        assertThat(exists).isTrue();

        verify(springDataRepository, times(1)).existsById(existingId);
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке удалить несуществующий алкомаркет")
    void shouldThrowExceptionWhenDeletingNonExistentShop() {
        // Given: несуществующий ID
        UUID nonExistentId = UUID.randomUUID();

        when(springDataRepository.existsById(nonExistentId))
            .thenReturn(false);

        // When & Then: ожидаем исключение
        assertThatThrownBy(() -> repository.deleteById(nonExistentId))
            .isInstanceOf(AlcoholShopNotFoundException.class)
            .hasMessageContaining("не найден");

        verify(springDataRepository, times(1)).existsById(nonExistentId);
        verify(springDataRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Должен успешно удалить существующий алкомаркет")
    void shouldDeleteExistingShop() {
        // Given: существующий ID
        UUID existingId = UUID.randomUUID();

        when(springDataRepository.existsById(existingId))
            .thenReturn(true);
        doNothing().when(springDataRepository).deleteById(existingId);

        // When: удаляем по ID
        repository.deleteById(existingId);

        // Then: проверяем, что deleteById был вызван
        verify(springDataRepository, times(1)).existsById(existingId);
        verify(springDataRepository, times(1)).deleteById(existingId);
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при save с null")
    void shouldThrowExceptionWhenSaveNull() {
        // When & Then: ожидаем исключение
        assertThatThrownBy(() -> repository.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("AlcoholShop cannot be null");

        verify(springDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при findById с null")
    void shouldThrowExceptionWhenFindByIdNull() {
        // When & Then: ожидаем исключение
        assertThatThrownBy(() -> repository.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be null");

        verify(springDataRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при existsById с null")
    void shouldThrowExceptionWhenExistsByIdNull() {
        // When & Then: ожидаем исключение
        assertThatThrownBy(() -> repository.existsById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be null");

        verify(springDataRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при deleteById с null")
    void shouldThrowExceptionWhenDeleteByIdNull() {
        // When & Then: ожидаем исключение
        assertThatThrownBy(() -> repository.deleteById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be null");

        verify(springDataRepository, never()).existsById(any());
        verify(springDataRepository, never()).deleteById(any());
    }
}
