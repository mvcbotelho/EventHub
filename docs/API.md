# Referência da API — EventHub API

Documentação dos endpoints **já implementados** no código atual.

---

## Informações gerais

| Item | Valor |
|------|-------|
| Base URL (local) | `http://localhost:8080` |
| Formato | JSON |
| Autenticação | JWT Bearer (exceto register/login) |
| Documentação interativa | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

### Autenticação

Rotas públicas (sem token):

- `POST /auth/register`
- `POST /auth/login`

Todas as demais rotas exigem o header:

```http
Authorization: Bearer <token>
```

O token é obtido em `POST /auth/login`. Expiração padrão: **24 horas** (`JWT_EXPIRATION_MS=86400000`).

### Formato de erro padrão

Erros tratados pelo `GlobalExceptionHandler`:

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

| Status | Situação |
|--------|----------|
| `400` | Erro de validação ou regra de negócio |
| `401` | Não autenticado ou credenciais inválidas |
| `403` | Autenticado, mas sem permissão |
| `404` | Recurso não encontrado |
| `500` | Erro interno |

> **Observação:** respostas 401/403 geradas diretamente pelo Spring Security podem retornar corpo vazio em alguns casos. Melhoria planejada — ver [`NEXT_STEPS.md`](NEXT_STEPS.md).

---

## Auth

### POST /auth/register

Cadastra um novo usuário.

**Autenticação:** não requerida

**Body:**

```json
{
  "name": "Marcus",
  "email": "marcus@email.com",
  "password": "123456"
}
```

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `name` | string | Sim | Máx. 255 caracteres |
| `email` | string | Sim | Formato e-mail válido, único |
| `password` | string | Sim | Mín. 6 caracteres |

**Resposta `201 Created`:**

```json
{
  "id": "d7a7f01d-79a7-4fea-bf29-349c539e7ca1",
  "name": "Marcus",
  "email": "marcus@email.com",
  "createdAt": "2026-06-11T00:00:00Z",
  "updatedAt": "2026-06-11T00:00:00Z"
}
```

**Erros comuns:**

- `400` — `"Email already registered"`
- `400` — erros de validação nos campos

---

### POST /auth/login

Autentica o usuário e retorna JWT.

**Autenticação:** não requerida

**Body:**

```json
{
  "email": "marcus@email.com",
  "password": "123456"
}
```

