# AlcoholShop Service - План реализации

**Цель:** Создать Spring Boot REST API сервис для управления алкомаркетами с использованием DDD и Clean Architecture

**Архитектура:** Hexagonal Architecture (Ports & Adapters), чистое разделение слоёв

**Tech Stack:** Spring Boot 3.3+, Java 21, Maven, PostgreSQL, JUnit 5 + AssertJ, Testcontainers

---

## Phase 1: Каркас проекта

### Задача 1.1: Создать Spring Boot проект

**Описание:** Создать базовый Spring Boot 3.3+ проект с Maven и базовыми зависимостями

**Цель:** Получить работающий каркас приложения

**Файлы:**
- Создать: `pom.xml`
- Создать: `src/main/resources/application.yml`

**Шаги:**

1. Создать `pom.xml` со следующими зависимостями:
   - spring-boot-starter-web (3.3+)
   - spring-boot-starter-data-jpa
   - spring-boot-starter-validation
   - postgresql
   - lombok
   - mapstruct

2. Создать базовую структуру пакетов:
   ```
   com.alcoradar.alcoholshop/
   ├── AlcoholShopApplication.java
   ├── domain/
   ├── application/
   ├── infrastructure/
   └── interfaces/
   ```

3. Создать `application.yml` с базовой конфигурацией

**Проверка:**
```bash
mvn clean compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-01): создать spring-boot проект с базовыми зависимостями"
```

---

### Задача 1.2: Добавить тестовые зависимости

**Описание:** Добавить зависимости для тестирования (JUnit 5, AssertJ, Mockito, Testcontainers)

**Цель:** Настроить окружение для написания тестов

**Файлы:**
- Модифицировать: `pom.xml`

**Шаги:**

1. Добавить в `pom.xml` тестовые зависимости:
   - spring-boot-starter-test (исключая JUnit assertions)
   - assertj-core
   - testcontainers (postgresql)
   - parameterized-tests

2. Настроить Maven для использования AssertJ вместо JUnit assertions

**Проверка:**
```bash
mvn clean test
# Ожидаем: BUILD SUCCESS (даже если нет тестов)
```

**Коммит:**
```bash
git add pom.xml
git commit -m "feat(25-02-13-02): добавить тестовые зависимости junit5 assertj mockito"
```

---

### Задача 1.3: Создать базовый Application и health check

**Описание:** Создать главный класс приложения и простой health check endpoint

**Цель:** Убедиться, что приложение запускается и отвечает

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/AlcoholShopApplication.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/interfaces/rest/HealthController.java`

**Шаги:**

1. Создать `AlcoholShopApplication.java` с @SpringBootApplication

2. Создать `HealthController` с endpoint:
   - GET /actuator/health
   - Возвращает: `{"status":"UP"}`

**Проверка:**
```bash
mvn spring-boot:run
# В другом терминале:
curl http://localhost:8080/actuator/health
# Ожидаем: {"status":"UP"}
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-03): создать health check endpoint для проверки запуска"
```

---

## Phase 2: Domain Layer - Value Objects

### Задача 2.1: Создать Coordinates Value Object

**Описание:** Создать Value Object для координат с валидацией

**Цель:** Получить переиспользуемый Value Object для геолокации

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/model/valueobject/Coordinates.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/domain/model/valueobject/CoordinatesTest.java`

**Шаги:**

1. Создать тест `CoordinatesTest.java`:
```java
@DisplayName("Проверка создания координат")
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
        Coordinates coordinates = new Coordinates(latitude, longitude);

        assertThat(coordinates)
            .isNotNull();
        assertThat(coordinates.getLatitude())
            .isEqualTo(latitude);
        assertThat(coordinates.getLongitude())
            .isEqualTo(longitude);
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
        assertThatThrownBy(() -> new Coordinates(latitude, longitude))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Координаты должны быть в диапазоне");
    }

    @Test
    @DisplayName("Должен вычислить расстояние между координатами")
    void shouldCalculateDistanceBetweenCoordinates() {
        Coordinates moscow = new Coordinates(55.7558, 37.6173);
        Coordinates saintPetersburg = new Coordinates(59.9343, 30.3351);

        Double distance = moscow.distanceTo(saintPetersburg);

        assertThat(distance)
            .isGreaterThan(600.0)  // ~630 км
            .isLessThan(700.0);
    }

    @Test
    @DisplayName("Координаты с одинаковыми значениями равны")
    void shouldConsiderCoordinatesWithSameValuesEqual() {
        Coordinates coords1 = new Coordinates(55.7558, 37.6173);
        Coordinates coords2 = new Coordinates(55.7558, 37.6173);

        assertThat(coords1)
            .isEqualTo(coords2)
            .hasSameHashCodeAs(coords2);
    }
}
```

