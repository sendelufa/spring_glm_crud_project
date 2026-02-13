package com.alcoradar.alcoholshop.application.usecase;

import com.alcoradar.alcoholshop.application.dto.AlcoholShopResponse;
import com.alcoradar.alcoholshop.application.dto.CoordinatesDto;
import com.alcoradar.alcoholshop.application.dto.CreateAlcoholShopRequest;
import com.alcoradar.alcoholshop.application.dto.PageResponse;
import com.alcoradar.alcoholshop.domain.exception.AlcoholShopNotFoundException;
import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;
import com.alcoradar.alcoholshop.domain.repository.AlcoholShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use Case для бизнес-логики операций с алкомаркетами
 * <p>
 * Этот класс реализует application layer бизнес-логику в Hexagonal Architecture.
 * Он координирует работу между domain layer (repository, domain model) и
 * infrastructure layer (DTOs, pagination).
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Конвертация DTO <-> Domain entities</li>
 *   <li>Валидация бизнес-правил</li>
 *   <li>Координация операций с repository</li>
 *   <li>Обработка исключений domain layer</li>
 * </ul>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * REST Controller (interfaces/rest)
 *    ↓ uses
 * Use Case (application/usecase) ← этот класс
 *    ↓ uses
 * Repository Port (domain/repository)
 *    ↓ implemented by
 * Repository Adapter (infrastructure/persistence)
 *    ↓ uses
 * Database (PostgreSQL)
 * </pre>
 *
 * @see AlcoholShopRepository
 * @see AlcoholShopResponse
 * @see CreateAlcoholShopRequest
 */
@Service
@RequiredArgsConstructor
public class AlcoholShopUseCase {

    private final AlcoholShopRepository repository;

    /**
     * Создаёт новый алкомаркет
     * <p>
     * Конвертирует {@link CreateAlcoholShopRequest} в domain entity, сохраняет
     * через repository и конвертирует результат в {@link AlcoholShopResponse}.
     * </p>
     *
     * @param request DTO с данными для создания алкомаркета
     * @return DTO с созданным алкомаркетом включая сгенерированный ID
     * @throws IllegalArgumentException если обязательные поля не прошли валидацию
     */
    public AlcoholShopResponse create(CreateAlcoholShopRequest request) {
        // 1. Конвертируем DTO -> Domain
        Coordinates coordinates = new Coordinates(
                request.coordinates().latitude(),
                request.coordinates().longitude()
        );

        PhoneNumber phoneNumber = request.phoneNumber() != null
                ? new PhoneNumber(request.phoneNumber())
                : null;

        WorkingHours workingHours = request.workingHours() != null
                ? new WorkingHours(request.workingHours())
                : null;

        AlcoholShop shop = AlcoholShop.create(
                request.name(),
                request.address(),
                coordinates,
                phoneNumber,
                workingHours,
                request.shopType()
        );

        // 2. Валидация (прошла в AlcoholShop.create())

        // 3. Сохранить через repository
        AlcoholShop savedShop = repository.save(shop);

        // 4. Конвертировать Domain -> DTO
        return toResponse(savedShop);
    }

    /**
     * Находит алкомаркет по его уникальному идентификатору
     * <p>
     * Ищет алкомаркет через repository и конвертирует результат в DTO.
     * Если алкомаркет не найден, выбрасывает {@link AlcoholShopNotFoundException}.
     * </p>
     *
     * @param id уникальный идентификатор алкомаркета
     * @return DTO с найденным алкомаркетом
     * @throws AlcoholShopNotFoundException если алкомаркет с указанным ID не найден
     * @throws IllegalArgumentException если id равен null
     */
    public AlcoholShopResponse findById(UUID id) {
        // 1. Найти через repository
        AlcoholShop shop = repository.findById(id)
                // 2. Если не найден - выбросить AlcoholShopNotFoundException
                .orElseThrow(() -> new AlcoholShopNotFoundException(id));

        // 3. Конвертировать Domain -> DTO и вернуть
        return toResponse(shop);
    }

    /**
     * Возвращает страницу алкомаркетов с пагинацией и сортировкой
     * <p>
     * Извлекает страницу алкомаркетов из repository, конвертирует в DTOs
     * и оборачивает в {@link PageResponse} для удобного API ответа.
     * </p>
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки (например: "name", "address")
     * @return DTO страницы с алкомаркетами и метаданными пагинации
     * @throws IllegalArgumentException если page < 0 или size <= 0
     */
    public PageResponse<AlcoholShopResponse> findAll(int page, int size, String sortBy) {
        // 1. Создаём Pageable с пагинацией и сортировкой
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // 2. Получить из repository
        Page<AlcoholShop> shopsPage = repository.findAll(pageable);

        // 3. Конвертировать Page<Domain> -> Page<DTO>
        Page<AlcoholShopResponse> responsePage = shopsPage.map(this::toResponse);

        // 4. Создать PageResponse
        return PageResponse.of(responsePage);
    }

    /**
     * Конвертирует Domain entity в Response DTO
     * <p>
     * Приватный helper-метод для конвертации domain model в DTO.
     * Используется во всех публичных методах Use Case.
     * </p>
     *
     * @param shop domain entity алкомаркета
     * @return DTO для API ответа
     */
    private AlcoholShopResponse toResponse(AlcoholShop shop) {
        CoordinatesDto coordinatesDto = shop.getCoordinates() != null
                ? new CoordinatesDto(
                        shop.getCoordinates().getLatitude(),
                        shop.getCoordinates().getLongitude()
                )
                : null;

        String phoneNumber = shop.getPhoneNumber() != null
                ? shop.getPhoneNumber().getValue()
                : null;

        String workingHours = shop.getWorkingHours() != null
                ? shop.getWorkingHours().getValue()
                : null;

        return new AlcoholShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                coordinatesDto,
                phoneNumber,
                workingHours,
                shop.getShopType(),
                shop.getCreatedAt()
        );
    }
}
