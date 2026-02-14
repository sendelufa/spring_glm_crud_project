package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.application.dto.AlcoholShopResponse;
import com.alcoradar.alcoholshop.application.dto.CreateAlcoholShopRequest;
import com.alcoradar.alcoholshop.application.dto.PageResponse;
import com.alcoradar.alcoholshop.application.usecase.AlcoholShopUseCase;
import com.alcoradar.alcoholshop.domain.model.Role;
import com.alcoradar.alcoholshop.interfaces.security.RequireAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "shops", description = "API для управления алкомаркетами")
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
    @Operation(
            summary = "Создать новый алкомаркет",
            description = """
                    Создаёт новый алкомаркет в системе.

                    **Обязательные поля:**
                    - name: название алкомаркета (минимум 2 символа)
                    - address: адрес алкомаркета
                    - chainName: название сети (если есть)
                    - hasLicense: наличие лицензии (true/false)

                    **Ответ:**
                    - Возвращает созданный алкомаркет с присвоенным ID
                    - Статус: 201 Created
                    """,
            tags = {"shops"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Алкомаркет успешно создан",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlcoholShopResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации входных данных",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationError")
                    )
            )
    })
    @PostMapping
    @RequireAuth(roles = {Role.USER, Role.ADMIN})
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
    @Operation(
            summary = "Получить алкомаркет по ID",
            description = """
                    Возвращает алкомаркет по его уникальному идентификатору (UUID).

                    **Параметр:**
                    - id: UUID алкомаркета

                    **Ответ:**
                    - DTO алкомаркета со всей информацией
                    - Статус: 200 OK

                    **Ошибки:**
                    - 404 Not Found: алкомаркет с указанным ID не существует
                    """,
            tags = {"shops"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Алкомаркет найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlcoholShopResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Алкомаркет не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/NotFoundError")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат UUID",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BadRequestError")
                    )
            )
    })
    @GetMapping("/{id}")
    ResponseEntity<AlcoholShopResponse> findById(
            @Parameter(
                    description = "Уникальный идентификатор алкомаркета (UUID)",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    required = true
            )
            @PathVariable UUID id) {
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
    @Operation(
            summary = "Получить список алкомаркетов",
            description = """
                    Возвращает страницу алкомаркетов с поддержкой пагинации и сортировки.

                    **Параметры пагинации:**
                    - page: номер страницы (начинается с 0, по умолчанию 0)
                    - size: размер страницы (по умолчанию 10, максимум 100)
                    - sortBy: поле для сортировки (по умолчанию "name")

                    **Доступные поля для сортировки:**
                    - name: по названию
                    - address: по адресу
                    - chainName: по названию сети

                    **Ответ:**
                    - Массив алкомаркетов
                    - Метаданные пагинации (номер страницы, размер, всего элементов)
                    - Статус: 200 OK
                    """,
            tags = {"shops"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список алкомаркетов успешно получен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные параметры пагинации",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BadRequestError")
                    )
            )
    })
    @GetMapping
    ResponseEntity<PageResponse<AlcoholShopResponse>> findAll(
            @Parameter(
                    description = "Номер страницы (0-based)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                    description = "Размер страницы",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "Поле для сортировки",
                    example = "name"
            )
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        PageResponse<AlcoholShopResponse> response = useCase.findAll(page, size, sortBy);
        return ResponseEntity.ok(response);
    }
}
