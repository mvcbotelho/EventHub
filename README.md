# EventHub API

REST API for creating, publishing, and joining events, built with **Java 21**, **Spring Boot 3**, and **PostgreSQL**, using a **Docker-first** approach.

## Stack

- Java 21
- Spring Boot 3
- Spring Web, Data JPA, Validation, Security
- JWT (jjwt)
- PostgreSQL
- Flyway
- Swagger/OpenAPI
- Docker & Docker Compose

## Project structure

```text
eventhub-api/
├── src/main/java/com/marcus/eventhub/
│   ├── auth/            # JWT, login, registration
│   ├── user/            # User entity
│   ├── event/           # Events
│   ├── registration/    # Registrations and participants
│   ├── common/          # Shared config and global errors
│   └── EventHubApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Getting started

```bash
cp .env.example .env
docker compose up --build
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Stop: `docker compose down`

## API client (Bruno)

A ready-to-use Bruno collection lives in [`api-client/eventhub`](api-client/eventhub). See [`api-client/README.md`](api-client/README.md) for setup and usage.

## Authentication

Public routes: `POST /auth/register`, `POST /auth/login`.

All other routes require:

```http
Authorization: Bearer <token>
```

In Swagger, click **Authorize** and paste **only the token** (without the `Bearer` prefix).

## Endpoints

### Auth

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register a user |
| POST | `/auth/login` | No | Login (returns JWT) |
| GET | `/auth/me` | Yes | Authenticated user profile |

### Events

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/events` | Create an event |
| GET | `/events` | List all events |
| GET | `/events/mine` | List events created by the authenticated user |
| GET | `/events/this-week` | List events this week |
| GET | `/events/registered` | List events the user is registered for |
| GET | `/events/{id}` | Get event details |
| PUT | `/events/{id}` | Update event (owner only) |
| DELETE | `/events/{id}` | Delete event (owner only) |

### Registrations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/events/{eventId}/registrations` | Register for an event |
| DELETE | `/events/{eventId}/registrations/me` | Cancel own registration |
| GET | `/events/{eventId}/participants` | List participants (owner only) |

## Example flow

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Marcus","email":"marcus@email.com","password":"123456"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"marcus@email.com","password":"123456"}' | jq -r .token)

# 3. Create an event
curl -X POST http://localhost:8080/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Meetup",
    "description": "Spring Boot 3",
    "location": "São Paulo",
    "startDateTime": "2026-06-15T19:00:00Z",
    "endDateTime": "2026-06-15T21:00:00Z",
    "maxParticipants": 50
  }'
```

## Next steps

1. Unit and integration tests (JUnit + Testcontainers)
2. Observability and CI/CD improvements

## Technical decisions

- **Stateless JWT:** no server-side sessions; a filter validates the token on each request.
- **BCrypt:** passwords are never stored in plain text.
- **Owner FK:** migration V3 links events to the creating user; stage 1 test data is removed before the schema change.
- **Single registration per user/event:** unique constraint on `(event_id, user_id)` with `CONFIRMED` or `CANCELED` status.
- **Authorization in services:** owner and participant rules live in the business layer, not only in Spring Security.
