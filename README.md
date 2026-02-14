# AlcoholShop Service

REST API сервис для управления алкомаркетами с использованием Domain-Driven Design и Hexagonal Architecture.

## Описание проекта

AlcoholShop Service - это современное Spring Boot приложение, реализующее REST API для CRUD операций над сущностью алкомаркета. Проект построен с использованием принципов Clean Architecture и Domain-Driven Design.

### Архитектура

Проект реализует **Hexagonal Architecture (Ports & Adapters)** с четким разделением слоев:

- **Domain Layer** - ядро бизнес-логики (DDD сущности, Value Objects, доменные исключения)
- **Application Layer** - use cases и DTO для взаимодействия
- **Infrastructure Layer** - технические детали (JPA репозитории, сущности БД)
- **Interfaces Layer** - REST контроллеры и обработка исключений

### Технологический стек

| Технология | Версия | Описание |
|------------|--------|----------|
| **Java** | 21 | LTS версия с виртуальными потоками |
| **Spring Boot** | 3.3.6 | Фреймворк для создания REST API |
| **PostgreSQL** | 16 | Реляционная СУБД |
| **Flyway** | Latest | Управление миграциями БД |
| **Lombok** | Latest | Генерация кода (boilerplate reduction) |
| **MapStruct** | 1.6.3 | Mapping между DTO и сущностями |
| **Testcontainers** | 1.20.1 | Интеграционные тесты с реальной БД |
| **Maven** | 3.9+ | Система сборки |

### Возможности API

- Создание алкомаркета с валидацией данных
- Получение информации о магазине по ID
- Поиск магазинов с пагинацией и сортировкой
- Обновление информации о магазине
- Удаление магазина
- Health check endpoint для мониторинга
- Глобальная обработка ошибок

## Требования

Для запуска проекта необходимы:

- **Java 21+** ([Download](https://jdk.java.net/21/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **Docker** ([Download](https://docs.docker.com/get-docker/)) - для запуска PostgreSQL
- **Docker Compose** - для управления контейнерами

### Проверка версий

```bash
java -version    # Ожидается: openjdk version "21.x.x"
mvn -version     # Ожидается: Apache Maven 3.9.x
docker --version # Ожидается: Docker version 24.x.x
```

## Быстрый старт

### 1. Клонирование репозитория

```bash
cd /home/sendel/myaiproject/java
```

### 2. Запуск PostgreSQL

Запустите контейнер с PostgreSQL в фоновом режиме:

```bash
docker-compose up -d
```

Проверьте статус контейнера:

```bash
docker-compose ps
```

Ожидаемый вывод:
```
NAME                    STATUS          PORTS
alcoholshop-postgres    Up (healthy)    0.0.0.0:5432->5432/tcp
```

### 3. Сборка проекта

```bash
mvn clean install
```

Эта команда:
- Очищает предыдущие артефакты
- Компилирует исходный код
- Запускает все тесты (unit + integration)
- Создает JAR файл в `target/`

### 4. Запуск приложения

После успешной сборки запустите приложение:

```bash
mvn spring-boot:run
```

Или используйте напрямую JAR:

```bash
java -jar target/alcohol-shop-1.0.0-SNAPSHOT.jar
```

### 5. Проверка работоспособности

Когда приложение запущено, проверьте health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Ожидаемый ответ:
```json
{
  "status": "UP"
}
```

## API Документация

### Базовый URL

```
http://localhost:8080/api
```

### Endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/shops` | Создать новый алкомаркет |
| GET | `/api/shops/{id}` | Получить магазин по ID |
| GET | `/api/shops` | Получить список магазинов с пагинацией |
| PUT | `/api/shops/{id}` | Обновить информацию о магазине |
| DELETE | `/api/shops/{id}` | Удалить магазин |
| GET | `/actuator/health` | Health check |

### Модели данных

#### ShopType (Тип магазина)

```json
{
  "SUPERMARKET": "Супермаркет",
  "CONVENIENCE_STORE": "Универсам",
  "DUTY_FREE": "Duty Free",
  "SPECIALTY": "Специализированный магазин"
}
```

#### CreateAlcoholShopRequest

```json
{
  "name": "Пятёрочка",
  "address": "ул. Ленина, д. 1",
  "coordinates": {
    "latitude": 55.7558,
    "longitude": 37.6173
  },
  "phoneNumber": "+74951234567",
  "workingHours": "9:00-22:00",
  "shopType": "SUPERMARKET"
}
```

#### AlcoholShopResponse

```json
{
  "id": 1,
  "name": "Пятёрочка",
  "address": "ул. Ленина, д. 1",
  "coordinates": {
    "latitude": 55.7558,
    "longitude": 37.6173
  },
  "phoneNumber": "+74951234567",
  "workingHours": "9:00-22:00",
  "shopType": "SUPERMARKET",
  "createdAt": "2025-02-14T10:30:00",
  "updatedAt": "2025-02-14T10:30:00"
}
```

### Примеры запросов

#### 1. Создание магазина

```bash
curl -X POST http://localhost:8080/api/shops \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Пятёрочка",
    "address": "ул. Ленина, д. 1",
    "coordinates": {
      "latitude": 55.7558,
      "longitude": 37.6173
    },
    "phoneNumber": "+74951234567",
    "workingHours": "9:00-22:00",
    "shopType": "SUPERMARKET"
  }'
```

**Ответ (201 Created):**
```json
{
  "id": 1,
  "name": "Пятёрочка",
  "address": "ул. Ленина, д. 1",
  "coordinates": {
    "latitude": 55.7558,
    "longitude": 37.6173
  },
  "phoneNumber": "+74951234567",
  "workingHours": "9:00-22:00",
  "shopType": "SUPERMARKET",
  "createdAt": "2025-02-14T10:30:00",
  "updatedAt": "2025-02-14T10:30:00"
}
```

#### 2. Получение магазина по ID

```bash
curl http://localhost:8080/api/shops/1
```

**Ответ (200 OK):**
```json
{
  "id": 1,
  "name": "Пятёрочка",
  "address": "ул. Ленина, д. 1",
  "coordinates": {
    "latitude": 55.7558,
    "longitude": 37.6173
  },
  "phoneNumber": "+74951234567",
  "workingHours": "9:00-22:00",
  "shopType": "SUPERMARKET",
  "createdAt": "2025-02-14T10:30:00",
  "updatedAt": "2025-02-14T10:30:00"
}
```

#### 3. Получение списка с пагинацией

```bash
curl "http://localhost:8080/api/shops?page=0&size=10&sortBy=name"
```

**Ответ (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Пятёрочка",
      "address": "ул. Ленина, д. 1",
      "coordinates": {
        "latitude": 55.7558,
        "longitude": 37.6173
      },
      "phoneNumber": "+74951234567",
      "workingHours": "9:00-22:00",
      "shopType": "SUPERMARKET",
      "createdAt": "2025-02-14T10:30:00",
      "updatedAt": "2025-02-14T10:30:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "totalPages": 1,
    "totalElements": 1
  }
}
```

**Параметры запроса:**

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `page` | Integer | Нет | Номер страницы (по умолчанию 0) |
| `size` | Integer | Нет | Размер страницы (по умолчанию 10) |
| `sortBy` | String | Нет | Поле для сортировки (по умолчанию `id`) |

#### 4. Обновление магазина

```bash
curl -X PUT http://localhost:8080/api/shops/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Пятёрочка (Обновленный)",
    "address": "ул. Ленина, д. 2",
    "coordinates": {
      "latitude": 55.7560,
      "longitude": 37.6175
    },
    "phoneNumber": "+74951234568",
    "workingHours": "8:00-23:00",
    "shopType": "SUPERMARKET"
  }'
```

**Ответ (200 OK):**
```json
{
  "id": 1,
  "name": "Пятёрочка (Обновленный)",
  "address": "ул. Ленина, д. 2",
  "coordinates": {
    "latitude": 55.7560,
    "longitude": 37.6175
  },
  "phoneNumber": "+74951234568",
  "workingHours": "8:00-23:00",
  "shopType": "SUPERMARKET",
  "createdAt": "2025-02-14T10:30:00",
  "updatedAt": "2025-02-14T11:00:00"
}
```

#### 5. Удаление магазина

```bash
curl -X DELETE http://localhost:8080/api/shops/1
```

**Ответ (204 No Content)** - тело ответа отсутствует

## Authentication

### Login Request

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
      "username": "admin",
      "password": "Admin123!"
    }'
```