2. Создать `Coordinates.java`:
   - Immutable final class
   - Private final Double latitude, longitude
   - Конструктор с валидацией (IllegalArgumentException если невалидно)
   - Метод distanceTo(Coordinates other) - возвращает км
   - equals(), hashCode() на основе значений

**Проверка:**
```bash
mvn test -Dtest=CoordinatesTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-04): создать Coordinates value object с валидацией"
```

---

### Задача 2.2: Создать PhoneNumber Value Object

**Описание:** Создать Value Object для телефона с валидацией и нормализацией

**Цель:** Получить переиспользуемый Value Object для телефонных номеров

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/model/valueobject/PhoneNumber.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/domain/model/valueobject/PhoneNumberTest.java`

**Шаги:**

1. Создать тест `PhoneNumberTest.java`:
```java
@DisplayName("Проверка Value Object для телефонного номера")
class PhoneNumberTest {

    @ParameterizedTest(name = "Валидный номер: {0}")
    @CsvSource({
        "+74951234567",
        "+79991234567",
        "+79161234567"
    })
    @DisplayName("Должен создать валидный номер телефона")
    void shouldCreateValidPhoneNumber(String phone) {
        PhoneNumber phoneNumber = new PhoneNumber(phone);

        assertThat(phoneNumber.getValue())
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
        assertThatThrownBy(() -> new PhoneNumber(phone))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Неверный формат номера");
    }

    @Test
    @DisplayName("Должен поддерживать пустой номер")
    void shouldSupportEmptyPhoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber(null);

        assertThat(phoneNumber.getValue())
            .isNull();
        assertThat(phoneNumber.isValid())
            .isFalse();
    }
}
```

2. Создать `PhoneNumber.java`:
   - Immutable final class
   - Private final String value (может быть null)
   - Конструктор с валидацией формата (+7XXXXXXXXXX, 11 цифр)
   - Метод isValid() - false если null
   - equals(), hashCode() на основе значения

**Проверка:**
```bash
mvn test -Dtest=PhoneNumberTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-05): создать PhoneNumber value object с валидацией"
```

---

### Задача 2.3: Создать WorkingHours Value Object

**Описание:** Создать Value Object для часов работы с валидацией формата

**Цель:** Получить Value Object для часов работы с бизнес-логикой

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/model/valueobject/WorkingHours.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/domain/model/valueobject/WorkingHoursTest.java`

**Шаги:**

1. Создать тест `WorkingHoursTest.java`:
```java
@DisplayName("Проверка Value Object для часов работы")
class WorkingHoursTest {

    @ParameterizedTest(name = "Валидное время: {0}")
    @CsvSource({
        "9:00-22:00",
        "0:00-23:59",
        "12:00-12:00",     // Круглосуточно
        "00:00-24:00"
    })
    @DisplayName("Должен создать валидное время работы")
    void shouldCreateValidWorkingHours(String hours) {
        WorkingHours workingHours = new WorkingHours(hours);

        assertThat(workingHours.getValue())
            .isEqualTo(hours);
    }

    @ParameterizedTest(name = "Невалидное время: {0}")
    @CsvSource({
        "25:00-22:00",     // Невалидный час
        "9:00-25:00",
        "9:00-22",         // Неполный формат
        "9:00",           // Только открытие
        "abc-def"
    })
    @DisplayName("Должен выбросить исключение для невалидного времени")
    void shouldThrowExceptionForInvalidWorkingHours(String hours) {
        assertThatThrownBy(() -> new WorkingHours(hours))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Неверный формат времени");
    }

    @Test
    @DisplayName("Должен проверить открыт ли магазин в указанное время")
    void shouldCheckIfStoreIsOpenAtSpecificTime() {
        WorkingHours workingHours = new WorkingHours("9:00-22:00");

        assertThat(workingHours.isOpenAt(LocalDateTime.of(2025, 2, 13, 12, 0)))
            .isTrue();
        assertThat(workingHours.isOpenAt(LocalDateTime.of(2025, 2, 13, 23, 0)))
            .isFalse();
    }
}
```

