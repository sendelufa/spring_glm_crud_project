package com.alcoradar.alcoholshop.domain.model;

import com.alcoradar.alcoholshop.domain.model.valueobject.Coordinates;
import com.alcoradar.alcoholshop.domain.model.valueobject.PhoneNumber;
import com.alcoradar.alcoholshop.domain.model.valueobject.WorkingHours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Тесты для доменной сущности AlcoholShop
 */
class AlcoholShopTest {

    @Test
    @DisplayName("Должен создать алкомаркет с обязательными полями")
    void shouldCreateAlcoholShopWithRequiredFields() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act
        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет на Тверской",
            "г. Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // Assert
        assertThat(shop).isNotNull();
        assertThat(shop.getId()).isNotNull();
        assertThat(shop.getName()).isEqualTo("Алкомаркет на Тверской");
        assertThat(shop.getAddress()).isEqualTo("г. Москва, ул. Тверская, д. 1");
        assertThat(shop.getCoordinates()).isEqualTo(coordinates);
        assertThat(shop.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(shop.getWorkingHours()).isEqualTo(workingHours);
        assertThat(shop.getShopType()).isEqualTo(ShopType.SUPERMARKET);
        assertThat(shop.getCreatedAt()).isNotNull();
        assertThat(shop.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Должен выбросить исключение для пустого названия")
    void shouldThrowExceptionForEmptyName() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act & Assert
        assertThatThrownBy(() -> AlcoholShop.create(
            "",
            "г. Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Название не может быть пустым");
    }

    @Test
    @DisplayName("Должен выбросить исключение для null названия")
    void shouldThrowExceptionForNullName() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act & Assert
        assertThatThrownBy(() -> AlcoholShop.create(
            null,
            "г. Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Название не может быть пустым");
    }

    @Test
    @DisplayName("Должен выбросить исключение для пустого адреса")
    void shouldThrowExceptionForEmptyAddress() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act & Assert
        assertThatThrownBy(() -> AlcoholShop.create(
            "Алкомаркет на Тверской",
            "",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Адрес не может быть пустым");
    }

    @Test
    @DisplayName("Должен выбросить исключение для null адреса")
    void shouldThrowExceptionForNullAddress() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act & Assert
        assertThatThrownBy(() -> AlcoholShop.create(
            "Алкомаркет на Тверской",
            null,
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Адрес не может быть пустым");
    }

    @Test
    @DisplayName("Должен обновить данные алкомаркета")
    void shouldUpdateAlcoholShopData() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет на Тверской",
            "г. Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // Act
        shop.updateName("Новый Алкомаркет");
        shop.updateAddress("г. Москва, ул. Арбат, д. 5");

        // Assert
        assertThat(shop.getName()).isEqualTo("Новый Алкомаркет");
        assertThat(shop.getAddress()).isEqualTo("г. Москва, ул. Арбат, д. 5");
        assertThat(shop.getId()).isNotNull(); // ID не изменился
        assertThat(shop.getCreatedAt()).isNotNull(); // createdAt не изменился
    }

    @Test
    @DisplayName("Должен создать магазин разных типов")
    void shouldCreateDifferentShopTypes() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // Act
        AlcoholShop supermarket = AlcoholShop.create(
            "Супермаркет",
            "г. Москва, ул. Тверская, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        AlcoholShop specialty = AlcoholShop.create(
            "Специализированный магазин",
            "г. Москва, ул. Петровка, д. 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SPECIALTY
        );

        AlcoholShop dutyFree = AlcoholShop.create(
            "Duty Free",
            "Шереметьево, Терминал D",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.DUTY_FREE
        );

        // Assert
        assertThat(supermarket.getShopType()).isEqualTo(ShopType.SUPERMARKET);
        assertThat(specialty.getShopType()).isEqualTo(ShopType.SPECIALTY);
        assertThat(dutyFree.getShopType()).isEqualTo(ShopType.DUTY_FREE);
    }

    @Test
    @DisplayName("Должен корректно сравнивать магазины по ID")
    void shouldCompareShopsById() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        AlcoholShop shop1 = AlcoholShop.create(
            "Магазин 1",
            "Адрес 1",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        AlcoholShop shop2 = AlcoholShop.create(
            "Магазин 2",
            "Адрес 2",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // Assert & Act
        assertThat(shop1).isNotEqualTo(shop2);
        assertThat(shop1.hashCode()).isNotEqualTo(shop2.hashCode());
        assertThat(shop1).isEqualTo(shop1); // Сам с собой равен
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении пустым названием")
    void shouldThrowExceptionWhenUpdatingWithEmptyName() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет",
            "Адрес",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // Act & Assert
        assertThatThrownBy(() -> shop.updateName(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Название не может быть пустым");
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении пустым адресом")
    void shouldThrowExceptionWhenUpdatingWithEmptyAddress() {
        // Arrange
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phoneNumber = new PhoneNumber("+74951234567");
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Алкомаркет",
            "Адрес",
            coordinates,
            phoneNumber,
            workingHours,
            ShopType.SUPERMARKET
        );

        // Act & Assert
        assertThatThrownBy(() -> shop.updateAddress(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Адрес не может быть пустым");
    }
}
