# EventHub API

REST API for creating, publishing, and joining events. Learning and portfolio project built with **Java 21**, **Spring Boot 3**, and **PostgreSQL**, using a **Docker-first** approach.

## Project goal

Provide a modern, well-structured API where users can:

- register and authenticate;
- publish and manage events;
- discover available events (paginated, with filters);
- register for and cancel event registrations;
- allow event owners to view participants.

The focus is hands-on backend learning, solid architecture practices, and evolvable code ready to grow.

## Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Language |
| Spring Boot 3.4.5 | Framework |
| Maven | Build and dependencies |
| Spring Web | REST API |
| Spring Data JPA | Persistence |
| Spring Security + JWT (jjwt) | Authentication and authorization |
| Bean Validation | Input validation |
| PostgreSQL 16 | Database |
| Flyway | Schema versioning |
| SpringDoc OpenAPI | Interactive docs (Swagger) |
| Spring Boot Actuator | Health checks |
| Logstash Logback Encoder | Structured JSON logging |
| Docker + Docker Compose | Runtime environment |
| JUnit + Testcontainers | Automated tests |
| Bruno | HTTP collection for manual testing |

## Implemented features

### Authentication
- User registration (`POST /auth/register`)
- JWT login with refresh token (`POST /auth/login`)
- Token refresh (`POST /auth/refresh`)
- Session logout / refresh token revocation (`POST /auth/logout`)
- Authenticated user profile (`GET /auth/me`)

### Events
- Full event CRUD (delete is soft delete)
- Paginated listings with optional filters (`title`, `location`, `startFrom`, `startTo`)
- Events this week (`GET /events/this-week`)
- User's events (`GET /events/mine`)
- Registered events (`GET /events/registered`)
- Update and soft-delete restricted to event owner

### Registrations
- Event registration
- Cancel own registration
- List participants (event owner only)

### Infrastructure and quality
- Docker-first (`Dockerfile` + `docker-compose.yml`) with healthchecks on `postgres` and `app`
- Flyway migrations (V1–V6)
- Global error handling (`@RestControllerAdvice`)
- JSON error responses for 401/403 (`SecurityProblemHandler`)
- DTOs, services, and repositories by domain
- Swagger/OpenAPI
- Bruno collection in `api-client/eventhub`
- Unit and integration tests (Testcontainers)
- GitHub Actions CI
- Actuator health endpoint (`GET /actuator/health`)
- Structured JSON logging (plain text in `test` profile)

## Planned features

See the detailed roadmap in [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md). Summary:

- Rate limiting
- Metrics and tracing (Prometheus, etc.)
- Email notifications on registration
- User schedule conflict validation
- Block event edits when registrants exist

## Project structure

```text
EventHub/
├── api-client/                 # Bruno collection
│   └── eventhub/
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── NEXT_STEPS.md
├── src/
│   ├── main/java/com/marcus/eventhub/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── event/
│   │   ├── registration/
│   │   ├── common/
│   │   └── EventHubApplication.java
│   ├── main/resources/
│   │   ├── application.yml
│   │   ├── logback-spring.xml
│   │   └── db/migration/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

Organized **by domain/feature** (`auth`, `user`, `event`, `registration`) with Controller → Service → Repository layers.

Additional documentation:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — architecture overview
- [`docs/API.md`](docs/API.md) — endpoint reference

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

Optional for manual testing:

- [Bruno](https://www.usebruno.com/)
- `curl` and `jq`

## Run with Docker

```bash
cp .env.example .env
docker compose up --build
```

Wait for `eventhub-postgres` and `eventhub-api` to become healthy (port 8080).

## Stop containers

```bash
docker compose down
```

To remove the PostgreSQL volume (deletes data):

```bash
docker compose down -v
```

## Access the API

| Resource | URL |
|----------|-----|
| Base URL (local) | http://localhost:8080 |
| Health check | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

### Authentication

Public routes:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /actuator/health`

All other routes require:

```http
Authorization: Bearer <token>
```

In Swagger, click **Authorize** and paste **only the token** (without the `Bearer` prefix).

Login returns an access token (default 24h) and a refresh token (default 7 days). Use `POST /auth/refresh` to obtain a new pair without re-entering credentials.

## Test with Bruno

1. Install [Bruno](https://www.usebruno.com/)
2. Open collection: `api-client/eventhub`
3. Select the **local** environment (top-right dropdown)
4. Run **Auth → Register** (first time) and **Auth → Login** (saves `token` and `refreshToken`)
5. Use requests under **Events** and **Registrations**

See [`api-client/README.md`](api-client/README.md).

## Environment variables

Defined in `.env.example` and used by `docker-compose.yml`:

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `eventhub` | Database name |
| `POSTGRES_USER` | `eventhub` | PostgreSQL user |
| `POSTGRES_PASSWORD` | `eventhub` | PostgreSQL password |
| `POSTGRES_PORT` | `5432` | Exposed PostgreSQL port |
| `APP_PORT` | `8080` | Exposed API port |
| `JWT_SECRET` | *(dev default)* | HMAC key for JWT signing |
| `JWT_EXPIRATION_MS` | `86400000` | Access token expiration (24h) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token expiration (7d) |

> **Warning:** change `JWT_SECRET` in real environments. The default is for local development only.

## Useful commands

```bash
# Run in background
docker compose up --build -d

# Unit tests (fast, no extra Docker)
docker run --rm -v "$PWD":/app -w /app maven:3.9.9-eclipse-temurin-21 mvn test

# Full suite including integration (requires Docker for Testcontainers)
docker run --rm -v "$PWD":/app -w /app -v /var/run/docker.sock:/var/run/docker.sock \
  maven:3.9.9-eclipse-temurin-21 mvn verify

# Health check
curl http://localhost:8080/actuator/health

# API logs (JSON in non-test profiles)
docker logs -f eventhub-api
```

## Quick curl example

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Marcus","email":"marcus@email.com","password":"123456"}'

# Login (returns token + refreshToken)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"marcus@email.com","password":"123456"}' | jq -r .token)

# Profile
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"

# List events (paginated)
curl "http://localhost:8080/events?page=0&size=20&title=Meetup" \
  -H "Authorization: Bearer $TOKEN"

# Create event
curl -X POST http://localhost:8080/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Meetup",
    "description": "Spring Boot 3",
    "location": "New York",
    "startDateTime": "2026-12-15T19:00:00Z",
    "endDateTime": "2026-12-15T21:00:00Z",
    "maxParticipants": 50
  }'
```

Event listings return a `PageResponse` object (`content`, `page`, `size`, `totalElements`, …). See [`docs/API.md`](docs/API.md) for full details.

## Project status

| Area | Status |
|------|--------|
| Docker + Flyway setup | Done |
| Event CRUD + soft delete | Done |
| JWT authentication + refresh token | Done |
| Event ownership | Done |
| Registrations | Done |
| Pagination and filters | Done |
| Swagger/OpenAPI | Done |
| Bruno collection | Done |
| Automated tests | Unit + integration (Testcontainers) |
| CI/CD | GitHub Actions (`mvn verify`) |
| Actuator + app healthcheck | Done |
| Structured JSON logging | Done |

**Version:** `0.0.1-SNAPSHOT` — functional MVP with product evolution features (Phase 7).

## Roadmap summary

| Phase | Theme | Status |
|-------|-------|--------|
| 1 | Setup, Docker, Flyway, event CRUD | Done |
| 2 | JWT auth and users | Done |
| 3 | Event ownership | Done |
| 4 | Event registrations | Done |
| 5 | Bruno collection | Done |
| 6 | Tests, CI/CD, observability | Done |
| 7 | Pagination, filters, refresh token, soft delete | Done |
| 8 | Rate limiting, metrics, notifications | **Next** |

Details in [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md).

## License

MIT