2. Создать `WorkingHours.java`:
   - Immutable final class
   - Private final String value
   - Конструктор с валидацией формата (HH:mm-HH:mm)
   - Метод isOpenAt(LocalDateTime dateTime)
   - Метод isClosed()
   - equals(), hashCode() на основе значения

**Проверка:**
```bash
mvn test -Dtest=WorkingHoursTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-06): создать WorkingHours value object с валидацией"
```

---

## Phase 3: Domain Layer - Entities и Repository Ports

### Задача 3.1: Создать ShopType Enum

**Описание:** Создать enum для типа магазина

**Цель:** Типизированное поле для классификации магазинов

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/model/ShopType.java`

**Шаги:**

1. Создать `ShopType.java`:
```java
public enum ShopType {
    SUPERMARKET,
    SPECIALTY,
    DUTY_FREE
}
```

**Проверка:**
```bash
mvn compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-07): создать ShopType enum"
```

---

### Задача 3.2: Создать AlcoholShop Domain Entity

**Описание:** Создать агрегат AlcoholShop с Value Objects

**Цель:** Центральная доменная сущность

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/model/AlcoholShop.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/domain/model/AlcoholShopTest.java`

**Шаги:**

1. Создать тест `AlcoholShopTest.java`:
```java
@DisplayName("Проверка сущности AlcoholShop")
class AlcoholShopTest {

    @Test
    @DisplayName("Должен создать алкомаркет с обязательными полями")
    void shouldCreateAlcoholShopWithRequiredFields() {
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);
        PhoneNumber phone = new PhoneNumber("+74951234567");
        WorkingHours hours = new WorkingHours("9:00-22:00");

        AlcoholShop shop = AlcoholShop.create(
            "Пятёрочка",
            "ул. Ленина, д. 1",
            coordinates,
            phone,
            hours,
            ShopType.SUPERMARKET
        );

        assertThat(shop)
            .isNotNull()
            .extracting(
                AlcoholShop::getName,
                AlcoholShop::getAddress,
                AlcoholShop::getShopType
            )
            .containsExactly(
                "Пятёрочка",
                "ул. Ленина, д. 1",
                ShopType.SUPERMARKET
            );
        assertThat(shop.getId())
            .isNotNull();
        assertThat(shop.getCreatedAt())
            .isNotNull();
    }

    @Test
    @DisplayName("Должен выбросить исключение для пустого названия")
    void shouldThrowExceptionForEmptyName() {
        Coordinates coordinates = new Coordinates(55.7558, 37.6173);

        assertThatThrownBy(() -> AlcoholShop.create(
            "",  // Пустое название
            "ул. Ленина, д. 1",
            coordinates,
            new PhoneNumber("+74951234567"),
            new WorkingHours("9:00-22:00"),
            ShopType.SUPERMARKET
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Название не может быть пустым");
    }

    @Test
    @DisplayName("Должен обновить данные алкомаркета")
    void shouldUpdateAlcoholShopData() {
        AlcoholShop shop = createTestShop();

        shop.updateName("Новое название");
        shop.updateAddress("Новый адрес");

        assertThat(shop.getName())
            .isEqualTo("Новое название");
        assertThat(shop.getAddress())
            .isEqualTo("Новый адрес");
    }

    private AlcoholShop createTestShop() {
        return AlcoholShop.create(
            "Пятёрочка",
            "ул. Ленина, д. 1",
            new Coordinates(55.7558, 37.6173),
            new PhoneNumber("+74951234567"),
            new WorkingHours("9:00-22:00"),
            ShopType.SUPERMARKET
        );
    }
}
```

2. Создать `AlcoholShop.java`:
   - Public static factory method: `create(...)`
   - Private constructor
   - Поля: UUID id, String name, String address, Coordinates, PhoneNumber, WorkingHours, ShopType, LocalDateTime createdAt
   - Методы updateName(), updateAddress()
   - Валидация: name не пустой, address не пустой

