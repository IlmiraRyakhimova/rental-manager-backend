# Rental Manager — Backend

Backend сервиса для автоматизации управления арендой апартаментов.
В процессе миграции с монолита на микросервисы с Apache Kafka.

## Стек
Java 17, Spring Boot 3, PostgreSQL, Redis, Docker

## Локальный запуск
1. `cp .env.example .env` и заполнить значения
2. `docker compose up -d` — поднять Postgres и Redis
3. `cd main-service && ./mvnw spring-boot:run`