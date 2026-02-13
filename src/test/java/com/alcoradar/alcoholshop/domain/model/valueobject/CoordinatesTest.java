package com.alcoradar.alcoholshop.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты для Value Object Coordinates")
class CoordinatesTest {

    @ParameterizedTest(name = "Валидные координаты: широта={0}, долгота={1}")
    @CsvSource({
        "55.7558, 37.6173",   // Москва
        "59.9343, 30.3351",   // Санкт-Петербург
        "0, 0",              // Экватор и нулевой меридиан
        "-90, -180",         // Минимальные значения
        "90, 180"            // Максимальные значения
    })
    @DisplayName("Должен создать валидные координаты")
    void shouldCreateValidCoordinates(Double latitude, Double longitude) {
        // When
        Coordinates coordinates = new Coordinates(latitude, longitude);

        // Then
        assertThat(coordinates.getLatitude()).isEqualTo(latitude);
        assertThat(coordinates.getLongitude()).isEqualTo(longitude);
    }

    @ParameterizedTest(name = "Невалидные координаты: широта={0}, долгота={1}")
    @CsvSource({
        "91, 0",             // Широта > 90
        "-91, 0",            // Широта < -90
        "0, 181",            // Долгота > 180
        "0, -181"            // Долгота < -180
    })
    @DisplayName("Должен выбросить исключение для невалидных координат")
    void shouldThrowExceptionForInvalidCoordinates(Double latitude, Double longitude) {
        // When & Then
        assertThatThrownBy(() -> new Coordinates(latitude, longitude))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Координаты должны быть в диапазоне: широта [-90, 90], долгота [-180, 180]");
    }

    @Test
    @DisplayName("Должен вычислить расстояние между координатами")
    void shouldCalculateDistanceBetweenCoordinates() {
        // Given
        Coordinates moscow = new Coordinates(55.7558, 37.6173);
        Coordinates saintPetersburg = new Coordinates(59.9343, 30.3351);

        // When
        Double distance = moscow.distanceTo(saintPetersburg);

        // Then - расстояние между Москвой и Санкт-Петербургом примерно 630 км
        assertThat(distance).isBetween(600.0, 700.0);
    }

    @Test
    @DisplayName("Должен вычислить нулевое расстояние для одинаковых координат")
    void shouldCalculateZeroDistanceForSameCoordinates() {
        // Given
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);

        // When
        Double distance = coordinates.distanceTo(coordinates);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Координаты с одинаковыми значениями равны")
    void shouldConsiderCoordinatesWithSameValuesEqual() {
        // Given
        Coordinates coordinates1 = new Coordinates(55.7558, 37.6173);
        Coordinates coordinates2 = new Coordinates(55.7558, 37.6173);
        Coordinates coordinates3 = new Coordinates(59.9343, 30.3351);

        // Then & When
        assertThat(coordinates1)
            .isEqualTo(coordinates2)
            .isNotEqualTo(coordinates3);

        assertThat(coordinates1.hashCode())
            .isEqualTo(coordinates2.hashCode())
            .isNotEqualTo(coordinates3.hashCode());
    }

    @Test
    @DisplayName("Координаты не равны null")
    void shouldNotEqualToNull() {
        // Given
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);

        // Then
        assertThat(coordinates).isNotNull();
        assertThat(coordinates).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Граничные значения координат")
    void shouldHandleBoundaryValues() {
        // Given - граничные значения
        Coordinates maxLatitude = new Coordinates(90.0, 0.0);
        Coordinates minLatitude = new Coordinates(-90.0, 0.0);
        Coordinates maxLongitude = new Coordinates(0.0, 180.0);
        Coordinates minLongitude = new Coordinates(0.0, -180.0);

        // Then - все должны быть созданы успешно
        assertThat(maxLatitude.getLatitude()).isEqualTo(90.0);
        assertThat(minLatitude.getLatitude()).isEqualTo(-90.0);
        assertThat(maxLongitude.getLongitude()).isEqualTo(180.0);
        assertThat(minLongitude.getLongitude()).isEqualTo(-180.0);
    }
}
