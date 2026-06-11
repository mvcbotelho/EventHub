# Next steps — EventHub API

Planning document for evolving the project after the functional MVP.

---

## Current state

**EventHub API** is at `0.0.1-SNAPSHOT` with production-hardening features implemented:

- JWT authentication with refresh tokens;
- paginated and filterable event listings with soft delete;
- event registrations with schedule conflict validation;
- owner-only updates blocked when registrants exist;
- distributed rate limiting (Redis in Docker, in-memory locally);
- OpenTelemetry tracing to Jaeger (Docker profile);
- Prometheus metrics + Grafana dashboards;
- role-based access (USER / ADMIN);
- event categories with slug filter;
- waitlist with automatic promotion on cancellation;
- email notifications on registration (MailHog in Docker Compose);
- structured JSON logging, CI, and Testcontainers integration tests.

**Not yet implemented:**

- advanced notification channels (SMS, push);
- multi-region deployment;
- API versioning.

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

### Phase 9 — Production hardening

- [x] Distributed rate limiting with Redis (Docker profile)
- [x] OpenTelemetry tracing + Grafana dashboards (Jaeger + Prometheus)
- [x] Role-based access (admin vs user)
- [x] Event categories/tags
- [x] Waitlist when events are full

---

## Recommended next phase

### Phase 10 — Platform expansion

1. **WebSocket or SSE** for real-time waitlist and registration updates
2. **Full-text search** (PostgreSQL `tsvector` or Elasticsearch)
3. **Event images** (S3-compatible object storage)
4. **OAuth2 social login** (Google, GitHub)
5. **Kubernetes manifests** and Helm chart

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
| Waitlist when full + auto-promotion | Implemented |
| Admin role for platform management | Implemented |
| Event categories | Implemented |

---

## Checklist for the next dev session

- [ ] Read [`ARCHITECTURE.md`](ARCHITECTURE.md) and [`API.md`](API.md)
- [ ] Start environment: `docker compose up --build`
- [ ] Open Grafana at http://localhost:3000 (admin/admin)
- [ ] Open Jaeger at http://localhost:16686
- [ ] Login as bootstrap admin: `admin@eventhub.local` / `admin123456`
- [ ] Pick first Phase 10 task
- [ ] Update this document when items are done

---

## Internal references

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`api-client/README.md`](../api-client/README.md)
