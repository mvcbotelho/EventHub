# EventHub API

API REST para criação, divulgação e participação em eventos. Projeto de aprendizado e portfólio construído com **Java 21**, **Spring Boot 3** e **PostgreSQL**, seguindo uma abordagem **Docker-first**.

## Objetivo do projeto

Oferecer uma API moderna e bem estruturada para que usuários possam:

- criar conta e autenticar-se;
- publicar e gerenciar eventos;
- descobrir eventos disponíveis;
- inscrever-se e cancelar inscrições;
- permitir que donos de eventos vejam quem participará.

O foco é aprendizado prático de backend, boas práticas de arquitetura e um código evolutivo, pronto para crescer.

## Stack utilizada

| Tecnologia | Uso |
|------------|-----|
| Java 21 | Linguagem |
| Spring Boot 3.4.5 | Framework |
| Maven | Build e dependências |
| Spring Web | API REST |
| Spring Data JPA | Persistência |
| Spring Security + JWT (jjwt) | Autenticação e autorização |
| Bean Validation | Validação de entrada |
| PostgreSQL 16 | Banco de dados |
| Flyway | Versionamento de schema |
| SpringDoc OpenAPI | Documentação interativa (Swagger) |
| Docker + Docker Compose | Ambiente de execução |
| JUnit + H2 (test) | Teste básico de contexto |
| Bruno | Collection HTTP para testes manuais |

## Funcionalidades já implementadas

### Autenticação
- Cadastro de usuário (`POST /auth/register`)
- Login com JWT (`POST /auth/login`)
- Perfil do usuário autenticado (`GET /auth/me`)

### Eventos
- CRUD completo de eventos
- Listagem de eventos da semana (`GET /events/this-week`)
- Listagem dos eventos do usuário (`GET /events/mine`)
- Listagem de eventos inscritos (`GET /events/registered`)
- Edição e exclusão restritas ao dono do evento

### Inscrições
- Inscrição em evento
- Cancelamento da própria inscrição
- Listagem de participantes (apenas dono do evento)

### Infraestrutura e qualidade
- Docker-first (`Dockerfile` + `docker-compose.yml`)
- Migrations Flyway (V1–V4)
- Tratamento global de erros (`@RestControllerAdvice`)
- DTOs, services e repositories por domínio
- Swagger/OpenAPI
- Collection Bruno em `api-client/eventhub`

## Funcionalidades planejadas

Consulte o roadmap detalhado em [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md). Resumo:

- Testes unitários e de integração (Testcontainers)
- Pipeline de CI/CD
- Melhorias de observabilidade (Actuator, logs estruturados)
- Paginação e filtros nas listagens
- Refresh token e políticas de segurança avançadas

## Arquitetura e organização das pastas