**Проверка:**
```bash
mvn test -Dtest=AlcoholShopTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-08): создать AlcoholShop domain entity с валидацией"
```

---

### Задача 3.3: Создать Repository Port Interface

**Описание:** Создать интерфейс порта репозитория в domain слое

**Цель:** Определить контракт для доступа к данным

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/repository/AlcoholShopRepository.java`

**Шаги:**

1. Создать `AlcoholShopRepository.java`:
```java
public interface AlcoholShopRepository {
    AlcoholShop save(AlcoholShop shop);
    Optional<AlcoholShop> findById(UUID id);
    List<AlcoholShop> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
```

**Проверка:**
```bash
mvn compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-09): создать AlcoholShopRepository port interface"
```

---

### Задача 3.4: Создать Domain Exceptions

**Описание:** Создать иерархию domain исключений

**Цель:** Типизированная обработка ошибок домена

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/exception/DomainException.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/domain/exception/AlcoholShopNotFoundException.java`

**Шаги:**

1. Создать базовый класс `DomainException` extends RuntimeException

2. Создать `AlcoholShopNotFoundException` extends DomainException

**Проверка:**
```bash
mvn compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-10): создать domain exceptions hierarchy"
```

---

## Phase 4: Infrastructure Layer

### Задача 4.1: Настроить PostgreSQL и Flyway

**Описание:** Настроить подключение к PostgreSQL и миграции

**Цель:** Рабочая база данных

**Файлы:**
- Модифицировать: `src/main/resources/application.yml`
- Создать: `src/main/resources/db/migration/V1__create_alcohol_shops_table.sql`

**Шаги:**

1. Добавить в `pom.xml`:
   - flyway
   - postgresql driver

2. Настроить `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alcoholshop
    username: alcoholshop
    password: alcoholshop
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

3. Создать миграцию `V1__create_alcohol_shops_table.sql`:
```sql
CREATE TABLE alcohol_shops (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    phone_number VARCHAR(20),
    working_hours VARCHAR(50),
    shop_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_alcohol_shops_name ON alcohol_shops(name);
CREATE INDEX idx_alcohol_shops_type ON alcohol_shops(shop_type);
```

**Проверка:**
```bash
# Запустить PostgreSQL:
docker run -d -p 5432:5432 -e POSTGRES_DB=alcoholshop -e POSTGRES_USER=alcoholshop -e POSTGRES_PASSWORD=alcoholshop postgres:16

mvn spring-boot:run
# Ожидаем: Приложение запускается, миграции применяются
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-11): настроить postgresql и flyway миграции"
```

---

### Задача 4.2: Создать AlcoholShopEntity JPA

**Описание:** Создать JPA entity для маппинга в базу данных

**Цель:** Сущность для персистентности

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/infrastructure/persistence/entity/AlcoholShopEntity.java`

**Шаги:**

1. Создать `AlcoholShopEntity.java`:
```java
@Entity
@Table(name = "alcohol_shops")
class AlcoholShopEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String phoneNumber;

    @Column
    private String workingHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopType shopType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Getters, setters
    // toDomain() method
}
```

**Проверка:**
```bash
mvn compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-12): создать AlcoholShopEntity JPA entity"
```

---

### Задача 4.3: Создать Repository Implementation

**Описание:** Реализовать порт репозитория через Spring Data JPA

**Цель:** Рабочий репозиторий

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/infrastructure/persistence/repository/SpringDataAlcoholShopRepository.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/infrastructure/persistence/repository/AlcoholShopRepositoryImpl.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/infrastructure/persistence/repository/AlcoholShopRepositoryIntegrationTest.java`

**Шаги:**

1. Создать Spring Data интерфейс:
```java
interface SpringDataAlcoholShopRepository extends JpaRepository<AlcoholShopEntity, UUID> {
}
```

2. Создать `AlcoholShopRepositoryImpl.java`:
```java
@Repository
@RequiredArgsConstructor
class AlcoholShopRepositoryImpl implements AlcoholShopRepository {

    private final SpringDataAlcoholShopRepository springDataRepository;

    @Override
    public AlcoholShop save(AlcoholShop shop) {
        // Entity -> Domain mapping
    }

