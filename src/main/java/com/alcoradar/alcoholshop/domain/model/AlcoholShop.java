package com.alcoradar.alcoholshop.domain.model;

import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Агрегат AlcoholShop - корень агрегата для доменной сущности алкомаркета
 *
 * <p>Представляет магазин по продаже алкогольной продукции с полной информацией
 * о местоположении, контактах и режиме работы.</p>
 *
 * <p>Является Aggregate Root в контексте DDD, инкапсулируя Value Objects
 * для координат, телефона и рабочего времени.</p>
 */
public class AlcoholShop {

    private UUID id;
    private String name;
    private String address;
    private Coordinates coordinates;
    private PhoneNumber phoneNumber;
    private WorkingHours workingHours;
    private ShopType shopType;
    private LocalDateTime createdAt;

    /**
     * Private конструктор для использования factory method
     */
    private AlcoholShop() {
    }

    /**
     * Factory method для создания алкомаркета
     *
     * @param name название магазина (не может быть пустым или null)
     * @param address адрес магазина (не может быть пустым или null)
     * @param coordinates координаты (Value Object)
     * @param phoneNumber номер телефона (Value Object)
     * @param workingHours режим работы (Value Object)
     * @param shopType тип магазина
     * @return созданный экземпляр AlcoholShop
     * @throws IllegalArgumentException если name или address пустые или null
     */
    public static AlcoholShop create(
        String name,
        String address,
        Coordinates coordinates,
        PhoneNumber phoneNumber,
        WorkingHours workingHours,
        ShopType shopType
    ) {
        AlcoholShop shop = new AlcoholShop();

        // Валидация обязательных полей
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес не может быть пустым");
        }

        // Инициализация полей
        shop.id = UUID.randomUUID();
        shop.name = name;
        shop.address = address;
        shop.coordinates = coordinates;
        shop.phoneNumber = phoneNumber;
        shop.workingHours = workingHours;
        shop.shopType = shopType;
        shop.createdAt = LocalDateTime.now();

        return shop;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public WorkingHours getWorkingHours() {
        return workingHours;
    }

    public ShopType getShopType() {
        return shopType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Domain methods

    /**
     * Обновляет название магазина
     *
     * @param name новое название
     * @throws IllegalArgumentException если name пустой или null
     */
    public void updateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        this.name = name;
    }

    /**
     * Обновляет адрес магазина
     *
     * @param address новый адрес
     * @throws IllegalArgumentException если address пустой или null
     */
    public void updateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес не может быть пустым");
        }
        this.address = address;
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlcoholShop that = (AlcoholShop) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AlcoholShop{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", address='" + address + '\'' +
            ", coordinates=" + coordinates +
            ", phoneNumber=" + phoneNumber +
            ", workingHours=" + workingHours +
            ", shopType=" + shopType +
            ", createdAt=" + createdAt +
            '}';
    }
}