```text
EventHub/
├── api-client/                 # Collection Bruno
│   └── eventhub/
├── docs/                       # Documentação do projeto
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── NEXT_STEPS.md
├── src/
│   ├── main/java/com/marcus/eventhub/
│   │   ├── auth/               # JWT, login, registro, security
│   │   ├── user/               # Entidade e repository de User
│   │   ├── event/              # Domínio de eventos
│   │   ├── registration/       # Inscrições e participantes
│   │   ├── common/             # Config, exceções globais
│   │   └── EventHubApplication.java
│   ├── main/resources/
│   │   ├── application.yml
│   │   └── db/migration/       # Flyway V1–V4
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

Organização **por domínio/feature** (`auth`, `user`, `event`, `registration`), com camadas Controller → Service → Repository dentro de cada pacote.

Documentação complementar:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — visão arquitetural
- [`docs/API.md`](docs/API.md) — referência de endpoints

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

Opcional para testes manuais:

- [Bruno](https://www.usebruno.com/)
- `curl` e `jq`

## Como rodar o projeto com Docker

1. Copie as variáveis de ambiente:

```bash
cp .env.example .env
```

2. Suba a aplicação e o banco:

```bash
docker compose up --build
```

3. Aguarde os containers `eventhub-postgres` (healthy) e `eventhub-api` (porta 8080).

## Como parar os containers

```bash
docker compose down
```

Para remover também o volume do PostgreSQL (apaga os dados):

```bash
docker compose down -v
```

## Como acessar a API

| Recurso | URL |
|---------|-----|
| Base URL local | http://localhost:8080 |
| Health check | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

### Autenticação

Rotas públicas:

- `POST /auth/register`
- `POST /auth/login`

Demais rotas exigem:

```http
Authorization: Bearer <token>
```

No Swagger, clique em **Authorize** e cole **somente o token** (sem o prefixo `Bearer`).

## Como testar com Bruno

1. Instale o [Bruno](https://www.usebruno.com/).
2. Abra a collection: `api-client/eventhub`
3. Selecione o environment **local**
4. Execute **Auth → Register** (primeira vez) e **Auth → Login** (salva `token` automaticamente)
5. Use os requests das pastas **Events** e **Registrations**

Detalhes em [`api-client/README.md`](api-client/README.md).

## Variáveis de ambiente

Definidas em `.env.example` e usadas pelo `docker-compose.yml`:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `POSTGRES_DB` | `eventhub` | Nome do banco |
| `POSTGRES_USER` | `eventhub` | Usuário do PostgreSQL |
| `POSTGRES_PASSWORD` | `eventhub` | Senha do PostgreSQL |
| `POSTGRES_PORT` | `5432` | Porta exposta do PostgreSQL |
| `APP_PORT` | `8080` | Porta exposta da API |
| `JWT_SECRET` | *(dev default)* | Chave HMAC para assinar JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Expiração do token (24h) |

> **Atenção:** altere `JWT_SECRET` em ambientes reais. O valor padrão serve apenas para desenvolvimento local.

## Comandos úteis

```bash
# Subir em background
docker compose up --build -d

# Testes unitários (rápidos, sem Docker extra)
docker run --rm -v "$PWD":/app -w /app maven:3.9.9-eclipse-temurin-21 mvn test

# Suite completa incluindo integração (requer Docker para Testcontainers)
docker run --rm -v "$PWD":/app -w /app -v /var/run/docker.sock:/var/run/docker.sock \
  maven:3.9.9-eclipse-temurin-21 mvn verify

# Health check
curl http://localhost:8080/actuator/health

# Ver logs da API
docker logs -f eventhub-api

# Ver logs do PostgreSQL
docker logs -f eventhub-postgres

# Rebuild forçado
docker compose up --build --force-recreate
```

## Exemplo rápido com curl

```bash
# Cadastro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Marcus","email":"marcus@email.com","password":"123456"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"marcus@email.com","password":"123456"}' | jq -r .token)

# Perfil
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"

# Criar evento
curl -X POST http://localhost:8080/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Meetup",
    "description": "Spring Boot 3",
    "location": "São Paulo",
    "startDateTime": "2026-12-15T19:00:00Z",
    "endDateTime": "2026-12-15T21:00:00Z",
    "maxParticipants": 50
  }'
```

## Status atual do projeto

| Área | Status |
|------|--------|
| Setup Docker + Flyway | Concluído |
| CRUD de eventos | Concluído |
| Autenticação JWT | Concluído |
| Ownership de eventos | Concluído |
| Inscrições | Concluído |
| Swagger/OpenAPI | Concluído |
| Collection Bruno | Concluído |
| Testes automatizados | Unitários + integração (Testcontainers) |
| CI/CD | GitHub Actions (`mvn verify`) |
| Actuator | `GET /actuator/health` |
| Testcontainers | Planejado |

**Versão:** `0.0.1-SNAPSHOT` — MVP funcional, pronto para evolução em qualidade e observabilidade.

> **Nota:** as mensagens de erro da API estão em **inglês**. A documentação do projeto está em **português**.

## Roadmap resumido

| Fase | Tema | Status |
|------|------|--------|
| 1 | Setup, Docker, Flyway, CRUD de eventos | Concluída |
| 2 | Autenticação JWT e usuários | Concluída |
| 3 | Ownership de eventos | Concluída |
| 4 | Inscrições em eventos | Concluída |
| 5 | Collection Bruno | Concluída |
| 6 | Testes, CI/CD e observabilidade | **Concluída** |
| 7 | Paginação, filtros e evolução de produto | **Próxima** |

Detalhes em [`docs/NEXT_STEPS.md`](docs/NEXT_STEPS.md).

## Licença

A confirmar.
