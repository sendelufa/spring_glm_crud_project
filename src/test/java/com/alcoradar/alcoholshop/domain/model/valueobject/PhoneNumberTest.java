package com.alcoradar.alcoholshop.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Тестовый класс для PhoneNumber Value Object.
 * Проверяет валидацию, нормализацию и поведение телефонных номеров.
 */
class PhoneNumberTest {

    @ParameterizedTest(name = "Валидный номер: {0}")
    @CsvSource({
        "+74951234567",
        "+79991234567",
        "+79161234567"
    })
    @DisplayName("Должен создать валидный номер телефона")
    void shouldCreateValidPhoneNumber(String phone) {
        // When
        PhoneNumber phoneNumber = new PhoneNumber(phone);

        // Then
        assertThat(phoneNumber.getValue())
            .isNotNull()
            .isEqualTo(phone);
        assertThat(phoneNumber.isValid())
            .isTrue();
    }

    @ParameterizedTest(name = "Невалидный номер: {0}")
    @CsvSource({
        "84951234567",      // Не начинается с +7
        "+71234567",        // Меньше 11 цифр
        "+7123456789012",   // Больше 11 цифр
        "abc123",           // Буквы
        "+61234567890"      // Не +7
    })
    @DisplayName("Должен выбросить исключение для невалидного номера")
    void shouldThrowExceptionForInvalidPhoneNumber(String phone) {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber(phone))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат номера");
    }

    @Test
    @DisplayName("Должен поддерживать пустой номер")
    void shouldSupportEmptyPhoneNumber() {
        // When
        PhoneNumber phoneNumber = new PhoneNumber(null);

        // Then
        assertThat(phoneNumber.getValue())
            .isNull();
        assertThat(phoneNumber.isValid())
            .isFalse();
    }

    @Test
    @DisplayName("Должен корректно реализовать equals и hashCode для валидных номеров")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        PhoneNumber phone1 = new PhoneNumber("+74951234567");
        PhoneNumber phone2 = new PhoneNumber("+74951234567");
        PhoneNumber phone3 = new PhoneNumber("+79991234567");

        // Then
        assertThat(phone1)
            .isEqualTo(phone2)
            .isNotEqualTo(phone3)
            .hasSameHashCodeAs(phone2);
    }

    @Test
    @DisplayName("Должен корректно реализовать equals и hashCode для null номеров")
    void shouldImplementEqualsAndHashCodeForNullNumbers() {
        // Given
        PhoneNumber phone1 = new PhoneNumber(null);
        PhoneNumber phone2 = new PhoneNumber(null);
        PhoneNumber phone3 = new PhoneNumber("+74951234567");

        // Then
        assertThat(phone1)
            .isEqualTo(phone2)
            .isNotEqualTo(phone3)
            .hasSameHashCodeAs(phone2);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустую строку как невалидный номер")
    void shouldTreatEmptyStringAsInvalid() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат номера");
    }

    @ParameterizedTest(name = "Невалидный формат с пробелами: '{0}'")
    @CsvSource({
        "+7 495 123 45 67",    // С пробелами
        "+7-495-123-45-67",    // С дефисами
        "+7 (495) 123-45-67"   // Со скобками
    })
    @DisplayName("Должен выбросить исключение для номера с разделителями")
    void shouldThrowExceptionForNumberWithDelimiters(String phone) {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber(phone))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат номера");
    }
}
