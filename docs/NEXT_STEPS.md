# Next steps — EventHub API

Planning document for evolving the project after the functional MVP.

---

## Current state

**EventHub API** is at `0.0.1-SNAPSHOT` with the main flow implemented:

- users register and authenticate via JWT with refresh tokens;
- authenticated users create and manage events (soft delete);
- paginated and filterable event listings;
- other users register for events;
- owners view participants;
- environment runs entirely via Docker Compose with app healthcheck;
- interactive Swagger documentation;
- Bruno collection for manual testing;
- unit and integration tests with CI;
- structured JSON logging in non-test profiles.

**Not yet implemented:**

- rate limiting;
- full observability stack (metrics, tracing);
- email notifications on registration.

---

## Completed phases

### Phase 1 — Foundation and events

- [x] Spring Boot 3 + Java 21 + Maven
- [x] Docker-first setup, Flyway, event CRUD

### Phase 2 — Authentication and users

- [x] User entity, JWT, register/login/me

### Phase 3 — Event ownership

- [x] Owner FK, mine endpoint, owner-only update/delete

### Phase 4 — Registrations

- [x] EventRegistration, all registration rules

### Phase 5 — Developer experience

- [x] Bruno collection, initial documentation

### Phase 6 — Quality, tests, and reliability

- [x] Unit tests: `AuthServiceTest`, `EventServiceTest`, `RegistrationServiceTest`
- [x] Integration tests with Testcontainers (`EventHubFlowIT`)
- [x] MockMvc: full flow, 401/403 JSON, public health
- [x] JSON error responses for 401/403 (`SecurityProblemHandler`)
- [x] Spring Boot Actuator (`GET /actuator/health`)
- [x] GitHub Actions CI (`.github/workflows/ci.yml`)
- [x] English API messages and documentation

### Phase 7 — Product evolution and observability

- [x] Pagination on event listings (`PageResponse`, default size 20)
- [x] Filters by title, location, and start date range
- [x] Refresh token (`POST /auth/refresh`, `POST /auth/logout`)
- [x] Docker Compose healthcheck for the `app` service (Actuator)
- [x] Structured JSON logging (`logstash-logback-encoder`)
- [x] Soft delete for events (`deleted_at` column)

---

## Recommended next phase

### Phase 8 — Scale and notifications

1. **Rate limiting** on auth and registration endpoints
2. **Metrics** via Actuator/Prometheus
3. **Email notifications** on registration
4. **User schedule conflict** validation
5. **Block edit** when event has registrants

---

## Pending business rules

| Rule | Status |
|------|--------|
| Unique email on registration | Implemented |
| BCrypt password hashing | Implemented |
| End date ≥ start date | Implemented |
| Start date not in the past | Implemented |
| Positive max participants | Implemented |
| Owner-only update/delete | Implemented |
| Single registration per user/event | Implemented |
| Full event blocks registration | Implemented |
| Owner cannot register for own event | Implemented |
| Re-register after cancel | Implemented |
| Pagination | Implemented |
| Soft delete | Implemented |
| User schedule conflict validation | **Planned / TBD** |
| Block edit when event has registrants | **Planned / TBD** |

---

## Checklist for the next dev session

- [ ] Read [`ARCHITECTURE.md`](ARCHITECTURE.md) and [`API.md`](API.md)
- [ ] Start environment: `docker compose up --build`
- [ ] Validate flow in Bruno or Swagger
- [ ] Pick first Phase 8 task
- [ ] Update this document when items are done

---

## Internal references

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`api-client/README.md`](../api-client/README.md)
