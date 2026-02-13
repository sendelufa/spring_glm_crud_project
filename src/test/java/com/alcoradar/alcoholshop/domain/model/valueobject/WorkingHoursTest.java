package com.alcoradar.alcoholshop.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Тесты для Value Object WorkingHours")
class WorkingHoursTest {

    // ========== Тест 1: Валидное время работы ==========

    @ParameterizedTest(name = "Валидное время: {0}")
    @CsvSource({
        "9:00-22:00",
        "0:00-23:59",
        "12:00-12:00",
        "00:00-24:00",
        "8:30-20:45",
        "10-18",         // Упрощенный формат без минут
        "9-22"
    })
    @DisplayName("Должен создать валидное время работы")
    void shouldCreateValidWorkingHours(String hours) {
        // When
        WorkingHours workingHours = new WorkingHours(hours);

        // Then
        assertThat(workingHours).isNotNull();
        assertThat(workingHours.getValue()).isEqualTo(hours);
        assertThat(workingHours.getOpenTime()).isNotNull();
        assertThat(workingHours.getCloseTime()).isNotNull();
    }

    // ========== Тест 2: Невалидное время работы ==========

    @ParameterizedTest(name = "Невалидное время: {0}")
    @CsvSource({
        "25:00-22:00",     // Невалидный час открытия
        "9:00-25:00",     // Невалидный час закрытия
        "9:60-22:00",     // Невалидные минуты
        "9:00-22:60",     // Невалидные минуты
        "9:00-22",        // Неполный формат (только часы)
        "9:00",           // Только открытие
        "abc-def",        // Не числа
        "9:00:22:00",     // Неправильный разделитель
        "9:00-",          // Нет времени закрытия
        "-22:00"          // Нет времени открытия
    })
    @DisplayName("Должен выбросить исключение для невалидного времени")
    void shouldThrowExceptionForInvalidWorkingHours(String hours) {
        // When & Then
        assertThatThrownBy(() -> new WorkingHours(hours))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат времени");
    }

    @Test
    @DisplayName("Должен выбросить исключение для пустой строки")
    void shouldThrowExceptionForEmptyString() {
        assertThatThrownBy(() -> new WorkingHours(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат времени");

        assertThatThrownBy(() -> new WorkingHours("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат времени");
    }

    // ========== Тест 3: Проверка isOpenAt ==========

    @Test
    @DisplayName("Должен проверить открыт ли магазин в указанное время")
    void shouldCheckIfStoreIsOpenAtSpecificTime() {
        // Given
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // When & Then - время в рабочие часы
        LocalDateTime openTime = LocalDateTime.of(2024, 2, 13, 12, 0);
        assertThat(workingHours.isOpenAt(openTime)).isTrue();

        // When & Then - время до открытия
        LocalDateTime beforeOpen = LocalDateTime.of(2024, 2, 13, 8, 0);
        assertThat(workingHours.isOpenAt(beforeOpen)).isFalse();

        // When & Then - время после закрытия
        LocalDateTime afterClose = LocalDateTime.of(2024, 2, 13, 23, 0);
        assertThat(workingHours.isOpenAt(afterClose)).isFalse();

        // When & Then - время открытия (граничное значение)
        LocalDateTime atOpenTime = LocalDateTime.of(2024, 2, 13, 9, 0);
        assertThat(workingHours.isOpenAt(atOpenTime)).isTrue();

        // When & Then - время закрытия (граничное значение)
        LocalDateTime atCloseTime = LocalDateTime.of(2024, 2, 13, 22, 0);
        assertThat(workingHours.isOpenAt(atCloseTime)).isTrue();
    }

    // ========== Тест 4: Круглосуточная работа ==========

    @Test
    @DisplayName("Должен поддерживать круглосуточную работу")
    void shouldSupport24HoursOperation() {
        // Given
        WorkingHours roundTheClock = new WorkingHours("0:00-23:59");
        LocalDateTime testTime = LocalDateTime.of(2024, 2, 13, 15, 30);

        // When & Then
        assertThat(roundTheClock.isOpenAt(testTime)).isTrue();
        assertThat(roundTheClock.isClosed()).isFalse();
    }

    // ========== Тест 5: Проверка isClosed ==========

    @Test
    @DisplayName("Должен определить закрыт ли магазин")
    void shouldCheckIfStoreIsClosed() {
        // Given
        WorkingHours workingHours = new WorkingHours("9:00-22:00");
        LocalDateTime closedTime = LocalDateTime.of(2024, 2, 13, 23, 0);

        // When & Then
        assertThat(workingHours.isOpenAt(closedTime)).isFalse();
        assertThat(workingHours.isClosed()).isFalse(); // isClosed проверяет сам объект, не время
    }

    // ========== Тест 6: Равенство и хеш-код ==========

    @Test
    @DisplayName("Должен корректно сравнивать объекты по значению")
    void shouldCorrectlyCompareObjectsByValue() {
        // Given
        WorkingHours hours1 = new WorkingHours("9:00-22:00");
        WorkingHours hours2 = new WorkingHours("9:00-22:00");
        WorkingHours hours3 = new WorkingHours("10:00-23:00");

        // When & Then
        assertThat(hours1).isEqualTo(hours2);
        assertThat(hours1).isNotEqualTo(hours3);
        assertThat(hours1.hashCode()).isEqualTo(hours2.hashCode());
        assertThat(hours1.hashCode()).isNotEqualTo(hours3.hashCode());
    }

    // ========== Тест 7: Получение времени ==========

    @Test
    @DisplayName("Должен возвращать корректные времена открытия и закрытия")
    void shouldReturnCorrectOpenAndCloseTimes() {
        // Given
        WorkingHours workingHours = new WorkingHours("9:30-20:45");

        // When & Then
        assertThat(workingHours.getOpenTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(workingHours.getCloseTime()).isEqualTo(LocalTime.of(20, 45));
        assertThat(workingHours.getValue()).isEqualTo("9:30-20:45");
    }

    // ========== Тест 8: Разные форматы ввода ==========

    @ParameterizedTest(name = "Формат: {0}")
    @MethodSource("provideValidFormats")
    @DisplayName("Должен поддерживать различные форматы времени")
    void shouldSupportDifferentTimeFormats(String input, LocalTime expectedOpen, LocalTime expectedClose) {
        // When
        WorkingHours workingHours = new WorkingHours(input);

        // Then
        assertThat(workingHours.getOpenTime()).isEqualTo(expectedOpen);
        assertThat(workingHours.getCloseTime()).isEqualTo(expectedClose);
    }

    private static Stream<Object[]> provideValidFormats() {
        return Stream.of(
            new Object[]{"9:00-22:00", LocalTime.of(9, 0), LocalTime.of(22, 0)},
            new Object[]{"9-22", LocalTime.of(9, 0), LocalTime.of(22, 0)},
            new Object[]{"9:30-20:45", LocalTime.of(9, 30), LocalTime.of(20, 45)},
            new Object[]{"09:00-22:00", LocalTime.of(9, 0), LocalTime.of(22, 0)}
        );
    }

    // ========== Тест 9: Null и пустая строка ==========

    @Test
    @DisplayName("Должен выбросить исключение для null")
    void shouldThrowExceptionForNull() {
        // When & Then
        assertThatThrownBy(() -> new WorkingHours(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Неверный формат времени");
    }

    // ========== Тест 10: Метод toString ==========

    @Test
    @DisplayName("Должен возвращать строковое представление")
    void shouldReturnStringValue() {
        // Given
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        // When
        String result = workingHours.toString();

        // Then
        assertThat(result).contains("9:00-22:00");
    }
}