### Login Response (200 OK)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5c6...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5c6...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### Using Access Token

```bash
curl -X GET http://localhost:8080/api/shops \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5c6..."
```

### Error Response (401 Unauthorized)

```json
{
  "status": 401,
  "message": "Invalid username or password"
}
```

### Обработка ошибок

#### Магазин не найден (404 Not Found)

```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Alcohol shop not found with id: 999",
  "path": "/api/shops/999"
}
```

#### Ошибка валидации (400 Bad Request)

```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name must not be blank"
    }
  ]
}
```

## Структура проекта

```
alcohol-shop/
├── src/
│   ├── main/
│   │   ├── java/com/alcoradar/alcoholshop/
│   │   │   ├── AlcoholShopApplication.java      # Главный класс Spring Boot
│   │   │   ├── domain/                           # Доменный слой (DDD)
│   │   │   │   ├── model/
│   │   │   │   │   ├── AlcoholShop.java         # Агрегат (сущность)
│   │   │   │   │   ├── ShopType.java            # Enum типа магазина
│   │   │   │   │   └── valueobject/
│   │   │   │   │       ├── Coordinates.java     # Value Object (координаты)
│   │   │   │   │       ├── PhoneNumber.java     # Value Object (телефон)
│   │   │   │   │       └── WorkingHours.java    # Value Object (часы работы)
│   │   │   │   ├── repository/
│   │   │   │   │   └── AlcoholShopRepository.java # Порт репозитория
│   │   │   │   └── exception/
│   │   │   │       ├── DomainException.java     # Базовое исключение
│   │   │   │       └── AlcoholShopNotFoundException.java
│   │   │   ├── application/                      # Слой приложения
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateAlcoholShopRequest.java
│   │   │   │   │   ├── UpdateAlcoholShopRequest.java
│   │   │   │   │   ├── AlcoholShopResponse.java
│   │   │   │   │   ├── CoordinatesDto.java
│   │   │   │   │   └── PageResponse.java
│   │   │   │   └── usecase/
│   │   │   │       └── AlcoholShopUseCase.java  # Use Cases (бизнес-логика)
│   │   │   ├── infrastructure/                   # Инфраструктурный слой
│   │   │   │   └── persistence/
│   │   │   │       ├── entity/
│   │   │   │       │   └── AlcoholShopEntity.java # JPA сущность
│   │   │   │       ├── repository/
│   │   │   │       │   ├── SpringDataAlcoholShopRepository.java
│   │   │   │       │   └── AlcoholShopRepositoryImpl.java # Адаптер репозитория
│   │   │   │       └── mapper/
│   │   │   │           └── AlcoholShopMapper.java  # MapStruct mapper
│   │   │   └── interfaces/                       # Слой интерфейсов
│   │   │       └── rest/
│   │   │           ├── AlcoholShopController.java  # REST контроллер
│   │   │           ├── GlobalExceptionHandler.java # Глобальный обработчик ошибок
│   │   │           └── HealthController.java      # Health check
│   │   └── resources/
│   │       ├── application.yml                    # Конфигурация Spring Boot
│   │       └── db/migration/                      # Flyway миграции
│   │           ├── V1__create_alcohol_shops_table.sql
│   │           └── V2__make_shop_type_nullable.sql
│   └── test/
│       └── java/com/alcoradar/alcoholshop/
│           ├── domain/                            # Unit тесты домена
│           ├── application/                       # Unit тесты use cases
│           └── integration/                      # Интеграционные тесты
├── docker-compose.yml                             # PostgreSQL контейнер
├── pom.xml                                       # Maven конфигурация
└── README.md                                      # Этот файл
```

### Слои архитектуры

```
┌─────────────────────────────────────────────────────────┐
│                   Interfaces Layer                       │
│  (REST Controllers, Exception Handlers, DTOs)           │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                 Application Layer                        │
│              (Use Cases, DTOs, Mappers)                 │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                         │
│   (Entities, Value Objects, Domain Services, Ports)    │
└─────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────┐
│                Infrastructure Layer                      │
│      (JPA Entities, Repository Implementations)         │
└─────────────────────────────────────────────────────────┘
```

## Конфигурация

### application.yml

