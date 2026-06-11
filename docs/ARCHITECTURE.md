# Architecture — EventHub API

Overview of technical decisions and project organization.

---

## Overview

**EventHub API** is a Spring Boot monolith exposing REST endpoints for event management and registrations. Persistence uses PostgreSQL with Flyway schema versioning. Authentication is **stateless** via JWT.

```text
Client (Bruno / Swagger / curl)
        │
        ▼
┌───────────────────┐
│  Spring Security  │  ← JwtAuthenticationFilter
└─────────┬─────────┘
          ▼
┌───────────────────┐
│    Controllers    │  ← validation (@Valid), HTTP
└─────────┬─────────┘
          ▼
┌───────────────────┐
│     Services      │  ← business rules, authorization
└─────────┬─────────┘
          ▼
┌───────────────────┐
│   Repositories    │  ← Spring Data JPA
└─────────┬─────────┘
          ▼
┌───────────────────┐
│    PostgreSQL     │  ← Flyway migrations
└───────────────────┘
```

---

## Current technical decisions

| Decision | Rationale |
|----------|-----------|
| **Domain organization** | Each feature (`auth`, `user`, `event`, `registration`) has its own package. Easy to navigate without over-engineering. |
| **DTOs separate from entities** | The API does not expose JPA entities. Input/output use records (`CreateEventRequest`, `EventResponse`, etc.). |
| **Rules in Service layer** | Business validation and authorization (owner, participant) live in services, not only in Security. |
| **Stateless JWT** | No server-side session; filter validates token on each request. Simple and suitable for REST. |
| **BCrypt** | Passwords are never stored in plain text. |
| **Flyway + `ddl-auto: validate`** | Schema versioned in Git; Hibernate only validates, never alters the database. |
| **Docker-first** | App connects to PostgreSQL via hostname `postgres` on the Docker network. |
| **English API messages** | All validation and business error messages are in English. |
| **Open-in-view disabled** | `spring.jpa.open-in-view: false` avoids lazy loading outside transactions in controllers. |

---

## Docker-first approach

### Components

| Service | Image / build | Port | Role |
|---------|---------------|------|------|
| `postgres` | `postgres:16-alpine` | 5432 | Database |
| `app` | Local build (`Dockerfile`) | 8080 | Spring Boot API |

### Dockerfile

Multi-stage:

1. **Build** — Maven 3.9 + Temurin 21 compiles the JAR
2. **Runtime** — JRE Alpine 21 runs the JAR as non-root user (`spring`)

### Startup flow

1. PostgreSQL starts and passes healthcheck (`pg_isready`)
2. App connects to `jdbc:postgresql://postgres:5432/eventhub`
3. Flyway applies pending migrations (V1–V4)
4. Hibernate validates entities against the schema
5. Tomcat listens on port 8080

---

## Domain organization

```text
com.marcus.eventhub
├── auth/           Authentication controllers, services, config
├── user/           User entity and UserRepository
├── event/          Events domain
├── registration/   Registrations
└── common/
    ├── config/     OpenApiConfig, CorsConfig
    └── exception/  GlobalExceptionHandler, custom exceptions
```

---

## Project layers

### Controller

- Handles HTTP, delegates to service, returns DTOs
- Uses `@Valid` for Bean Validation
- Swagger annotations (`@Operation`, `@Tag`, `@SecurityRequirement`)
- **No** business rules

### Service

- Orchestrates business rules and transactions (`@Transactional`)
- Validates ownership and permissions
- Throws domain exceptions (`BusinessException`, `ForbiddenException`, `ResourceNotFoundException`)

### Repository

- Spring Data JPA interfaces
- Custom `@Query` and `@EntityGraph` when needed (e.g. load `owner` with `Event`)

### Entity

- JPA mapping to PostgreSQL tables
- Relationships: `Event.owner` → `User`, `EventRegistration` → `Event` + `User`

### DTO

- Immutable records for request/response
- Jakarta Validation on input
- Static factory methods (`EventResponse.from(event)`)

### Config

- `SecurityConfig` — filter chain, public routes, BCrypt
- `OpenApiConfig` — Swagger metadata and Bearer scheme
- `application.yml` — datasource, Flyway, JWT, SpringDoc, Actuator

---

## Basic request flow

Example: **create authenticated event**

```text
1. Client sends POST /events + Authorization: Bearer <jwt>
2. JwtAuthenticationFilter validates token and sets SecurityContext
3. EventController.create() deserializes and validates CreateEventRequest
4. EventService.create() assigns current user as owner and persists
5. Returns EventResponse as JSON 201 Created
```

---

## Database versioning (Flyway)

Migrations in `src/main/resources/db/migration/`:

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__create_events_table.sql` | `events` table |
| V2 | `V2__create_users_table.sql` | `users` table |
| V3 | `V3__add_owner_to_events.sql` | `owner_id` FK |
| V4 | `V4__create_event_registrations_table.sql` | `event_registrations` table |

---

## Security

### Public routes

- `POST /auth/register`, `POST /auth/login`
- `GET /actuator/health`
- Swagger / OpenAPI paths

### Protected routes

All others require a valid JWT.

### Fine-grained authorization

Implemented in **services**, not via `@PreAuthorize`:

- Update/delete event → owner only
- View participants → owner only
- Registration → authenticated user (with capacity and status rules)

### Error responses

`SecurityProblemHandler` returns JSON `ApiErrorResponse` for 401 and 403.

---

## Tests (current state)

| Type | Class | Status |
|------|-------|--------|
| Spring context (H2) | `EventHubApplicationTests` | Active |
| Unit tests | `AuthServiceTest`, `EventServiceTest`, `RegistrationServiceTest` | Active |
| Integration (Testcontainers) | `EventHubFlowIT` | Active — requires Docker |
| CI | `.github/workflows/ci.yml` | `mvn verify` on push/PR |

---

## Possible architectural evolutions

| Evolution | When to consider |
|-----------|------------------|
| **Mapper layer** (MapStruct) | When DTOs and entities diverge significantly |
| **Problem Details (RFC 7807)** | Standardize HTTP errors interoperably |
| **CQRS / read models** | Complex listings with filters and aggregations |
| **Event-driven** | Async integrations (email, notifications) |
| **Pagination** | Large event listings |

---

## References

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`NEXT_STEPS.md`](NEXT_STEPS.md)
