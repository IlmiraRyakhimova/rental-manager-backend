# Rental Manager — Backend

Backend сервиса для автоматизации управления арендой апартаментов.
В процессе миграции с монолита на микросервисы с Apache Kafka.

## Стек
Java 17, Spring Boot 3, PostgreSQL, Kafka, Docker

## Структура проекта
Многомодульный Maven-проект:

| Модуль | Описание |
|---|---|
| `main-service` | основное приложение (монолит, постепенно разбивается на микросервисы) |
| [`controller-logging-spring-boot-starter`](controller-logging-spring-boot-starter/README.md) | стартер: логирование запросов/ответов контроллеров через AOP с маскированием паролей и токенов |

## Локальный запуск
1. `cp .env.example .env` и заполнить значения
2. `docker compose up -d` — поднять Postgres (порт 5434) и Kafka
3. Собрать и запустить из корня проекта:
   ```bash
   ./mvnw install -pl main-service -am -DskipTests
   set -a; source .env; set +a
   ./mvnw -pl main-service spring-boot:run
   ```
