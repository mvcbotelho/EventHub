# API Reference — EventHub API

Documentation for endpoints **currently implemented** in the codebase.

---

## General information

| Item | Value |
|------|-------|
| Base URL (local) | `http://localhost:8080` |
| Format | JSON |
| Authentication | JWT Bearer (except register/login) |
| Interactive docs | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Health check | http://localhost:8080/actuator/health |

### Authentication

Public routes (no token):

- `POST /auth/register`
- `POST /auth/login`
- `GET /actuator/health`

All other routes require:

```http
Authorization: Bearer <token>
```

The token is obtained from `POST /auth/login`. Default expiration: **24 hours** (`JWT_EXPIRATION_MS=86400000`).

### Standard error format

Errors handled by `GlobalExceptionHandler` and `SecurityProblemHandler`:

```json
{
  "timestamp": "2026-06-11T00:00:00Z",
  "status": 400,
  "message": "Validation error",
  "errors": {
    "email": "Invalid email"
  }
}
```

| Status | Meaning |
|--------|---------|
| `400` | Validation or business rule error |
| `401` | Not authenticated or invalid credentials |
| `403` | Authenticated but not authorized |
| `404` | Resource not found |
| `500` | Internal server error |

---

## Auth

### POST /auth/register

Registers a new user.

**Authentication:** not required

**Body:**

```json
{
  "name": "Marcus",
  "email": "marcus@email.com",
  "password": "123456"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `name` | string | Yes | Max 255 characters |
| `email` | string | Yes | Valid email format, unique |
| `password` | string | Yes | Min 6 characters |

**Response `201 Created`:** `UserResponse` (id, name, email, createdAt, updatedAt)

**Common errors:**

- `400` — `"Email already registered"`
- `400` — field validation errors

---

### POST /auth/login

Authenticates the user and returns a JWT.

**Authentication:** not required

**Body:**

```json
{
  "email": "marcus@email.com",
  "password": "123456"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

**Common errors:**

- `401` — `"Invalid email or password"`

---

### GET /auth/me

Returns the authenticated user's profile.

**Authentication:** required

**Response `200 OK`:** `UserResponse`

---

## Events

All endpoints below **require authentication**.

### POST /events

Creates an event. The authenticated user becomes the **owner**.

**Body:**

```json
{
  "title": "Java Meetup",
  "description": "Spring Boot 3 workshop",
  "location": "New York",
  "startDateTime": "2026-12-15T19:00:00Z",
  "endDateTime": "2026-12-15T21:00:00Z",
  "maxParticipants": 50
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `title` | string | Yes | Not blank |
| `description` | string | No | — |
| `location` | string | Yes | Not blank |
| `startDateTime` | ISO-8601 (UTC) | Yes | Cannot be in the past |
| `endDateTime` | ISO-8601 (UTC) | Yes | Must be ≥ `startDateTime` |
| `maxParticipants` | integer | Yes | Positive value |

**Response `201 Created`:** `EventResponse` (includes `ownerId`, `ownerName`)

**Common errors:**

- `400` — `"End date and time cannot be before start date and time"`

---

### GET /events

Lists all events.

**Response `200 OK`:** array of `EventResponse`

---

### GET /events/mine

Lists events created by the authenticated user.

---

### GET /events/this-week

Lists events whose **start date** falls in the current week (Monday 00:00 UTC through Sunday).

---

### GET /events/registered

Lists events where the authenticated user has a `CONFIRMED` registration.

---

### GET /events/{id}

Returns event details.

**Path param:** `id` — event UUID

**Common errors:**

- `404` — `"Event not found"`

---

### PUT /events/{id}

Updates an event. **Owner only.**

**Body:** same format as `POST /events`

**Common errors:**

- `403` — `"Only the event owner can perform this action"`
- `404` — `"Event not found"`

---

### DELETE /events/{id}

Deletes an event. **Owner only.** Registrations are removed via `ON DELETE CASCADE`.

**Response `204 No Content`**

---

## Registrations

All endpoints below **require authentication**.

Base path: `/events/{eventId}`

### POST /events/{eventId}/registrations

Registers the authenticated user for an event.

**Body:** none

**Response `201 Created`:** `RegistrationResponse`

**Business rules:**

- Event owner **cannot** register
- Ended events **reject** new registrations
- Full events **block** new registrations
- Duplicate `CONFIRMED` registration returns error
- Existing `CANCELED` registration is **reactivated**

**Common errors:**

- `400` — `"Event owners do not need to register for their own events"`
- `400` — `"Cannot register for an event that has already ended"`
- `400` — `"User is already registered for this event"`
- `400` — `"Event is full"`

---

### DELETE /events/{eventId}/registrations/me

Cancels the authenticated user's registration (status → `CANCELED`).

**Response `204 No Content`**

---

### GET /events/{eventId}/participants

Lists `CONFIRMED` participants. **Event owner only.**

**Response `200 OK`:** array of `ParticipantResponse`

**Common errors:**

- `403` — `"Only the event owner can view participants"`

---

## Planned endpoints

Not present in the current codebase:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/refresh` | Refresh token — *planned* |
| GET | `/events?query=&page=&size=` | Paginated filtered listing — *planned* |

See [`NEXT_STEPS.md`](NEXT_STEPS.md) for priorities.

---

## Database tables (reference)

| Table | Migration |
|-------|-----------|
| `events` | V1 (+ `owner_id` in V3) |
| `users` | V2 |
| `event_registrations` | V4 |

Registration status values: `CONFIRMED`, `CANCELED`.

---

## Bruno collection

Requests mirroring these endpoints live in `api-client/eventhub/`. See [`api-client/README.md`](../api-client/README.md).