**Resposta `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

**Erros comuns:**

- `401` — `"Invalid email or password"`

---

### GET /auth/me

Retorna o perfil do usuário autenticado.

**Autenticação:** requerida

**Resposta `200 OK`:**

```json
{
  "id": "d7a7f01d-79a7-4fea-bf29-349c539e7ca1",
  "name": "Marcus",
  "email": "marcus@email.com",
  "createdAt": "2026-06-11T00:00:00Z",
  "updatedAt": "2026-06-11T00:00:00Z"
}
```

---

## Events

Todos os endpoints abaixo **requerem autenticação**.

### POST /events

Cria um evento. O usuário autenticado torna-se o **dono** (`owner`).

**Body:**

```json
{
  "title": "Java Meetup",
  "description": "Spring Boot 3 workshop",
  "location": "São Paulo",
  "startDateTime": "2026-12-15T19:00:00Z",
  "endDateTime": "2026-12-15T21:00:00Z",
  "maxParticipants": 50
}
```

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `title` | string | Sim | Não vazio |
| `description` | string | Não | — |
| `location` | string | Sim | Não vazio |
| `startDateTime` | ISO-8601 (UTC) | Sim | Não pode estar no passado |
| `endDateTime` | ISO-8601 (UTC) | Sim | Deve ser ≥ `startDateTime` |
| `maxParticipants` | integer | Sim | Valor positivo |

**Resposta `201 Created`:**

```json
{
  "id": "11f661fe-eb5c-4d22-be5d-947550436949",
  "title": "Java Meetup",
  "description": "Spring Boot 3 workshop",
  "location": "São Paulo",
  "startDateTime": "2026-12-15T19:00:00Z",
  "endDateTime": "2026-12-15T21:00:00Z",
  "maxParticipants": 50,
  "ownerId": "d7a7f01d-79a7-4fea-bf29-349c539e7ca1",
  "ownerName": "Marcus",
  "createdAt": "2026-06-11T00:00:00Z",
  "updatedAt": "2026-06-11T00:00:00Z"
}
```

**Erros comuns:**

- `400` — `"End date and time cannot be before start date and time"`

---

### GET /events

Lista todos os eventos.

**Resposta `200 OK`:** array de `EventResponse`

---

### GET /events/mine

Lista eventos criados pelo usuário autenticado.

**Resposta `200 OK`:** array de `EventResponse`

---

### GET /events/this-week

Lista eventos cuja **data de início** cai na semana corrente (segunda 00:00 UTC até domingo 23:59 UTC).

**Resposta `200 OK`:** array de `EventResponse`

---

### GET /events/registered

Lista eventos em que o usuário autenticado possui inscrição com status `CONFIRMED`.

**Resposta `200 OK`:** array de `EventResponse`

---

### GET /events/{id}

Retorna detalhes de um evento.

**Path param:** `id` — UUID do evento

**Resposta `200 OK`:** objeto `EventResponse`

**Erros comuns:**

- `404` — `"Event not found"`

---

### PUT /events/{id}

Atualiza um evento. **Apenas o dono** pode executar.

**Path param:** `id` — UUID do evento

**Body:** mesmo formato de `POST /events`

**Resposta `200 OK`:** objeto `EventResponse` atualizado

**Erros comuns:**

- `403` — `"Only the event owner can perform this action"`
- `404` — `"Event not found"`

---

### DELETE /events/{id}

Exclui um evento. **Apenas o dono** pode executar. Inscrições associadas são removidas em cascata (FK `ON DELETE CASCADE`).

**Path param:** `id` — UUID do evento

**Resposta `204 No Content`**

**Erros comuns:**

- `403` — `"Only the event owner can perform this action"`
- `404` — `"Event not found"`

---

## Registrations

Todos os endpoints abaixo **requerem autenticação**.

Base path: `/events/{eventId}`

### POST /events/{eventId}/registrations

Inscreve o usuário autenticado no evento.

**Path param:** `eventId` — UUID do evento

**Body:** nenhum

**Resposta `201 Created`:**

```json
{
  "id": "6eeea845-1121-4784-a67d-c624ece4099f",
  "eventId": "11f661fe-eb5c-4d22-be5d-947550436949",
  "userId": "80b2c54e-966f-4581-a05f-18cece5af554",
  "status": "CONFIRMED",
  "registeredAt": "2026-06-11T00:00:00Z"
}
```

**Regras de negócio:**

- Dono do evento **não pode** se inscrever
- Evento encerrado (`endDateTime` no passado) **não aceita** inscrições
- Evento lotado (`CONFIRMED` ≥ `maxParticipants`) **bloqueia** novas inscrições
- Usuário já inscrito (`CONFIRMED`) recebe erro
- Se existir inscrição `CANCELED`, a inscrição é **reativada**

**Erros comuns:**

- `400` — `"Event owners do not need to register for their own events"`
- `400` — `"Cannot register for an event that has already ended"`
- `400` — `"User is already registered for this event"`
- `400` — `"Event is full"`

---

### DELETE /events/{eventId}/registrations/me

Cancela a inscrição do usuário autenticado (status → `CANCELED`).

**Path param:** `eventId` — UUID do evento

**Resposta `204 No Content`**

**Erros comuns:**

- `400` — `"Registration not found"`
- `400` — `"Registration is already canceled"`

---

### GET /events/{eventId}/participants

Lista participantes com inscrição `CONFIRMED`. **Apenas o dono do evento** pode acessar.

**Path param:** `eventId` — UUID do evento

**Resposta `200 OK`:**

```json
[
  {
    "userId": "80b2c54e-966f-4581-a05f-18cece5af554",
    "userName": "Ana",
    "userEmail": "ana@email.com",
    "registeredAt": "2026-06-11T00:00:00Z"
  }
]
```

**Erros comuns:**

- `403` — `"Only the event owner can view participants"`
- `404` — `"Event not found"`

---

## Endpoints planejados

Itens **não presentes** no código atual. Mantidos aqui apenas como referência de evolução:

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/refresh` | Refresh token — *planejado* |
| GET | `/events?query=&page=&size=` | Listagem paginada e filtrada — *planejado* |

### Infraestrutura

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/actuator/health` | Health check público — **implementado** |

Consulte [`NEXT_STEPS.md`](NEXT_STEPS.md) para prioridades.

---

## Entidades do banco (referência)

| Tabela | Migration |
|--------|-----------|
| `events` | V1 (+ `owner_id` em V3) |
| `users` | V2 |
| `event_registrations` | V4 |

Status de inscrição: `CONFIRMED`, `CANCELED`.

---

## Collection Bruno

Requests espelhando estes endpoints estão em `api-client/eventhub/`. Ver [`api-client/README.md`](../api-client/README.md).
