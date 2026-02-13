package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.domain.exception.AlcoholShopNotFoundException;
import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.repository.AlcoholShopRepository;
import com.alcoradar.alcoholshop.infrastructure.persistence.entity.AlcoholShopEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация порта AlcoholShopRepository через Spring Data JPA
 *
 * <p>Это адаптер в Hexagonal Architecture (Ports and Adapters pattern),
 * который реализует Domain порт {@link AlcoholShopRepository} используя
 * Spring Data JPA для персистентности.</p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Конвертация Domain Entity <-> JPA Entity</li>
 *   <li>Делегирование CRUD операций в Spring Data JPA</li>
 *   <li>Обработка исключений persistence слоя</li>
 * </ul>
 *
 * <p><b>Architecture:</b></p>
 * <pre>
 * Domain Layer (Port)
 *    ↓ depends on
 * Infrastructure Layer (Adapter)
 *    ↓ uses
 * Spring Data JPA
 *    ↓ uses
 * PostgreSQL Database
 * </pre>
 *
 * @see AlcoholShopRepository
 * @see SpringDataAlcoholShopRepository
 * @see AlcoholShopEntity
 */
@Repository
@RequiredArgsConstructor
public class AlcoholShopRepositoryImpl implements AlcoholShopRepository {

    private final SpringDataAlcoholShopRepository springDataRepository;

    /**
     * Сохраняет или обновляет алкомаркет
     *
     * <p>Конвертирует Domain Entity в JPA Entity, сохраняет в базу данных,
     * затем конвертирует обратно в Domain Entity.</p>
     *
     * @param shop сущность алкомаркета для сохранения
     * @return сохранённая сущность с присвоенным ID
     * @throws IllegalArgumentException если shop равен null
     */
    @Override
    public AlcoholShop save(AlcoholShop shop) {
        if (shop == null) {
            throw new IllegalArgumentException("AlcoholShop cannot be null");
        }

        // Конвертируем Domain -> Entity
        AlcoholShopEntity entity = AlcoholShopEntity.fromDomain(shop);

        // Сохраняем через Spring Data JPA
        AlcoholShopEntity savedEntity = springDataRepository.save(entity);

        // Конвертируем Entity -> Domain и возвращаем
        return savedEntity.toDomain();
    }

    /**
     * Находит алкомаркет по его уникальному идентификатору
     *
     * <p>Делегирует поиск в Spring Data JPA и конвертирует результат в Domain Entity.</p>
     *
     * @param id уникальный идентификатор алкомаркета
     * @return Optional с найденным алкомаркетом, или пустой Optional если не найден
     * @throws IllegalArgumentException если id равен null
     */
    @Override
    public Optional<AlcoholShop> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        // Ищем через Spring Data JPA
        return springDataRepository.findById(id)
            .map(AlcoholShopEntity::toDomain);
    }

    /**
     * Возвращает все алкомаркеты
     *
     * <p>Делегирует вызов в Spring Data JPA и конвертирует все результаты в Domain Entity.</p>
     *
     * @return список всех алкомаркетов
     */
    @Override
    public List<AlcoholShop> findAll() {
        // Получаем все Entity через Spring Data JPA
        List<AlcoholShopEntity> entities = springDataRepository.findAll();

        // Конвертируем Entity -> Domain через Stream API
        return entities.stream()
            .map(AlcoholShopEntity::toDomain)
            .toList();
    }

    /**
     * Проверяет существование алкомаркета по ID
     *
     * <p>Делегирует проверку в Spring Data JPA.</p>
     *
     * @param id уникальный идентификатор алкомаркета
     * @return true если алкомаркет существует, false в противном случае
     * @throws IllegalArgumentException если id равен null
     */
    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        // Делегируем в Spring Data JPA
        return springDataRepository.existsById(id);
    }

    /**
     * Удаляет алкомаркет по его уникальному идентификатору
     *
     * <p>Делегирует удаление в Spring Data JPA.</p>
     *
     * @param id уникальный идентификатор алкомаркета для удаления
     * @throws IllegalArgumentException если id равен null
     * @throws AlcoholShopNotFoundException если алкомаркет не найден
     */
    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        // Проверяем существование перед удалением
        if (!springDataRepository.existsById(id)) {
            throw new AlcoholShopNotFoundException(
                "AlcoholShop с ID " + id + " не найден"
            );
        }

        // Делегируем удаление в Spring Data JPA
        springDataRepository.deleteById(id);
    }
}
