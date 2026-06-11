# Próximos passos — EventHub API

Documento de planejamento para evolução do projeto após a conclusão do MVP funcional.

---

## Estado atual do projeto

O **EventHub API** está em `0.0.1-SNAPSHOT` com o fluxo principal implementado:

- usuários se cadastram e autenticam via JWT;
- usuários autenticados criam e gerenciam eventos;
- outros usuários se inscrevem em eventos;
- donos visualizam participantes;
- ambiente sobe inteiramente via Docker Compose;
- documentação interativa via Swagger;
- collection Bruno para testes manuais.

O que **ainda não existe** no código:

- testes de integração com PostgreSQL real (Testcontainers);
- testes unitários de services e regras de negócio;
- pipeline de CI/CD;
- Spring Boot Actuator / health checks expostos;
- paginação nas listagens;
- refresh token;
- rate limiting;
- internacionalização configurável das mensagens da API.

---

## O que já foi concluído

### Fase 1 — Fundação e eventos

- [x] Projeto Spring Boot 3 + Java 21 + Maven
- [x] `Dockerfile` multi-stage
- [x] `docker-compose.yml` (app + postgres)
- [x] `application.yml` configurado
- [x] Flyway habilitado
- [x] Migration `V1__create_events_table.sql`
- [x] Entidade `Event` e CRUD inicial
- [x] DTOs, Service, Repository, Controller
- [x] Tratamento global de erros
- [x] Bean Validation
- [x] Swagger/OpenAPI

### Fase 2 — Autenticação e usuários

- [x] Entidade `User` e migration `V2__create_users_table.sql`
- [x] Cadastro (`POST /auth/register`)
- [x] Login (`POST /auth/login`)
- [x] Spring Security configurado
- [x] JWT com jjwt (`JwtService`, `JwtAuthenticationFilter`)
- [x] Endpoint `GET /auth/me`
- [x] Senhas com BCrypt (`PasswordEncoder`)

### Fase 3 — Ownership dos eventos

- [x] Migration `V3__add_owner_to_events.sql`
- [x] Eventos associados ao usuário criador (`owner_id`)
- [x] Edição apenas pelo dono
- [x] Exclusão apenas pelo dono
- [x] Endpoint `GET /events/mine`

### Fase 4 — Inscrições em eventos

- [x] Entidade `EventRegistration` e migration `V4__create_event_registrations_table.sql`
- [x] Status `CONFIRMED` / `CANCELED`
- [x] Inscrição (`POST /events/{eventId}/registrations`)
- [x] Impedir inscrição duplicada (constraint + regra de negócio)
- [x] Impedir inscrição em evento lotado
- [x] Impedir inscrição em evento encerrado
- [x] Impedir dono de se inscrever no próprio evento
- [x] Cancelamento (`DELETE /events/{eventId}/registrations/me`)
- [x] Listagem de participantes pelo dono (`GET /events/{eventId}/participants`)
- [x] Endpoint `GET /events/registered`
- [x] Endpoint `GET /events/this-week`

### Fase 5 — Developer Experience

- [x] Collection Bruno completa (`api-client/eventhub`)
- [x] Environment `local` com variáveis e scripts de token
- [x] Documentação inicial (`README.md`, `docs/`)

---

### Fase 6 — Qualidade, testes e confiabilidade

- [x] Testes unitários: `AuthServiceTest`, `EventServiceTest`, `RegistrationServiceTest`
- [x] Testes de integração com Testcontainers (`EventHubFlowIT`)
- [x] MockMvc: fluxo completo, 401 JSON, 403 ownership, health público
- [x] Respostas JSON padronizadas em 401/403 (`SecurityProblemHandler`)
- [x] Spring Boot Actuator (`GET /actuator/health`)
- [x] Pipeline CI (`.github/workflows/ci.yml`)

---

## Próxima fase recomendada

### Fase 7 — Evolução de produto e observabilidade

Prioridade média/baixa após a base de qualidade estar pronta.

---

## Lista priorizada de tarefas

### Concluído na Fase 6

