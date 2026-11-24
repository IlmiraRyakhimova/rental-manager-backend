# controller-logging-spring-boot-starter

Spring Boot стартер, который логирует запросы и ответы всех `@RestController` через AOP
и маскирует чувствительные данные (пароли, токены) в логах.

## Подключение

```xml
<dependency>
    <groupId>com.rental</groupId>
    <artifactId>controller-logging-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

Больше ничего не нужно — стартер включается автоматически через автоконфигурацию.

## Пример логов

```
→ AuthController.signIn(..) args=[{"email":"test@example.com","password":"****"}]
← BookingController.create(..) 37 ms result={"code":"ABC123","checkIn":"2026-12-01"}
✗ AuthController.signIn(..) 96 ms exception=BadCredentialsException: Invalid email or password
```

- `→` вход в метод контроллера с аргументами
- `←` успешный ответ и время выполнения
- `✗` исключение и время выполнения (исключение пробрасывается дальше без изменений)

## Настройки

```yaml
app:
  logging:
    enabled: true                           # выключить: false (или APP_LOGGING_ENABLED=false)
    masked-fields: password, token, secret  # значения по умолчанию
```

Маскирование:
- поле маскируется, если его имя **содержит** одно из ключевых слов, без учёта регистра
  (`password` скроет `password`, `oldPassword`, `newPassword`; `token` скроет `accessToken`, `refreshToken`);
- работает на любой глубине — во вложенных объектах и массивах;
- свой список **заменяет** список по умолчанию, а не дополняет его;
- маскируются только логи — ответ клиенту не меняется.

Если аргумент нельзя сериализовать в JSON (например, `HttpServletRequest`), в лог пишется имя класса: `<HttpServletRequest>`.

## Как это работает

| Класс | Назначение |
|---|---|
| `ControllerLoggingAutoConfiguration` | регистрирует бины; подключается через `META-INF/spring/...AutoConfiguration.imports` |
| `ControllerLoggingAspect` | `@Around`-аспект на все `@RestController` |
| `SensitiveDataMasker` | превращает объект в JSON-дерево (Jackson) и рекурсивно маскирует поля |
| `ControllerLoggingProperties` | настройки с префиксом `app.logging` |

Автоконфигурация срабатывает, только если:
- приложение веб (`@ConditionalOnWebApplication`);
- `app.logging.enabled` не равно `false` (`@ConditionalOnProperty`).

Бины `ControllerLoggingAspect` и `SensitiveDataMasker` можно заменить своими —
стартер не создаст собственные, если такие бины уже есть (`@ConditionalOnMissingBean`).

## Требования

Java 17+, Spring Boot 3.x, веб-приложение (Spring MVC).

## Тесты

```bash
./mvnw -pl controller-logging-spring-boot-starter test
```
