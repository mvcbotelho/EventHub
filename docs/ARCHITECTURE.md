# Arquitetura — EventHub API

Visão geral das decisões técnicas e da organização do projeto.

---

## Visão geral

O **EventHub API** é uma aplicação monolítica Spring Boot que expõe endpoints REST para gerenciamento de eventos e inscrições. A persistência usa PostgreSQL com schema versionado pelo Flyway. A autenticação é **stateless** via JWT.

```text
Cliente (Bruno / Swagger / curl)
        │
        ▼
┌───────────────────┐
│  Spring Security  │  ← JwtAuthenticationFilter
└─────────┬─────────┘
          ▼
┌───────────────────┐
│    Controllers    │  ← validação (@Valid), HTTP
└─────────┬─────────┘
          ▼
┌───────────────────┐
│     Services      │  ← regras de negócio, autorização
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

## Decisões técnicas atuais

| Decisão | Motivo |
|---------|--------|
| **Organização por domínio** | Cada feature (`auth`, `user`, `event`, `registration`) tem seu pacote. Facilita navegação e evolução sem over-engineering inicial. |
| **DTOs separados das entidades** | A API não expõe entidades JPA. Entrada e saída são records (`CreateEventRequest`, `EventResponse`, etc.). |
| **Regras no Service** | Validações de negócio e autorização (dono, participante) ficam na camada de serviço, não só no Security. |
| **JWT stateless** | Sem sessão no servidor; filtro valida token a cada request. Simples e adequado para API REST. |
| **BCrypt** | Senhas nunca armazenadas em texto plano. |
| **Flyway + `ddl-auto: validate`** | Schema versionado no Git; Hibernate apenas valida, sem alterar o banco automaticamente. |
| **Docker-first** | App conecta ao PostgreSQL pelo hostname `postgres` na rede Docker. Reduz dependências locais. |
| **Mensagens da API em inglês** | Padrão para portfólio internacional; documentação do projeto em português. |
| **Open-in-view desabilitado** | `spring.jpa.open-in-view: false` evita lazy loading fora de transação em controllers. |

---

## Abordagem Docker-first

### Componentes

| Serviço | Imagem / build | Porta | Função |
|---------|----------------|-------|--------|
| `postgres` | `postgres:16-alpine` | 5432 | Banco de dados |
| `app` | Build local (`Dockerfile`) | 8080 | API Spring Boot |

### Dockerfile

Multi-stage:

1. **Build** — Maven 3.9 + Temurin 21 compila o JAR
2. **Runtime** — JRE Alpine 21 executa o JAR com usuário não-root (`spring`)

### Fluxo de inicialização

1. PostgreSQL sobe e passa no healthcheck (`pg_isready`)
2. App conecta em `jdbc:postgresql://postgres:5432/eventhub`
3. Flyway aplica migrations pendentes (V1–V4)
4. Hibernate valida entidades contra o schema
5. Tomcat escuta na porta 8080

---

## Organização por domínio

```text
com.marcus.eventhub
├── auth/           Controllers, services e config de autenticação
│   ├── dto/
│   ├── AuthController, AuthService
│   ├── JwtService, JwtAuthenticationFilter
│   ├── SecurityConfig, CurrentUserService
│   └── CustomUserDetailsService
├── user/           Entidade User e UserRepository
├── event/          Domínio de eventos
│   ├── dto/
│   ├── Event, EventRepository
│   ├── EventService, EventController
├── registration/   Inscrições
│   ├── dto/
│   ├── EventRegistration, RegistrationStatus
│   ├── RegistrationService, RegistrationController
└── common/
    ├── config/     OpenApiConfig
    └── exception/  GlobalExceptionHandler, exceções customizadas
```

---

## Camadas do projeto

### Controller

- Recebe HTTP, delega ao service, retorna DTOs
- Usa `@Valid` para Bean Validation
- Anotações Swagger (`@Operation`, `@Tag`, `@SecurityRequirement`)
- **Não contém** regras de negócio

Exemplos: `AuthController`, `EventController`, `RegistrationController`

### Service

- Orquestra regras de negócio e transações (`@Transactional`)
- Valida ownership e permissões
- Lança exceções de domínio (`BusinessException`, `ForbiddenException`, `ResourceNotFoundException`)

Exemplos: `AuthService`, `EventService`, `RegistrationService`

### Repository