1. ~~Testes unitários de services~~
2. ~~Testes de integração com Testcontainers~~
3. ~~Testes MockMvc (401, 403, fluxo feliz)~~
4. ~~Erros JSON no Spring Security~~
5. ~~Actuator health~~
6. ~~GitHub Actions CI~~

### Alta prioridade (Fase 7)
8. **Paginação** nas listagens (`GET /events`, `/events/mine`, etc.)
9. **Refresh token** e revogação de sessão
10. **Soft delete** de eventos
11. **Notificações** (e-mail ao inscrever-se) — *planejado, fora do escopo atual*

---

## Sugestão de ordem de implementação

```text
1. Testes unitários dos services (rápido, alto valor)
2. Configurar Testcontainers + 2–3 testes de integração críticos
3. Testes de controller/security (401, 403, fluxos felizes)
4. CI com GitHub Actions
5. Actuator + healthcheck no docker-compose
6. Melhorias de erro JSON no Security
7. Paginação e filtros (quando necessário)
```

---

## Regras de negócio pendentes

Regras **já implementadas** estão documentadas em [`API.md`](API.md). Pendências ou melhorias:

| Regra | Status |
|-------|--------|
| E-mail único no cadastro | Implementada |
| Senha com hash BCrypt | Implementada |
| Data fim ≥ data início | Implementada (service + CHECK no banco) |
| Data início não no passado | Implementada (Bean Validation `@FutureOrPresent`) |
| Máximo de participantes positivo | Implementada |
| Apenas dono edita/exclui evento | Implementada |
| Inscrição única por usuário/evento | Implementada |
| Evento lotado bloqueia inscrição | Implementada |
| Dono não se inscreve no próprio evento | Implementada |
| Re-inscrição após cancelamento | Implementada (reativa status `CANCELED`) |
| Paginação de listagens | **Planejada** |
| Validar conflito de horário do usuário | **Planejada / a confirmar** |
| Impedir edição de evento com inscritos | **Planejada / a confirmar** |

---

## Melhorias técnicas futuras

- **MapStruct** ou mappers dedicados (se DTOs crescerem)
- **Problem Details** (RFC 7807) no lugar de `ApiErrorResponse` customizado
- **OpenAPI** gerado como artefato no CI
- **Profiles** Spring (`dev`, `test`, `prod`) mais explícitos
- **Secrets** via Docker secrets ou variáveis injetadas em produção
- **Logs estruturados** (JSON) para observabilidade
- **Migration de dados** com Flyway para ambientes existentes
- **README em inglês** (versão internacional do portfólio) — *opcional*

---

## Possíveis commits sugeridos

Organize o trabalho da Fase 6 em commits pequenos:

```bash
# Testes unitários
git commit -m "test: add unit tests for AuthService and EventService"

# Testcontainers
git commit -m "test: add integration tests with Testcontainers and PostgreSQL"

# Controller/security
git commit -m "test: add MockMvc tests for auth and protected routes"

# CI
git commit -m "ci: add GitHub Actions workflow for build and tests"

# Actuator
git commit -m "feat: add Spring Boot Actuator health endpoint"

# Security errors
git commit -m "fix: return JSON error body for 401 and 403 responses"

# Paginação (futuro)
git commit -m "feat: add pagination to event listing endpoints"
```

---

## Checklist para a próxima sessão de desenvolvimento

Use esta lista ao retomar o projeto:

- [ ] Ler [`ARCHITECTURE.md`](ARCHITECTURE.md) e [`API.md`](API.md) para contexto
- [ ] Subir o ambiente: `docker compose up --build`
- [ ] Validar fluxo manual no Bruno ou Swagger
- [ ] Adicionar dependência Testcontainers no `pom.xml`
- [ ] Escrever primeiro teste unitário (`EventService.validateDateRange`)
- [ ] Escrever primeiro teste de integração (register + login)
- [ ] Configurar GitHub Actions (build + test)
- [ ] Atualizar este documento marcando itens concluídos
- [ ] Atualizar status no `README.md`

---

## Referências internas

- [`README.md`](../README.md) — visão geral e como rodar
- [`API.md`](API.md) — endpoints e exemplos
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — decisões técnicas
- [`api-client/README.md`](../api-client/README.md) — collection Bruno
