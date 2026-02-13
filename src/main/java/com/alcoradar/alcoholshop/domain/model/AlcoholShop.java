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
     * Package-private constructor для реконструкции из persistence layer.
     * Используется JPA entity для восстановления доменной сущности из базы данных.
     * <p>
     * WARNING: Не использовать напрямую в бизнес-логике! Используйте {@link #create()}
     * </p>
     */
    AlcoholShop(boolean reconstructed) {
        // Constructor for reconstruction from persistence layer
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

    // Package-private setters for reconstruction from persistence layer

    /**
     * Устанавливает ID при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Устанавливает название при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setNameForReconstruction(String name) {
        this.name = name;
    }

    /**
     * Устанавливает адрес при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setAddressForReconstruction(String address) {
        this.address = address;
    }

    /**
     * Устанавливает координаты при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setCoordinatesForReconstruction(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Устанавливает телефон при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setPhoneNumberForReconstruction(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Устанавливает режим работы при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setWorkingHoursForReconstruction(WorkingHours workingHours) {
        this.workingHours = workingHours;
    }

    /**
     * Устанавливает тип магазина при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setShopTypeForReconstruction(ShopType shopType) {
        this.shopType = shopType;
    }

    /**
     * Устанавливает дату создания при реконструкции из persistence layer.
     * WARNING: Только для внутреннего использования в infrastructure layer!
     */
    public void setCreatedAtForReconstruction(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
