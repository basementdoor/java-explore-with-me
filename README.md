# Explore With Me

**Explore With Me** — это микросервисное приложение на **Spring Boot 3**, предназначенное для управления событиями, пользователями, комментариями и категориями. Проект состоит из нескольких сервисов: `main-service` и `stats-service`.

---

## Структура проекта

Проект имеет модульную структуру:

- `main-service` — основной сервис с REST API для работы с событиями, пользователями, комментариями и категориями.
- `stats-service` — сервис статистики (включает `stats-server`, `stats-client`, `stats-dto`).
- Общий `pom.xml` управляет зависимостями и сборкой всех модулей.

---

## Технологии

- **Java 21**
- **Spring Boot 3.3**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Maven**
- **JUnit 5 / MockMvc** (тестирование)
- **Checkstyle, SpotBugs, Jacoco** (анализ кода и покрытие)

---

## REST API

### Категории

- `GET /categories` — получить все категории (пагинация)
- `GET /categories/{catId}` — получить категорию по ID
- `POST /admin/categories` — создать категорию
- `PATCH /admin/categories/{catId}` — обновить категорию
- `DELETE /admin/categories/{catId}` — удалить категорию

### События

- `GET /events` — публичные события с фильтрацией
- `GET /events/{id}` — информация о событии
- `GET /users/{userId}/events` — события пользователя
- `POST /users/{userId}/events` — создать событие
- `PATCH /users/{userId}/events/{eventId}` — обновить событие
- `GET /admin/events` — получить полную информацию для админов
- `PATCH /admin/events/{eventId}` — обновить событие админом

### Запросы на участие

- `GET /users/{userId}/requests` — получить запросы пользователя
- `POST /users/{userId}/requests?eventId=` — создать запрос
- `PATCH /users/{userId}/requests/{requestId}/cancel` — отменить запрос
- `PATCH /users/{userId}/events/{eventId}/requests` — обновить статус запросов на участие (админ)

### Пользователи

- `GET /admin/users` — получить пользователей (фильтр по ID)
- `POST /admin/users` — создать пользователя
- `DELETE /admin/users/{userId}` — удалить пользователя

### Комментарии

- `GET /events/{eventId}/comments` — комментарии к событию
- `POST /users/{userId}/comments?eventId=` — создать комментарий
- `PATCH /users/{userId}/comments/{commentId}` — обновить комментарий
- `DELETE /users/{userId}/comments/{commentId}` — удалить комментарий
- `GET /admin/comments` — получить комментарии для админа
- `PATCH /admin/comments/{commentId}` — обновить статус комментария

### Подборки (Compilations)

- `GET /compilations` — получить подборки (публичные)
- `GET /compilations/{compId}` — получить подборку по ID
- `POST /admin/compilations` — создать подборку
- `PATCH /admin/compilations/{compId}` — обновить подборку
- `DELETE /admin/compilations/{compId}` — удалить подборку

---

## Схема базы данных

Основные таблицы:

- `users` — пользователи
- `categories` — категории событий
- `events` — события
- `requests` — запросы на участие
- `comments` — комментарии к событиям
- `compilations` — подборки событий
- `compilations_events` — связь подборок и событий

---

## Конфигурация

### application.yaml (main-service)

```yaml
app:
  name: "main-service"
server:
  port: 8080
spring:
  datasource:
    driver-class-name: "org.postgresql.Driver"
    url: "jdbc:postgresql://localhost:5432/ewm"
    username: "ewm"
    password: "1234"
  jpa:
    ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: "org.hibernate.dialect.PostgreSQLDialect"
        format_sql: true
```

## Docker Compose

Для запуска проекта с PostgreSQL:

```
docker-compose up --build
```


### Сервисы:

- ewm-service — основной сервис, порт 8080

- ewm-db — PostgreSQL база для main-service, порт 5432

- stats-server — сервис статистики, порт 9090

- stats-db — PostgreSQL база для stats-server, порт 9091
