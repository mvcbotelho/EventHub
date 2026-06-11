# EventHub API Client (Bruno)

Bruno collection for testing the EventHub API locally.

## Prerequisites

- [Bruno](https://www.usebruno.com/) installed
- API running at `http://localhost:8080` (`docker compose up --build`)

## Open the collection

1. Open Bruno
2. **Open Collection** → select the folder `api-client/eventhub`
3. **Select environment `local`** in the top-right dropdown (next to the collection name)

> **Critical:** the top-right dropdown must show **local**, not **No Environment**.
> Editing variables under the Environments tab does not activate them — you must pick **local** in that dropdown.
>
> After updating `bruno.json`, close and reopen the collection so **local** is selected automatically.

## Environment

Select the **local** environment (top right). Default variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `token` | *(empty)* | JWT — set automatically after Login |
| `eventId` | *(empty)* | Last created event ID — set after Create Event |
| `name` | `Marcus` | User name for Register |
| `email` | `marcus@email.com` | User email for Register / Login |
| `password` | `123456` | User password for Register / Login |

The same defaults are defined in `collection.bru`, so requests work even before selecting an environment.

## Suggested flow

1. **Auth → Register** (first time only)
2. **Auth → Login** — saves `token`
3. **Auth → Me** — verify authentication
4. **Events → Create Event** — saves `eventId`
5. **Events → List Events** / **List My Events**
6. **Registrations → Register for Event** (use a second user account to test as participant)
7. **Registrations → List Participants** (as event owner)

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `Invalid URL` / `{{baseUrl}}` in red | Select environment **local** (top right) |
| `401 Unauthorized` | Run **Auth → Login** first |
| Connection refused | Start the API: `docker compose up --build` |
| `Email already registered` | Change `email` in the environment or use Login |

## Folder structure

```text
eventhub/
├── bruno.json
├── collection.bru          # Default variables (baseUrl, etc.)
├── environments/
│   └── local.bru
├── auth/
├── events/
└── registrations/
```