Основные параметры конфигурации:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alcoholshop
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate  # Не создавать таблицы автоматически
    show-sql: false       # Логирование SQL запросов

  flyway:
    enabled: true         # Автоматический запуск миграций

server:
  port: 8080             # Порт приложения
```

### Переменные окружения

Для переопределения конфигурации используйте переменные окружения:

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `DB_URL` | URL базы данных | `jdbc:postgresql://localhost:5432/alcoholshop` |
| `DB_USERNAME` | Имя пользователя БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `postgres` |
| `SERVER_PORT` | Порт приложения | `8080` |
| `SPRING_PROFILES_ACTIVE` | Активный профиль | `dev` |

Пример запуска с переопределением:

```bash
export DB_URL=jdbc:postgresql://production-host:5432/alcoholshop
export DB_USERNAME=app_user
export DB_PASSWORD=secure_password
mvn spring-boot:run
```

## Тестирование

### Запуск всех тестов

```bash
mvn test
```

### Запуск только unit тестов

```bash
mvn test -DskipITs
```

### Запуск только интеграционных тестов

```bash
mvn verify -DskipUTs
```

### Покрытие тестами

Для просмотра отчета о покрытии:

```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

### Типы тестов

- **Unit тесты** - тестируют individual классы в изоляции
- **Integration тесты** - используют Testcontainers для реальной PostgreSQL БД

## Сборка в production

### Сборка оптимизированного JAR

```bash
mvn clean package -DskipTests
```

### Запуск с production профилем

```bash
java -jar target/alcohol-shop-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker образ (опционально)

Создайте `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/alcohol-shop-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Сборка образа:

```bash
docker build -t alcohol-shop:1.0.0 .
```

Запуск:

```bash
docker run -p 8080:8080 alcohol-shop:1.0.0
```

## Полезные команды

### Работа с PostgreSQL

```bash
# Запуск контейнера
docker-compose up -d

# Остановка контейнера
docker-compose down

# Просмотр логов
docker-compose logs -f postgres

# Подключение к psql
docker exec -it alcoholshop-postgres psql -U postgres -d alcoholshop

# Выполнить SQL запрос
docker exec -it alcoholshop-postgres psql -U postgres -d alcoholshop -c "SELECT * FROM alcohol_shops;"
```

### Maven команды

```bash
# Очистка и сборка
mvn clean install

# Быстрая сборка без тестов
mvn clean package -DskipTests

# Запуск Spring Boot
mvn spring-boot:run

# Просмотр дерева зависимостей
mvn dependency:tree

# Обновление зависимостей
mvn versions:display-dependency-updates
```

### Отладка

Для запуска в режиме отладки:

```bash
mvn spring-boot:run -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

Затем подключитесь из вашей IDE к `localhost:5005`

## Мониторинг

### Actuator Endpoints

Приложение предоставляет следующие monitoring endpoints:

- `GET /actuator/health` - Проверка здоровья приложения
- `GET /actuator/info` - Информация о приложении
- `GET /actuator/metrics` - Метрики приложения

Пример:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## Troubleshooting

### Проблема: Порт 5432 уже занят

**Решение:**
```bash
# Остановите conflicting процесс
sudo lsof -ti:5432 | xargs kill -9

# Или измените порт в docker-compose.yml
ports:
  - "5433:5432"
```

### Проблема: Ошибка подключения к БД

**Решение:**
```bash
# Проверьте, что контейнер запущен
docker-compose ps

# Проверьте логи
docker-compose logs postgres

# Перезапустите контейнер
docker-compose restart postgres
```

### Проблема: Flyway миграции не применяются

**Решение:**
```bash
# Очистите БД и примените миграции заново
docker exec -it alcoholshop-postgres psql -U postgres -d alcoholshop -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

### Проблема: Тесты падают с connection refused

**Решение:**
```bash
# Убедитесь, что Docker запущен
docker ps

# Проверьте, что Testcontainers может достучаться до Docker
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

## Дополнительные ресурсы

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.3.6/reference/html/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Hexagonal Architecture](https://herbertograca.com/2017/09/14/ports-adapters-architecture/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://docs.flywaydb.org/)

## Лицензия

Copyright © 2025 AlcoRadar. All rights reserved.

## Контакты

Для вопросов и предложений создайте issue в репозитории проекта.