    @Override
    public Optional<AlcoholShop> findById(UUID id) {
        // ...
    }
    // ...
}
```

3. Создать интеграционный тест `AlcoholShopRepositoryIntegrationTest.java`:
```java
@SpringBootTest
@Testcontainers
@DisplayName("Интеграционные тесты репозитория")
class AlcoholShopRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AlcoholShopRepository repository;

    @Test
    @DisplayName("Должен сохранить и найти алкомаркет по ID")
    void shouldSaveAndFindById() {
        AlcoholShop shop = createTestShop();

        AlcoholShop saved = repository.save(shop);
        Optional<AlcoholShop> found = repository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(s -> {
                assertThat(s.getName()).isEqualTo("Пятёрочка");
                assertThat(s.getAddress()).isEqualTo("ул. Ленина, д. 1");
            });
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional для несуществующего ID")
    void shouldReturnEmptyForNonExistentId() {
        Optional<AlcoholShop> found = repository.findById(UUID.randomUUID());

        assertThat(found)
            .isEmpty();
    }

    private AlcoholShop createTestShop() {
        return AlcoholShop.create(
            "Пятёрочка",
            "ул. Ленина, д. 1",
            new Coordinates(55.7558, 37.6173),
            new PhoneNumber("+74951234567"),
            new WorkingHours("9:00-22:00"),
            ShopType.SUPERMARKET
        );
    }
}
```

**Проверка:**
```bash
mvn test -Dtest=AlcoholShopRepositoryIntegrationTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-13): реализовать AlcoholShopRepository с интеграционными тестами"
```

---

## Phase 5: Application Layer

### Задача 5.1: Создать DTOs

**Описание:** Создать DTOs для REST API

**Цель:** Контракты для API

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/application/dto/CreateAlcoholShopRequest.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/application/dto/AlcoholShopResponse.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/application/dto/CoordinatesDto.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/application/dto/PageResponse.java`

**Шаги:**

1. Создать `CreateAlcoholShopRequest.java`:
```java
public record CreateAlcoholShopRequest(
    @NotBlank String name,
    @NotBlank @Size(min = 10, max = 500) String address,
    @Valid CoordinatesDto coordinates,
    String phoneNumber,
    String workingHours,
    ShopType shopType
) {
}

public record CoordinatesDto(
    @Min(-90) @Max(90) Double latitude,
    @Min(-180) @Max(180) Double longitude
) {
}
```

2. Создать `AlcoholShopResponse.java`:
```java
public record AlcoholShopResponse(
    UUID id,
    String name,
    String address,
    CoordinatesDto coordinates,
    String phoneNumber,
    String workingHours,
    ShopType shopType,
    LocalDateTime createdAt
) {
}
```

3. Создать `PageResponse.java`:
```java
public record PageResponse<T>(
    List<T> content,
    int currentPage,
    int pageSize,
    long totalElements,
    int totalPages
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
```

**Проверка:**
```bash
mvn compile
# Ожидаем: BUILD SUCCESS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-14): создать DTOs для API"
```

---

### Задача 5.2: Создать AlcoholShopUseCase

**Описание:** Создать Use Case для операций с алкомаркетами

**Цель:** Бизнес-логика приложения

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/application/usecase/AlcoholShopUseCase.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/application/usecase/AlcoholShopUseCaseTest.java`

**Шаги:**

1. Создать `AlcoholShopUseCase.java`:
```java
@Service
@RequiredArgsConstructor
public class AlcoholShopUseCase {

    private final AlcoholShopRepository repository;

    public AlcoholShopResponse create(CreateAlcoholShopRequest request) {
        // Маппинг DTO -> Domain
        // Валидация
        // Сохранение
        // Маппинг Domain -> DTO
    }

    public AlcoholShopResponse findById(UUID id) {
        // Поиск или выброс исключения
    }

