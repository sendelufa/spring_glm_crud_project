package com.alcoradar.alcoholshop.domain.model.valueobject;

import java.util.Objects;

/**
 * Value Object для представления телефонного номера.
 * <p>
 * Обеспечивает валидацию и неизменяемость телефонных номеров.
 * Поддерживает форматы российских номеров: +7XXXXXXXXXX (ровно 11 цифр).
 * Допускает значение null для случаев, когда телефон не указан.
 * </p>
 * <p>
 * Примеры использования:
 * <pre>{@code
 * PhoneNumber validPhone = new PhoneNumber("+74951234567");
 * PhoneNumber emptyPhone = new PhoneNumber(null);
 *
 * if (validPhone.isValid()) {
 *     System.out.println("Номер: " + validPhone.getValue());
 * }
 * }</pre>
 * </p>
 */
public final class PhoneNumber {

    private static final String PHONE_REGEX = "^\\+7\\d{10}$";
    private final String value;

    /**
     * Создает экземпляр телефонного номера с валидацией.
     *
     * @param value телефонный номер в формате +7XXXXXXXXXX или null
     * @throws IllegalArgumentException если значение не null и не соответствует формату
     */
    public PhoneNumber(String value) {
        if (value != null && !isValidFormat(value)) {
            throw new IllegalArgumentException("Неверный формат номера");
        }
        this.value = value;
    }

    /**
     * Проверяет соответствие формату телефонного номера.
     * <p>
     * Валидный формат: +7 followed by exactly 10 digits (total 12 characters).
     * </p>
     *
     * @param phone телефонный номер для проверки
     * @return true если формат соответствует, иначе false
     */
    private boolean isValidFormat(String phone) {
        return phone.matches(PHONE_REGEX);
    }

    /**
     * Возвращает значение телефонного номера.
     *
     * @return телефонный номер или null, если номер не был указан
     */
    public String getValue() {
        return value;
    }

    /**
     * Проверяет, указан ли валидный телефонный номер.
     *
     * @return true если номер не null и валиден, иначе false
     */
    public boolean isValid() {
        return value != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? value : "не указан";
    }
}
