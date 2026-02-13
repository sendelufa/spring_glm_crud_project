# AlcoholShop Service

REST API для управления алкомаркетами с использованием Domain-Driven Design и Clean Architecture.

## Архитектура

Проект построен по принципам Hexagonal Architecture (Ports & Adapters):

```
com.alcoradar.alcoholshop/
├── domain/          - Доменный слой (сущности, value objects, domain services)
├── application/     - Слой приложения (use cases, application services)
├── infrastructure/  - Инфраструктура (репозитории, внешние системы)
└── interfaces/      - Интерфейсы (REST controllers, DTOs)
```

## Технологический стек

- **Java 21** - последняя LTS версия
- **Spring Boot 3.3.6** - фреймворк
- **Spring Data JPA** - работа с БД
- **PostgreSQL** - СУБД
- **Lombok** - сокращение кода
- **MapStruct 1.6.3** - маппинг сущностей
- **Maven** - сборка проекта
- **JUnit 5 + AssertJ** - тестирование

## Структура слоев

### Domain Layer (доменный слой)
- Сущности (Entities)
- Value Objects
- Domain Services
- Repository Interfaces (порты)
- Domain Events

### Application Layer (слой приложения)
- Use Cases / Application Services
- Application Services
- Input/Output DTOs
- Интерфейсы репозиториев

### Infrastructure Layer (инфраструктура)
- JPA Repository реализации
- Мапперы (MapStruct)
- Конфигурация БД
- Внешние сервисы

### Interface Layer (интерфейсы)
- REST Controllers
- Request/Response DTOs
- Обработка ошибок
- Валидация

## Разработка

### Сборка проекта

```bash
mvn clean compile
```

### Запуск тестов

```bash
mvn clean test
```

### Сборка JAR

```bash
mvn clean package
```

## Конфигурация

Файл конфигурации: `src/main/resources/application.yml`

## Версионирование

Формат коммитов: `feat(DD-MM-YY-XX): описание`

Пример: `feat(25-02-13-01): создать spring-boot проект с базовыми зависимостями`

## Лицензия

Copyright (c) 2025 AlcoRadar