    public PageResponse<AlcoholShopResponse> findAll(int page, int size, String sortBy) {
        // Получение всех с пагинацией
    }
}
```

2. Создать тест `AlcoholShopUseCaseTest.java`:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit тесты для AlcoholShopUseCase")
class AlcoholShopUseCaseTest {

    @Mock
    private AlcoholShopRepository repository;

    @InjectMocks
    private AlcoholShopUseCase useCase;

    @Test
    @DisplayName("Должен создать алкомаркет")
    void shouldCreateAlcoholShop() {
        // Given
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
            "Пятёрочка",
            "ул. Ленина, д. 1",
            new CoordinatesDto(55.7558, 37.6173),
            "+74951234567",
            "9:00-22:00",
            ShopType.SUPERMARKET
        );

        AlcoholShop shop = createTestShop();
        when(repository.save(any())).thenReturn(shop);

        // When
        AlcoholShopResponse response = useCase.create(request);

        // Then
        assertThat(response)
            .isNotNull();
        assertThat(response.name()).isEqualTo("Пятёрочка");

        verify(repository).save(any());
    }

    @Test
    @DisplayName("Должен выбросить исключение если магазин не найден")
    void shouldThrowExceptionWhenShopNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findById(id))
            .isInstanceOf(AlcoholShopNotFoundException.class);

        verify(repository).findById(id);
    }
}
```

**Проверка:**
```bash
mvn test -Dtest=AlcoholShopUseCaseTest
# Ожидаем: Все тесты PASS
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-15): создать AlcoholShopUseCase с unit тестами"
```

---

## Phase 6: Interfaces Layer - REST API

### Задача 6.1: Создать AlcoholShopController

**Описание:** Создать REST контроллер с endpoints

