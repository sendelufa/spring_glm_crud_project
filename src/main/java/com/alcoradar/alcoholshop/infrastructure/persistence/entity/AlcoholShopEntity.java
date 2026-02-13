package com.alcoradar.alcoholshop.infrastructure.persistence.entity;

import com.alcoradar.alcoholshop.domain.model.AlcoholShop;
import com.alcoradar.alcoholshop.domain.model.ShopType;
import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity для персистентности агрегата AlcoholShop.
 * <p>
 * Отвечает за маппинг доменной сущности в таблицу alcohol_shops базы данных.
 * Использует простой подход с плоской структурой полей для оптимизации производительности.
 * </p>
 * <p>
 * Конвертация между JPA Entity и Domain Entity осуществляется через методы:
 * <ul>
 *   <li>{@link #toDomain()} - преобразует JPA Entity в Domain Entity с Value Objects</li>
 *   <li>{@link #fromDomain(AlcoholShop)} - статический factory method для создания JPA Entity из Domain Entity</li>
 * </ul>
 * </p>
 *
 * @see AlcoholShop
 * @see Coordinates
 * @see PhoneNumber
 * @see WorkingHours
 */
@Entity
@Table(name = "alcohol_shops")
public class AlcoholShopEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "working_hours")
    private String workingHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_type", nullable = false)
    private ShopType shopType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor для JPA.
     * Не должен использоваться в бизнес-логике приложения.
     */
    protected AlcoholShopEntity() {
    }

    /**
     * Конструктор для создания JPA Entity с полным набором полей.
     * Используется для удобства создания entity в тестах и мапперах.
     *
     * @param id           уникальный идентификатор
     * @param name         название магазина
     * @param address      адрес магазина
     * @param latitude     широта
     * @param longitude    долгота
     * @param phoneNumber  номер телефона
     * @param workingHours режим работы
     * @param shopType     тип магазина
     * @param createdAt    дата создания
     */
    public AlcoholShopEntity(
        UUID id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String phoneNumber,
        String workingHours,
        ShopType shopType,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.workingHours = workingHours;
        this.shopType = shopType;
        this.createdAt = createdAt;
    }

    /**
     * Статический factory method для создания JPA Entity из Domain Entity.
     * Извлекает данные из Domain Entity и его Value Objects.
     * <p>
     * Пример использования:
     * <pre>{@code
     * AlcoholShop domainShop = AlcoholShop.create(...);
     * AlcoholShopEntity entity = AlcoholShopEntity.fromDomain(domainShop);
     * }</pre>
     * </p>
     *
     * @param domain доменная сущность AlcoholShop
     * @return JPA Entity для сохранения в базу данных
     * @throws IllegalArgumentException если domain равен null
     */
    public static AlcoholShopEntity fromDomain(AlcoholShop domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain entity cannot be null");
        }

        Coordinates coords = domain.getCoordinates();
        PhoneNumber phone = domain.getPhoneNumber();
        WorkingHours hours = domain.getWorkingHours();

        return new AlcoholShopEntity(
            domain.getId(),
            domain.getName(),
            domain.getAddress(),
            coords != null ? coords.getLatitude() : null,
            coords != null ? coords.getLongitude() : null,
            phone != null ? phone.getValue() : null,
            hours != null ? hours.getValue() : null,
            domain.getShopType(),
            domain.getCreatedAt()
        );
    }

    /**
     * Конвертирует JPA Entity в Domain Entity.
     * Создаёт экземпляр агрегата AlcoholShop с соответствующими Value Objects.
     * <p>
     * Примечание: этот метод создаёт Domain Entity используя factory method
     * с последующей реконструкцией состояния из базы данных.
     * </p>
     * <p>
     * Пример использования:
     * <pre>{@code
     * AlcoholShopEntity entity = repository.findById(id);
     * AlcoholShop domainShop = entity.toDomain();
     * }</pre>
     * </p>
     *
     * @return доменная сущность AlcoholShop
     */
    public AlcoholShop toDomain() {
        Coordinates coordinates = null;
        if (latitude != null && longitude != null) {
            coordinates = new Coordinates(latitude, longitude);
        }

        PhoneNumber phoneNumber = null;
        if (this.phoneNumber != null) {
            phoneNumber = new PhoneNumber(this.phoneNumber);
        }

        WorkingHours workingHours = null;
        if (this.workingHours != null) {
            workingHours = new WorkingHours(this.workingHours);
        }

        // Создаём доменную сущность через factory method с временными валидными данными
        AlcoholShop shop = AlcoholShop.create(
            this.name,
            this.address,
            coordinates,
            phoneNumber,
            workingHours,
            this.shopType
        );

        // Восстанавливаем оригинальные данные из БД
        shop.setId(this.id);
        shop.setNameForReconstruction(this.name);
        shop.setAddressForReconstruction(this.address);
        shop.setCoordinatesForReconstruction(coordinates);
        shop.setPhoneNumberForReconstruction(phoneNumber);
        shop.setWorkingHoursForReconstruction(workingHours);
        shop.setShopTypeForReconstruction(this.shopType);
        shop.setCreatedAtForReconstruction(this.createdAt);

        return shop;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public ShopType getShopType() {
        return shopType;
    }

    public void setShopType(ShopType shopType) {
        this.shopType = shopType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlcoholShopEntity that = (AlcoholShopEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AlcoholShopEntity{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", address='" + address + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", workingHours='" + workingHours + '\'' +
            ", shopType=" + shopType +
            ", createdAt=" + createdAt +
            '}';
    }
}
