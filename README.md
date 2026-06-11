# EventHub API

API REST para criação, divulgação e participação em eventos, construída com **Java 21**, **Spring Boot 3** e **PostgreSQL**, com abordagem **Docker-first**.

## Stack

- Java 21
- Spring Boot 3
- Spring Web, Data JPA, Validation
- PostgreSQL
- Flyway
- Swagger/OpenAPI
- Docker & Docker Compose

## Estrutura do projeto

```text
eventhub-api/
├── src/main/java/com/marcus/eventhub/
│   ├── common/          # Configurações e tratamento global de erros
│   ├── event/           # Domínio de eventos (MVP - Etapa 1)
│   └── EventHubApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/    # Migrations Flyway
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

## Como executar

1. Copie as variáveis de ambiente de exemplo:

```bash
cp .env.example .env
```

2. Suba a aplicação e o banco:

```bash
docker compose up --build
```

3. Acesse a documentação interativa:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

4. Para parar os containers:

```bash
docker compose down
```

## Endpoints disponíveis (Etapa 1)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/events` | Criar evento |
| GET | `/events` | Listar todos os eventos |
| GET | `/events/this-week` | Listar eventos da semana |
| GET | `/events/{id}` | Detalhes de um evento |
| PUT | `/events/{id}` | Atualizar evento |
| DELETE | `/events/{id}` | Excluir evento |

> **Nota:** Nesta etapa inicial, os endpoints estão **sem autenticação**. Autenticação JWT, usuários e inscrições serão adicionados nas próximas etapas.

## Exemplo com cURL

Criar um evento:

```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Meetup Java",
    "description": "Encontro sobre Spring Boot 3",
    "location": "São Paulo - SP",
    "startDateTime": "2026-06-15T19:00:00Z",
    "endDateTime": "2026-06-15T21:00:00Z",
    "maxParticipants": 50
  }'
```

Listar eventos:

```bash
curl http://localhost:8080/events
```

## Próximas etapas

1. Cadastro e login de usuários com JWT
2. Associação de eventos ao dono (owner)
3. Regras de autorização (editar/excluir apenas o dono)
4. Inscrições em eventos
5. Testes com JUnit e Testcontainers

## Decisões técnicas (Etapa 1)

- **Organização por domínio:** cada feature (`event`, futuramente `auth`, `user`, `registration`) fica em seu próprio pacote.
- **DTOs separados:** entrada (`CreateEventRequest`, `UpdateEventRequest`) e saída (`EventResponse`) desacoplam a API da entidade JPA.
- **Flyway + `ddl-auto: validate`:** o schema é versionado via migrations; o Hibernate apenas valida, sem alterar o banco automaticamente.
- **Docker-first:** a aplicação se conecta ao PostgreSQL pelo nome do serviço `postgres` na rede Docker.
- **Validação em camadas:** Bean Validation nos DTOs + regras de negócio no `EventService` (ex.: data fim >= data início).
