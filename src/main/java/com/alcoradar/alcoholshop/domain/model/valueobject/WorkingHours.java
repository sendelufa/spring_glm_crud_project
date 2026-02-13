package com.alcoradar.alcoholshop.domain.model.valueobject;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Value Object для представления часов работы магазина.
 * Хранит время работы в формате "HH:mm-HH:mm" (например: "9:00-22:00").
 */
public final class WorkingHours {

    private final String value;
    private final LocalTime openTime;
    private final LocalTime closeTime;

    /**
     * Создает объект часов работы из строки формата "HH:mm-HH:mm".
     *
     * @param value строка с временем работы (например: "9:00-22:00" или "9-22")
     * @throws IllegalArgumentException если формат неверный
     */
    public WorkingHours(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Неверный формат времени");
        }

        this.value = value.trim();
        String[] parts = this.value.split("-");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Неверный формат времени");
        }

        String openPart = parts[0].trim();
        String closePart = parts[1].trim();

        // Проверяем согласованность формата: если одна часть содержит двоеточие,
        // то и другая тоже должна содержать двоеточие
        boolean openHasColon = openPart.contains(":");
        boolean closeHasColon = closePart.contains(":");

        if (openHasColon != closeHasColon) {
            throw new IllegalArgumentException("Неверный формат времени");
        }

        try {
            this.openTime = parseTime(openPart);
            this.closeTime = parseTime(closePart);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат времени", e);
        }
    }

    /**
     * Парсит время из строки.
     * Поддерживает форматы: "H:mm", "HH:mm", "H", "HH".
     *
     * @param timeStr строка времени
     * @return LocalTime объект
     * @throws DateTimeParseException если формат неверный
     */
    private LocalTime parseTime(String timeStr) throws DateTimeParseException {
        // Проверяем на null или пустую строку после trim
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new DateTimeParseException("Пустая строка времени", timeStr, 0);
        }

        timeStr = timeStr.trim();

        // Проверяем что строка содержит только цифры и не более одного двоеточия
        long colonCount = timeStr.chars().filter(ch -> ch == ':').count();
        if (colonCount > 1) {
            throw new DateTimeParseException("Неверный формат времени", timeStr, 0);
        }

        // Если формат содержит только часы (например: "9" или "22")
        if (!timeStr.contains(":")) {
            // Проверяем что это только цифры
            if (!timeStr.matches("^\\d+$")) {
                throw new DateTimeParseException("Неверный формат времени", timeStr, 0);
            }
            return LocalTime.of(Integer.parseInt(timeStr), 0);
        }

        // Парсим формат HH:mm или H:mm
        // Проверяем базовый формат: цифры:две_цифры
        if (!timeStr.matches("^\\d+:\\d{2}$")) {
            throw new DateTimeParseException("Неверный формат времени", timeStr, 0);
        }

        // Для поддержки однозначных часов добавляем ноль если нужно
        String normalized = timeStr;
        if (timeStr.matches("^\\d:\\d{2}$")) {
            normalized = "0" + timeStr;
        }

        return LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Возвращает исходное строковое значение времени работы.
     *
     * @return строка с временем работы
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает время открытия.
     *
     * @return время открытия
     */
    public LocalTime getOpenTime() {
        return openTime;
    }

    /**
     * Возвращает время закрытия.
     *
     * @return время закрытия
     */
    public LocalTime getCloseTime() {
        return closeTime;
    }

    /**
     * Проверяет, открыт ли магазин в указанное время.
     *
     * @param dateTime дата и время для проверки
     * @return true если магазин открыт в указанное время
     */
    public boolean isOpenAt(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        LocalTime timeToCheck = dateTime.toLocalTime();

        // Если время закрытия больше времени открытия (обычный график)
        if (closeTime.isAfter(openTime)) {
            return !timeToCheck.isBefore(openTime) && !timeToCheck.isAfter(closeTime);
        }

        // Круглосуточная работа (время закрытия равно времени открытия)
        if (closeTime.equals(openTime)) {
            return true;
        }

        // Ночной график (время закрытия меньше времени открытия, например: 22:00-6:00)
        // В этом случае магазин открыт с openTime до полуночи и с полуночи до closeTime
        return !timeToCheck.isBefore(openTime) || !timeToCheck.isAfter(closeTime);
    }

    /**
     * Проверяет, закрыт ли магазин полностью.
     * Возвращает false если магазин имеет рабочие часы.
     *
     * @return true если магазин закрыт, false в противном случае
     */
    public boolean isClosed() {
        // Value Object с установленными часами работы не может быть "закрыт"
        // Этот метод имеет смысл для Optional<WorkingHours> в сущности Shop
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingHours that = (WorkingHours) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "WorkingHours{" +
                "value='" + value + '\'' +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                '}';
    }
}
