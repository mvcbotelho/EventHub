# Next steps — EventHub API

Planning document for evolving the project after the functional MVP.

---

## Current state

**EventHub API** is at `0.0.1-SNAPSHOT` with the main flow implemented:

- users register and authenticate via JWT;
- authenticated users create and manage events;
- other users register for events;
- owners view participants;
- environment runs entirely via Docker Compose;
- interactive Swagger documentation;
- Bruno collection for manual testing;
- unit and integration tests with CI.

**Not yet implemented:**

- pagination on listings;
- refresh token;
- rate limiting;
- structured logging / full observability stack;
- Docker healthcheck using Actuator on the app container.

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

---

## Recommended next phase

### Phase 7 — Product evolution and observability

Medium/low priority after the quality foundation is in place.

---

## Prioritized tasks (Phase 7)

1. **Pagination** on listings (`GET /events`, `/events/mine`, etc.)
2. **Filters** by location, date, or title
3. **Refresh token** and session revocation
4. **Docker Compose healthcheck** for the `app` service using Actuator
5. **Structured JSON logging**
6. **Soft delete** for events
7. **Email notifications** on registration — *planned, out of current scope*

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
| Pagination | **Planned** |
| User schedule conflict validation | **Planned / TBD** |
| Block edit when event has registrants | **Planned / TBD** |

---

## Suggested commit messages (Phase 7)

```bash
git commit -m "feat: add pagination to event listing endpoints"
git commit -m "feat: add refresh token support"
git commit -m "chore: add docker healthcheck for app service"
```

---

## Checklist for the next dev session

- [ ] Read [`ARCHITECTURE.md`](ARCHITECTURE.md) and [`API.md`](API.md)
- [ ] Start environment: `docker compose up --build`
- [ ] Validate flow in Bruno or Swagger
- [ ] Pick first Phase 7 task (pagination recommended)
- [ ] Update this document when items are done

---

## Internal references

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`api-client/README.md`](../api-client/README.md)
