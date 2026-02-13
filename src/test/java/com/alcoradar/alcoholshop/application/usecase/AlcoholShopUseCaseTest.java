package com.alcoradar.alcoholshop.application.usecase;

import com.alcoradar.alcoholshop.application.dto.AlcoholShopResponse;
import com.alcoradar.alcoholshop.application.dto.CreateAlcoholShopRequest;
import com.alcoradar.alcoholshop.application.dto.CoordinatesDto;
import com.alcoradar.alcoholshop.application.dto.PageResponse;
import com.alcoradar.alcoholshop.domain.exception.AlcoholShopNotFoundException;
import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.model.ShopType;
import com.alcoradar.alcoholshop.domain.repository.AlcoholShopRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link AlcoholShopUseCase}
 * <p>
 * Тестирует бизнес-логику приложения layer с использованием Mockito для mock зависимостей.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit тесты для AlcoholShopUseCase")
class AlcoholShopUseCaseTest {

    @Mock
    private AlcoholShopRepository repository;

    @InjectMocks
    private AlcoholShopUseCase useCase;

    @Nested
    @DisplayName("Метод create()")
    class CreateMethodTests {

        @Test
        @DisplayName("Должен успешно создать алкомаркет")
        void shouldCreateAlcoholShop() {
            // Given
            CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                    "Алкомаркет №1",
                    "г. Москва, ул. Примерная, д. 1",
                    new CoordinatesDto(55.7558, 37.6173),
                    "+74951234567",
                    "09:00-22:00",
                    ShopType.SUPERMARKET
            );

            AlcoholShop domainShop = createTestShop();
            when(repository.save(any(AlcoholShop.class))).thenReturn(domainShop);

            // When
            AlcoholShopResponse response = useCase.create(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(domainShop.getId());
            assertThat(response.name()).isEqualTo("Алкомаркет №1");
            assertThat(response.address()).isEqualTo("г. Москва, ул. Примерная, д. 1");
            assertThat(response.coordinates().latitude()).isEqualTo(55.7558);
            assertThat(response.coordinates().longitude()).isEqualTo(37.6173);
            assertThat(response.phoneNumber()).isEqualTo("+74951234567");
            assertThat(response.workingHours()).isEqualTo("09:00-22:00");
            assertThat(response.shopType()).isEqualTo(ShopType.SUPERMARKET);
            assertThat(response.createdAt()).isNotNull();

            verify(repository, times(1)).save(any(AlcoholShop.class));
        }

        @Test
        @DisplayName("Должен создать алкомаркет с опциональными полями")
        void shouldCreateAlcoholShopWithOptionalFields() {
            // Given
            CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                    "Алкомаркет №2",
                    "г. Санкт-Петербург, Невский пр., д. 1",
                    new CoordinatesDto(59.9343, 30.3351),
                    null,
                    null,
                    null
            );

            AlcoholShop domainShop = AlcoholShop.create(
                    "Алкомаркет №2",
                    "г. Санкт-Петербург, Невский пр., д. 1",
                    new com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates(59.9343, 30.3351),
                    null,
                    null,
                    null
            );

            when(repository.save(any(AlcoholShop.class))).thenReturn(domainShop);

            // When
            AlcoholShopResponse response = useCase.create(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Алкомаркет №2");
            assertThat(response.phoneNumber()).isNull();
            assertThat(response.workingHours()).isNull();
            assertThat(response.shopType()).isNull();

            verify(repository, times(1)).save(any(AlcoholShop.class));
        }
    }

    @Nested
    @DisplayName("Метод findById()")
    class FindByIdMethodTests {

        @Test
        @DisplayName("Должен найти алкомаркет по ID")
        void shouldFindAlcoholShopById() {
            // Given
            UUID shopId = UUID.randomUUID();
            AlcoholShop domainShop = createTestShop();
            domainShop.setId(shopId);

            when(repository.findById(shopId)).thenReturn(Optional.of(domainShop));

            // When
            AlcoholShopResponse response = useCase.findById(shopId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(shopId);
            assertThat(response.name()).isEqualTo("Алкомаркет №1");

            verify(repository, times(1)).findById(shopId);
        }

        @Test
        @DisplayName("Должен выбросить исключение когда алкомаркет не найден")
        void shouldThrowExceptionWhenShopNotFound() {
            // Given
            UUID shopId = UUID.randomUUID();
            when(repository.findById(shopId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.findById(shopId))
                    .isInstanceOf(AlcoholShopNotFoundException.class)
                    .hasMessageContaining("AlcoholShop with id " + shopId + " not found");

            verify(repository, times(1)).findById(shopId);
        }
    }

    @Nested
    @DisplayName("Метод findAll()")
    class FindAllMethodTests {

        @Test
        @DisplayName("Должен вернуть страницу с алкомаркетами")
        void shouldReturnPageOfAlcoholShops() {
            // Given
            List<AlcoholShop> shops = List.of(
                    createTestShop(),
                    createTestShop(),
                    createTestShop()
            );

            Page<AlcoholShop> page = new PageImpl<>(shops);
            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PageResponse<AlcoholShopResponse> response = useCase.findAll(0, 3, "name");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(3);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.pageSize()).isEqualTo(3);
            assertThat(response.totalElements()).isEqualTo(3);
            assertThat(response.totalPages()).isEqualTo(1);

            verify(repository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Должен вернуть пустую страницу когда нет алкомаркетов")
        void shouldReturnEmptyPageWhenNoShops() {
            // Given
            Page<AlcoholShop> emptyPage = Page.empty();
            when(repository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PageResponse<AlcoholShopResponse> response = useCase.findAll(0, 10, "name");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);

            verify(repository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Должен передать корректные параметры пагинации")
        void shouldPassCorrectPaginationParameters() {
            // Given
            Page<AlcoholShop> emptyPage = Page.empty();
            when(repository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            useCase.findAll(2, 20, "address");

            // Then
            verify(repository, times(1)).findAll(PageRequest.of(2, 20, Sort.by("address")));
        }
    }

    /**
     * Создаёт тестовый экземпляр AlcoholShop для использования в тестах.
     */
    private AlcoholShop createTestShop() {
        return AlcoholShop.create(
                "Алкомаркет №1",
                "г. Москва, ул. Примерная, д. 1",
                new com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates(55.7558, 37.6173),
                new com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber("+74951234567"),
                new com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours("09:00-22:00"),
                ShopType.SUPERMARKET
        );
    }
}
