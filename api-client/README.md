# EventHub API Client (Bruno)

Bruno collection for testing the EventHub API locally.

## Prerequisites

- [Bruno](https://www.usebruno.com/) installed
- API running at `http://localhost:8080` (`docker compose up --build`)

## Open the collection

1. Open Bruno
2. **Open Collection** → select `api-client/eventhub`

## Environment

Select the **local** environment (top right). Default variables:

| Variable | Description |
|----------|-------------|
| `baseUrl` | API base URL (`http://localhost:8080`) |
| `token` | JWT (set automatically after Login) |
| `eventId` | Last created event ID (set after Create Event) |
| `name` | User name for Register |
| `email` | User email for Register / Login |
| `password` | User password for Register / Login |

## Suggested flow

1. **Auth → Register** (first time only)
2. **Auth → Login** — saves `token`
3. **Auth → Me** — verify authentication
4. **Events → Create Event** — saves `eventId`
5. **Events → List Events** / **List My Events**
6. **Registrations → Register for Event** (use a second user account to test as participant)
7. **Registrations → List Participants** (as event owner)

## Folder structure

```text
eventhub/
├── bruno.json
├── collection.bru
├── environments/
│   └── local.bru
├── auth/
├── events/
└── registrations/
```