- Interface Spring Data JPA
- Queries customizadas com `@Query` e `@EntityGraph` quando necessário (ex.: carregar `owner` junto com `Event`)

Exemplos: `UserRepository`, `EventRepository`, `EventRegistrationRepository`

### Entity

- Mapeamento JPA ↔ tabelas PostgreSQL
- Relacionamentos: `Event.owner` → `User`, `EventRegistration` → `Event` + `User`
- Lifecycle callbacks (`@PrePersist`, `@PreUpdate`) para timestamps

Exemplos: `User`, `Event`, `EventRegistration`

### DTO

- Records imutáveis para request/response
- Validação de entrada via Jakarta Validation
- Factory methods estáticos (`EventResponse.from(event)`)

### Config

- `SecurityConfig` — filter chain, rotas públicas, BCrypt
- `OpenApiConfig` — metadados Swagger e scheme Bearer
- `application.yml` — datasource, Flyway, JWT, SpringDoc

---

## Fluxo básico de uma requisição

Exemplo: **criar evento autenticado**

```text
1. Cliente envia POST /events + Authorization: Bearer <jwt>

2. JwtAuthenticationFilter
   - Extrai token do header
   - Valida assinatura e expiração
   - Carrega UserDetails e popula SecurityContext

3. Spring Security
   - Verifica rota autenticada → permite

4. EventController.create()
   - Deserializa CreateEventRequest
   - Bean Validation (título, datas, etc.)

5. EventService.create()
   - Obtém usuário via CurrentUserService
   - Valida endDateTime >= startDateTime
   - Cria Event com owner = usuário atual
   - Persiste via EventRepository

6. EventResponse.from(event) → JSON 201 Created
```

Exemplo: **dono lista participantes**

```text
RegistrationController → RegistrationService.listParticipants()
  → verifica event.getOwner().id == currentUser.id
  → senão: ForbiddenException (403)
  → busca registrations CONFIRMED → ParticipantResponse
```

---

## Versionamento do banco (Flyway)

Migrations em `src/main/resources/db/migration/`:

| Versão | Arquivo | Descrição |
|--------|---------|-----------|
| V1 | `V1__create_events_table.sql` | Tabela `events` + constraints de data e capacidade |
| V2 | `V2__create_users_table.sql` | Tabela `users` + e-mail único |
| V3 | `V3__add_owner_to_events.sql` | FK `owner_id` → `users` |
| V4 | `V4__create_event_registrations_table.sql` | Tabela `event_registrations` + status |

Configuração:

```yaml
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true
```

O Hibernate **não altera** o schema em runtime — apenas valida que entidades correspondem às tabelas.

---

## Segurança

### Rotas públicas

- `POST /auth/register`
- `POST /auth/login`
- Swagger / OpenAPI (`/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**`)

### Rotas protegidas

Todas as demais exigem JWT válido.

### Autorização fine-grained

Implementada nos **services**, não via `@PreAuthorize`:

- Editar/excluir evento → dono
- Ver participantes → dono
- Inscrição → usuário autenticado (com regras de capacidade e status)

---

## Testes (estado atual)

| Tipo | Status |
|------|--------|
| `EventHubApplicationTests.contextLoads` | Existe (profile `test`, H2, Flyway off) |
| Testes unitários de services | **Não implementados** |
| Testes de integração (Testcontainers) | **Não implementados** |
| Testes de controller (MockMvc) | **Não implementados** |

---

## Possíveis evoluções arquiteturais

Evoluções **planejadas**, não presentes hoje:

| Evolução | Quando considerar |
|----------|-------------------|
| **Camada de mapper** (MapStruct) | Quando DTOs e entidades divergirem muito |
| **Problem Details (RFC 7807)** | Padronizar erros HTTP de forma interoperável |
| **CQRS / read models** | Se listagens ficarem complexas (filtros, agregações) |
| **Event-driven** (publicar `EventCreated`) | Integrações assíncronas (e-mail, notificações) |
| **Modular monolith** | Se o projeto crescer além de ~15–20 pacotes de domínio |
| **API Gateway** | Múltiplos serviços ou autenticação centralizada externa |
| **Cache (Redis)** | Listagens de alta leitura |
| **Multi-tenancy** | Organizações distintas gerenciando eventos isolados |

---

## Referências

- [`README.md`](../README.md)
- [`API.md`](API.md)
- [`NEXT_STEPS.md`](NEXT_STEPS.md)
