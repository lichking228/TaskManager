# TaskManager

Учебный REST-сервис для работы с задачами. Проект написан на Spring Boot, данные хранятся через Spring Data JPA.

## Стек

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Bean Validation
- H2
- PostgreSQL
- Gradle

## API

### Получить все задачи

```http
GET /tasks
```

### Получить задачу по id

```http
GET /tasks/{id}
```

Если задачи нет, вернется `404`.

### Создать задачу

```http
POST /tasks
Content-Type: application/json
```

```json
{
  "creatorId": 1,
  "assignedUserId": 2,
  "deadlineDate": "2026-08-01",
  "priority": "High"
}
```

`id`, `status` и `createDateTime` при создании передавать не нужно. `id` создает база, `status` ставится в `CREATED`, `createDateTime` ставится текущим временем, если поле не передано.

### Обновить задачу

```http
PUT /tasks/{id}
Content-Type: application/json
```

```json
{
  "creatorId": 1,
  "assignedUserId": 3,
  "status": "IN_PROGRESS",
  "deadlineDate": "2026-08-10",
  "priority": "Medium"
}
```

Если задача уже в статусе `DONE`, полное обновление запрещено и вернется `400`.

### Изменить статус

```http
PATCH /tasks/{id}/status
Content-Type: application/json
```

```json
"DONE"
```

Задачу в статусе `DONE` можно вернуть только в `IN_PROGRESS`.

### Удалить задачу

```http
DELETE /tasks/{id}
```

Если задачи нет, вернется `404`.

## Валидация

Для задачи действуют правила:

- `creatorId` обязателен и должен быть положительным числом;
- `assignedUserId` обязателен и должен быть положительным числом;
- `priority` обязателен;
- `deadlineDate`, если передан, не должен быть в прошлом;
- `createDateTime`, если передан, не должен быть в будущем;
- при создании нельзя передавать `id` и `status`;
- при обновлении `id` в теле запроса должен отсутствовать или совпадать с `id` в URL.

Пример ответа при ошибке валидации:

```json
{
  "message": "Validation failed",
  "errors": {
    "creatorId": "creatorId must be positive",
    "priority": "priority is required"
  }
}
```
