package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.AlcoholShopResponse;
import com.alcoradar.alcoholshop.application.dto.CreateAlcoholShopRequest;
import com.alcoradar.alcoholshop.application.dto.PageResponse;
import com.alcoradar.alcoholshop.application.usecase.AlcoholShopUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller для управления алкомаркетами
 *
 * <p>Этот контроллер предоставляет REST API для операций CRUD над алкомаркетами.
 * Следует принципам Hexagonal Architecture как часть interfaces layer (REST adapter).</p>
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/shops - создание нового алкомаркета</li>
 *   <li>GET /api/shops/{id} - получение алкомаркета по ID</li>
 *   <li>GET /api/shops - получение списка алкомаркетов с пагинацией</li>
 * </ul>
 *
 * <p><b>Валидация:</b></p>
 * <ul>
 *   <li>Все request DTOs валидируются через Jakarta Validation annotations</li>
 *   <li>При ошибках валидации возвращается HTTP 400 Bad Request</li>
 *   <li>При отсутствии ресурса возвращается HTTP 404 Not Found</li>
 *   <li>При успешном создании возвращается HTTP 201 Created</li>
 * </ul>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * REST Controller (interfaces/rest) ← этот класс
 *    ↓ делегирует
 * Use Case (application/usecase)
 *    ↓ использует
 * Repository Port (domain/repository)
 *    ↓ реализует
 * Repository Adapter (infrastructure/persistence)
 * </pre>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 * @see AlcoholShopUseCase
 * @see CreateAlcoholShopRequest
 * @see AlcoholShopResponse
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class AlcoholShopController {

    private final AlcoholShopUseCase useCase;

    /**
     * Создаёт новый алкомаркет
     *
     * <p>Принимает {@link CreateAlcoholShopRequest} с данными для создания,
     * валидирует их и создаёт новый алкомаркет через {@link AlcoholShopUseCase}.</p>
     *
     * <p>При успешном создании возвращает HTTP 201 Created с DTO созданного алкомаркета
     * включая сгенерированный ID.</p>
     *
     * @param request DTO с данными для создания алкомаркета
     * @return DTO созданного алкомаркета со статусом 201 Created
     */
    @PostMapping
    ResponseEntity<AlcoholShopResponse> create(@Valid @RequestBody CreateAlcoholShopRequest request) {
        AlcoholShopResponse response = useCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Находит алкомаркет по его уникальному идентификатору
     *
     * <p>Ищет алкомаркет через {@link AlcoholShopUseCase} и возвращает его DTO.
     * Если алкомаркет не найден, UseCase выбросит исключение, которое будет
     * обработано глобальным обработчиком исключений (если он настроен).</p>
     *
     * @param id уникальный идентификатор алкомаркета
     * @return DTO найденного алкомаркета со статусом 200 OK
     */
    @GetMapping("/{id}")
    ResponseEntity<AlcoholShopResponse> findById(@PathVariable UUID id) {
        AlcoholShopResponse response = useCase.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает страницу алкомаркетов с пагинацией и сортировкой
     *
     * <p>Поддерживает пагинацию и сортировку через query параметры:
     * <ul>
     *   <li>page - номер страницы (0-based, по умолчанию 0)</li>
     *   <li>size - размер страницы (по умолчанию 10)</li>
     *   <li>sortBy - поле для сортировки (по умолчанию "name")</li>
     * </ul>
     * </p>
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @return страница с алкомаркетами и метаданными пагинации
     */
    @GetMapping
    ResponseEntity<PageResponse<AlcoholShopResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        PageResponse<AlcoholShopResponse> response = useCase.findAll(page, size, sortBy);
        return ResponseEntity.ok(response);
    }
}
