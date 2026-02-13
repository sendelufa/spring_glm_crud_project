package com.alcoradar.alcoholshop.infrastructure.persistence.repository;

import com.alcoradar.alcoholshop.infrastructure.persistence.entity.AlcoholShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository для AlcoholShopEntity
 *
 * <p>Это адаптер Spring Data JPA, который предоставляет стандартные CRUD операции
 * для работы с JPA Entity. Является частью Infrastructure слоя в Hexagonal Architecture.</p>
 *
 * <p>Расширяет {@link JpaRepository} для получения готовых методов:
 * <ul>
 *   <li>save() - сохранение и обновление</li>
 *   <li>findById() - поиск по ID</li>
 *   <li>findAll() - получение всех записей</li>
 *   <li>existsById() - проверка существования</li>
 *   <li>deleteById() - удаление по ID</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> Этот интерфейс используется только внутри Infrastructure слоя.
 * Domain слой работает с портом {@link com.alcoradar.alcoholshop.domain.repository.AlcoholShopRepository}.</p>
 *
 * @see AlcoholShopEntity
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
interface SpringDataAlcoholShopRepository extends JpaRepository<AlcoholShopEntity, UUID> {
}
