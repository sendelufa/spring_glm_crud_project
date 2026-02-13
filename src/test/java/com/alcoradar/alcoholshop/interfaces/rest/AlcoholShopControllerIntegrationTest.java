package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.AlcoholShopResponse;
import com.alcoradar.alcoholshop.application.dto.CreateAlcoholShopRequest;
import com.alcoradar.alcoholshop.application.dto.PageResponse;
import com.alcoradar.alcoholshop.domain.model.ShopType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для AlcoholShopController
 *
 * <p>Использует Testcontainers для запуска реальной PostgreSQL базы данных
 * в Docker контейнере и TestRestTemplate для HTTP запросов к REST API.</p>
 *
 * <p>Тестируется полный цикл CRUD операций через REST endpoints:</p>
 * <ul>
 *   <li>POST /api/shops - создание нового алкомаркета</li>
 *   <li>GET /api/shops/{id} - получение алкомаркета по ID</li>
 *   <li>GET /api/shops - получение списка алкомаркетов с пагинацией</li>
 * </ul>
 *
 * <p>Валидация проверяется отправкой невалидных данных и ожиданием 400 Bad Request.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AlcoholShopControllerIntegrationTest {

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
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Должен создать алкомаркет и вернуть 201 Created с данными")
    void shouldCreateAlcoholShop() {
        // Given: формируем запрос на создание алкомаркета
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                "Пятёрочка",
                "г. Москва, ул. Ленина, д. 1, кв. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                "+74951234567",
                "9:00-22:00",
                ShopType.SUPERMARKET
        );

        // When: отправляем POST запрос на /api/shops
        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
                "/api/shops",
                request,
                AlcoholShopResponse.class
        );

        // Then: проверяем статус 201 Created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // And: проверяем, что тело ответа содержит данные созданного алкомаркета
        AlcoholShopResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("Пятёрочка");
        assertThat(body.address()).isEqualTo("г. Москва, ул. Ленина, д. 1, кв. 1");
        assertThat(body.coordinates()).isNotNull();
        assertThat(body.coordinates().latitude()).isEqualTo(55.7558);
        assertThat(body.coordinates().longitude()).isEqualTo(37.6173);
        assertThat(body.phoneNumber()).isEqualTo("+74951234567");
        assertThat(body.workingHours()).isEqualTo("9:00-22:00");
        assertThat(body.shopType()).isEqualTo(ShopType.SUPERMARKET);
        assertThat(body.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request для невалидных данных (пустое название)")
    void shouldReturnBadRequestForInvalidData() {
        // Given: формируем запрос с пустым названием (нарушение @NotBlank)
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                "",  // пустое название - должно вызвать ошибку валидации
                "г. Москва, ул. Ленина, д. 1, кв. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                "+74951234567",
                "9:00-22:00",
                ShopType.SUPERMARKET
        );

        // When: отправляем POST запрос на /api/shops
        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
                "/api/shops",
                request,
                AlcoholShopResponse.class
        );

        // Then: ожидаем статус 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request для невалидных данных (короткий адрес)")
    void shouldReturnBadRequestForShortAddress() {
        // Given: формируем запрос с коротким адресом (менее 10 символов)
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                "Пятёрочка",
                "Тверская",  // короткий адрес - менее 10 символов
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                "+74951234567",
                "9:00-22:00",
                ShopType.SUPERMARKET
        );

        // When: отправляем POST запрос на /api/shops
        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
                "/api/shops",
                request,
                AlcoholShopResponse.class
        );

        // Then: ожидаем статус 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Должен найти алкомаркет по ID и вернуть 200 OK")
    void shouldFindById() {
        // Given: создаём алкомаркет через POST
        CreateAlcoholShopRequest createRequest = new CreateAlcoholShopRequest(
                "Алкомаркет для поиска",
                "г. Санкт-Петербург, Невский пр., д. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(59.9343, 30.3351),
                "+78121234567",
                "10:00-21:00",
                ShopType.SPECIALTY
        );

        ResponseEntity<AlcoholShopResponse> createResponse = restTemplate.postForEntity(
                "/api/shops",
                createRequest,
                AlcoholShopResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID createdId = createResponse.getBody().id();

        // When: ищем алкомаркет по ID через GET
        ResponseEntity<AlcoholShopResponse> getResponse = restTemplate.getForEntity(
                "/api/shops/{id}",
                AlcoholShopResponse.class,
                createdId
        );

        // Then: проверяем, что найден тот же самый алкомаркет
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AlcoholShopResponse body = getResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(createdId);
        assertThat(body.name()).isEqualTo("Алкомаркет для поиска");
        assertThat(body.address()).isEqualTo("г. Санкт-Петербург, Невский пр., д. 1");
        assertThat(body.coordinates()).isNotNull();
        assertThat(body.coordinates().latitude()).isEqualTo(59.9343);
        assertThat(body.coordinates().longitude()).isEqualTo(30.3351);
        assertThat(body.phoneNumber()).isEqualTo("+78121234567");
        assertThat(body.workingHours()).isEqualTo("10:00-21:00");
        assertThat(body.shopType()).isEqualTo(ShopType.SPECIALTY);
    }

    @Test
    @DisplayName("Должен вернуть страницу алкомаркетов с пагинацией")
    void shouldReturnPageOfShops() {
        // Given: создаём несколько алкомаркетов
        CreateAlcoholShopRequest request1 = new CreateAlcoholShopRequest(
                "Алкомаркет А",
                "г. Москва, ул. Тверская, д. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                "+74951111111",
                "9:00-22:00",
                ShopType.SUPERMARKET
        );

        CreateAlcoholShopRequest request2 = new CreateAlcoholShopRequest(
                "Алкомаркет Б",
                "г. Москва, ул. Арбат, д. 2",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                "+74952222222",
                "10:00-21:00",
                ShopType.DUTY_FREE
        );

        restTemplate.postForEntity("/api/shops", request1, AlcoholShopResponse.class);
        restTemplate.postForEntity("/api/shops", request2, AlcoholShopResponse.class);

        // When: запрашиваем страницу с алкомаркетами
        // Используем exchange с ParameterizedTypeReference для правильной десериализации generics
        ResponseEntity<PageResponse<AlcoholShopResponse>> response = restTemplate.exchange(
                "/api/shops?page=0&size=10&sortBy=name",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageResponse<AlcoholShopResponse>>() {}
        );

        // Then: проверяем, что страница содержит созданные алкомаркеты
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<AlcoholShopResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).isNotEmpty();
        assertThat(body.content()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(body.currentPage()).isEqualTo(0);
        assertThat(body.pageSize()).isEqualTo(10);
        assertThat(body.totalElements()).isGreaterThanOrEqualTo(2);

        // Проверяем, что среди результатов есть наши алкомаркеты
        assertThat(body.content())
                .extracting(AlcoholShopResponse::name)
                .contains("Алкомаркет А", "Алкомаркет Б");
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found для несуществующего ID")
    void shouldReturnNotFoundForNonExistentId() {
        // Given: несуществующий UUID
        UUID nonExistentId = UUID.randomUUID();

        // When: пытаемся найти алкомаркет по несуществующему ID
        ResponseEntity<AlcoholShopResponse> response = restTemplate.getForEntity(
                "/api/shops/{id}",
                AlcoholShopResponse.class,
                nonExistentId
        );

        // Then: ожидаем статус 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request для невалидных координат (ширина вне диапазона)")
    void shouldReturnBadRequestForInvalidLatitude() {
        // Given: формируем запрос с невалидной широтой (больше 90)
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                "Пятёрочка",
                "г. Москва, ул. Ленина, д. 1, кв. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(95.0, 37.6173),  // невалидная широта
                "+74951234567",
                "9:00-22:00",
                ShopType.SUPERMARKET
        );

        // When: отправляем POST запрос на /api/shops
        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
                "/api/shops",
                request,
                AlcoholShopResponse.class
        );

        // Then: ожидаем статус 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Должен создать алкомаркет с минимальными обязательными полями")
    void shouldCreateAlcoholShopWithMinimumRequiredFields() {
        // Given: формируем запрос только с обязательными полями
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
                "Минимальный Алкомаркет",
                "г. Москва, ул. Минимальная, д. 1, оф. 1",
                new com.alcoradar.alcoholshop.application.dto.CoordinatesDto(55.7558, 37.6173),
                null,  // phoneNumber - опциональное поле
                null,  // workingHours - опциональное поле
                null   // shopType - опциональное поле
        );

        // When: отправляем POST запрос
        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
                "/api/shops",
                request,
                AlcoholShopResponse.class
        );

        // Then: ожидаем успешное создание
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AlcoholShopResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("Минимальный Алкомаркет");
        assertThat(body.address()).isEqualTo("г. Москва, ул. Минимальная, д. 1, оф. 1");
        assertThat(body.phoneNumber()).isNull();
        assertThat(body.workingHours()).isNull();
        assertThat(body.shopType()).isNull();
    }
}
