package com.alcoradar.alcoholshop.domain.model.valueobject;

/**
 * Value Object для представления географических координат.
 * Неизменяемый (immutable) класс с валидацией значений.
 */
public final class Coordinates {

    private final Double latitude;
    private final Double longitude;

    /**
     * Создаёт экземпляр координат с валидацией.
     *
     * @param latitude  широта, должна быть в диапазоне [-90, 90]
     * @param longitude долгота, должна быть в диапазоне [-180, 180]
     * @throws IllegalArgumentException если координаты вне допустимого диапазона
     */
    public Coordinates(Double latitude, Double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Валидирует широту.
     */
    private void validateLatitude(Double latitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                "Координаты должны быть в диапазоне: широта [-90, 90], долгота [-180, 180]"
            );
        }
    }

    /**
     * Валидирует долготу.
     */
    private void validateLongitude(Double longitude) {
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                "Координаты должны быть в диапазоне: широта [-90, 90], долгота [-180, 180]"
            );
        }
    }

    /**
     * @return широта в градусах
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return долгота в градусах
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Вычисляет расстояние до другой точки координат по формуле Haversine.
     *
     * @param other целевые координаты
     * @return расстояние в километрах
     */
    public Double distanceTo(Coordinates other) {
        if (other == null) {
            throw new IllegalArgumentException("Целевые координаты не могут быть null");
        }

        // Радиус Земли в километрах
        final double EARTH_RADIUS = 6371.0;

        // Переводим градусы в радианы
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        // Формула Haversine
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Coordinates that = (Coordinates) o;

        return latitude.equals(that.latitude) && longitude.equals(that.longitude);
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Coordinates{latitude=%.6f, longitude=%.6f}", latitude, longitude);
    }
}