**Цель:** Работающий REST API

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/interfaces/rest/AlcoholShopController.java`
- Создать: `src/test/java/com/alcoradar/alcoholshop/interfaces/rest/AlcoholShopControllerIntegrationTest.java`

**Шаги:**

1. Создать `AlcoholShopController.java`:
```java
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class AlcoholShopController {

    private final AlcoholShopUseCase useCase;

    @PostMapping
    ResponseEntity<AlcoholShopResponse> create(@Valid @RequestBody CreateAlcoholShopRequest request) {
        AlcoholShopResponse response = useCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<AlcoholShopResponse> findById(@PathVariable UUID id) {
        AlcoholShopResponse response = useCase.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    ResponseEntity<PageResponse<AlcoholShopResponse>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sortBy
    ) {
        PageResponse<AlcoholShopResponse> response = useCase.findAll(page, size, sortBy);
        return ResponseEntity.ok(response);
    }
}
```

2. Создать интеграционный тест `AlcoholShopControllerIntegrationTest.java`:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@DisplayName("Интеграционные тесты REST API")
class AlcoholShopControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Должен создать алкомаркет через POST /api/shops")
    void shouldCreateAlcoholShop() {
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
            "Пятёрочка",
            "ул. Ленина, д. 1",
            new CoordinatesDto(55.7558, 37.6173),
            "+74951234567",
            "9:00-22:00",
            ShopType.SUPERMARKET
        );

        ResponseEntity<AlcoholShopResponse> response = restTemplate.postForEntity(
            "/api/shops",
            request,
            AlcoholShopResponse.class
        );

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
            .isNotNull()
            .extracting(AlcoholShopResponse::name, AlcoholShopResponse::address)
            .containsExactly("Пятёрочка", "ул. Ленина, д. 1");
    }

    @Test
    @DisplayName("Должен вернуть 400 для невалидных данных")
    void shouldReturnBadRequestForInvalidData() {
        CreateAlcoholShopRequest request = new CreateAlcoholShopRequest(
            "",  // Пустое название
            "ул. Ленина, д. 1",
            new CoordinatesDto(55.7558, 37.6173),
            "+74951234567",
            "9:00-22:00",
            ShopType.SUPERMARKET
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/shops",
            request,
            ErrorResponse.class
        );

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
```

**Проверка:**
```bash
mvn test -Dtest=AlcoholShopControllerIntegrationTest
# Ожидаем: Все тесты PASS

# Ручная проверка:
mvn spring-boot:run
curl -X POST http://localhost:8080/api/shops \
  -H "Content-Type: application/json" \
  -d '{"name":"Пятёрочка","address":"ул. Ленина, д. 1","coordinates":{"latitude":55.7558,"longitude":37.6173},"phoneNumber":"+74951234567","workingHours":"9:00-22:00","shopType":"SUPERMARKET"}'
# Ожидаем: 201 Created с AlcoholShopResponse
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-16): создать REST API endpoints с интеграционными тестами"
```

---

### Задача 6.2: Создать Exception Handler

**Описание:** Создать глобальный обработчик исключений

**Цель:** Корректные HTTP ответы для ошибок

**Файлы:**
- Создать: `src/main/java/com/alcoradar/alcoholshop/interfaces/rest/RestExceptionHandler.java`
- Создать: `src/main/java/com/alcoradar/alcoholshop/interfaces/rest/ErrorResponse.java`

**Шаги:**

1. Создать `ErrorResponse.java`:
```java
public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
}
```

2. Создать `RestExceptionHandler.java`:
```java
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AlcoholShopNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(AlcoholShopNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, message, LocalDateTime.now()));
    }
}
```

3. Добавить тест в `AlcoholShopControllerIntegrationTest`:
```java
@Test
@DisplayName("Должен вернуть 404 для несуществующего ID")
void shouldReturnNotFoundForNonExistentId() {
    ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
        "/api/shops/" + UUID.randomUUID(),
        ErrorResponse.class
    );

    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
}
```

**Проверка:**
```bash
mvn test -Dtest=AlcoholShopControllerIntegrationTest
# Ожидаем: Все тесты PASS

# Ручная проверка:
curl http://localhost:8080/api/shops/00000000-0000-0000-0000-000000000000
# Ожидаем: 404 Not Found
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-17): создать глобальный exception handler"
```

---

## Phase 7: Финализация

### Задача 7.1: Создать README с инструкциями

**Описание:** Создать документацию для запуска проекта

**Цель:** Понятные инструкции для разработчика

**Файлы:**
- Создать: `README.md`

**Шаги:**

1. Создать `README.md` с:
   - Описание проекта
   - Требования (Java 21+, Docker)
   - Инструкции по запуску PostgreSQL
   - Инструкции по сборке и запуску
   - Примеры API запросов
   - Структура проекта

**Проверка:**
```bash
cat README.md
# Ожидаем: Полная документация
```

**Коммит:**
```bash
git add .
git commit -m "docs(25-02-13-18): добавить README с инструкциями"
```

---

### Задача 7.2: Проверить coverage

**Описание:** Запустить все тесты и проверить покрытие кода

**Цель:** Убедиться что coverage >80%

**Шаги:**

1. Запустить все тесты с coverage:
```bash
mvn clean test jacoco:report
```

2. Проверить отчёт:
```bash
cat target/site/jacoco/index.html
```

**Проверка:**
```bash
# Ожидаем: Coverage >80% для domain и application слоев
```

**Коммит:**
```bash
git add .
git commit -m "test(25-02-13-19): проверить test coverage"
```

---

### Задача 7.3: Финальная проверка всех endpoints

**Описание:** Ручная проверка всех API endpoints

**Цель:** Убедиться что всё работает

**Шаги:**

1. Запустить приложение и проверить все endpoints:
```bash
# 1. Создать магазин
curl -X POST http://localhost:8080/api/shops \
  -H "Content-Type: application/json" \
  -d '{"name":"Пятёрочка","address":"ул. Ленина, д. 1","coordinates":{"latitude":55.7558,"longitude":37.6173},"phoneNumber":"+74951234567","workingHours":"9:00-22:00","shopType":"SUPERMARKET"}'

# 2. Получить по ID (используя ID из первого запроса)
curl http://localhost:8080/api/shops/{id}

# 3. Получить список
curl http://localhost:8080/api/shops?page=0&size=10&sort=name,asc
```

**Проверка:**
```bash
# Ожидаем: Все endpoints работают корректно
```

**Коммит:**
```bash
git add .
git commit -m "feat(25-02-13-20): финальная проверка и завершение проекта"
```

---

## Итоговая проверка

После выполнения всех задач у вас должно быть:

✓ Работающий Spring Boot 3.3+ проект
✓ REST API для создания и получения алкомаркетов
✓ PostgreSQL база данных
✓ Hexagonal Architecture (чистое разделение слоёв)
✓ Value Objects для Coordinates, PhoneNumber, WorkingHours
✓ Unit тесты с AssertJ и параметризацией
✓ Интеграционные тесты с Testcontainers
✓ Coverage >80%
✓ README с документацией
✓ 20+ git коммитов с осмысленными сообщениями
