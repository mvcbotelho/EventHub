# Next steps — EventHub API

Planning document for evolving the project after the functional MVP.

---

## Current state

**EventHub API** is at `0.0.1-SNAPSHOT` with the main flow and scale features implemented:

- JWT authentication with refresh tokens;
- paginated and filterable event listings with soft delete;
- event registrations with schedule conflict validation;
- owner-only updates blocked when registrants exist;
- rate limiting on auth and registration endpoints;
- Prometheus metrics via Actuator;
- email notifications on registration (MailHog in Docker Compose);
- structured JSON logging, CI, and Testcontainers integration tests.

**Not yet implemented:**

- distributed rate limiting (Redis);
- full observability stack (tracing, dashboards);
- advanced notification channels (SMS, push).

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

- [x] Unit and integration tests, CI, Actuator health, JSON error responses

### Phase 7 — Product evolution and observability

- [x] Pagination, filters, refresh token, soft delete, Docker healthcheck, JSON logging

### Phase 8 — Scale and notifications

- [x] Rate limiting (Bucket4j) on auth and registration endpoints
- [x] Prometheus metrics (`GET /actuator/prometheus`, custom registration counter)
- [x] Email notifications on registration (SMTP via MailHog in Docker)
- [x] User schedule conflict validation
- [x] Block event edit when confirmed registrants exist

---

## Recommended next phase

### Phase 9 — Production hardening

1. **Distributed rate limiting** with Redis
2. **OpenTelemetry tracing** and Grafana dashboards
3. **Role-based access** (admin vs user)
4. **Event categories/tags**
5. **Waitlist** when events are full

---

## Business rules

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
| Pagination and soft delete | Implemented |
| Schedule conflict validation | Implemented |
| Block edit with confirmed registrants | Implemented |
| Rate limiting on sensitive endpoints | Implemented |
| Registration email notification | Implemented (when mail enabled) |

---

## Checklist for the next dev session

- [ ] Read [`ARCHITECTURE.md`](ARCHITECTURE.md) and [`API.md`](API.md)
- [ ] Start environment: `docker compose up --build`
- [ ] Open MailHog UI at http://localhost:8025 after registering for an event
- [ ] Pick first Phase 9 task
- [ ] Update this document when items are done

---

## Internal references

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`api-client/README.md`](../api-client/README.md)
