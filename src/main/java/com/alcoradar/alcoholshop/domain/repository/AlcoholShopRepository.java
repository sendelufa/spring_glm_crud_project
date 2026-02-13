package com.alcoradar.alcoholshop.domain.repository;

import com.alcoradar.alcoholshop.domain.model.AlcoholShop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Port для AlcoholShop агрегата
 *
 * <p>Это интерфейс порта в Hexagonal Architecture (Ports and Adapters pattern).
 * Определяет контракт для доступа к данным доменного слоя,
 * не зависящий от конкретной технологии реализации.</p>
 *
 * <p>В terminology DDD это Repository интерфейс, который будет реализован
 * в infrastructure слое через конкретные адаптеры (JPA, MongoDB, и т.д.).</p>
 *
 * <p><b>Port Role:</b> Этот порт определяет "что" нужно доменному слою для
 * persistence, не заботясь о "как" это будет реализовано.</p>
 *
 * @see AlcoholShop
 * @see <a href="https://herbertograca.com/2017/09/14/ports-adapters-architecture/">Hexagonal Architecture</a>
 */
public interface AlcoholShopRepository {

    /**
     * Сохраняет или обновляет алкомаркет
     *
     * <p>Если у сущности нет ID, создаётся новая запись.
     * Если ID есть, обновляется существующая запись.</p>
     *
     * @param shop сущность алкомаркета для сохранения
     * @return сохранённая сущность с присвоенным ID
     * @throws IllegalArgumentException если shop равен null
     */
    AlcoholShop save(AlcoholShop shop);

    /**
     * Находит алкомаркет по его уникальному идентификатору
     *
     * @param id уникальный идентификатор алкомаркета
     * @return Optional с найденным алкомаркетом, или пустой Optional если не найден
     * @throws IllegalArgumentException если id равен null
     */
    Optional<AlcoholShop> findById(UUID id);

    /**
     * Возвращает все алкомаркеты
     *
     * <p><b>Внимание:</b> Для больших объёмов данных рекомендуется использовать
     * пагинацию или streaming API в конкретной реализации.</p>
     *
     * @return список всех алкомаркетов
     */
    List<AlcoholShop> findAll();

    /**
     * Проверяет существование алкомаркета по ID
     *
     * <p>Используется для валидации перед операциями обновления
     * или для проверки бизнес-ограничений.</p>
     *
     * @param id уникальный идентификатор алкомаркета
     * @return true если алкомаркет существует, false в противном случае
     * @throws IllegalArgumentException если id равен null
     */
    boolean existsById(UUID id);

    /**
     * Удаляет алкомаркет по его уникальному идентификатору
     *
     * <p><b>Внимание:</b> Операция необратимая. Рекомендуется
     * использовать soft delete в реальных проектах.</p>
     *
     * @param id уникальный идентификатор алкомаркета для удаления
     * @throws IllegalArgumentException если id равен null
     * @throws com.alcoradar.alcoholshop.domain.model.AlcoholShopNotFoundException если алкомаркет не найден
     */
    void deleteById(UUID id);
}
