# Ticket Classification Service(Fariz)

A Spring Boot (Kotlin) service that ingests support tickets, stores them in PostgreSQL, and classifies them in the
background with an LLM (category, priority, one-sentence summary).

## Run it

Prerequisites: JDK 17 and Docker.

```bash
docker run -d --name tickets-db -p 5432:5432 -e POSTGRES_DB=tickets -e POSTGRES_PASSWORD=******* postgres:16
./gradlew bootRun
```

The app connects to `jdbc:postgresql://localhost:5432/tickets` (`postgres` / `postgres`). `schema.sql` runs on every
startup and is safe to re-run (`CREATE TABLE IF NOT EXISTS`). The API listens on `http://localhost:8080`.

## How it works

1. `POST /api/tickets` saves the ticket as `PENDING`. No classification happens in the request.
2. `TicketClassificationWorker` runs every 10 minutes and loads every `PENDING` ticket.
3. For each ticket, `TicketClassificationService`:
   - asks `InjectionDetector` whether the text looks like a prompt-injection attempt. If it does, the ticket is
     marked `FAILED` and is **not** sent to the model;
   - calls `MlClient` (the fake LLM), which returns **raw text**;
   - passes that text to `ClassificationValidator`, which either returns a `ValidatedClassification` or throws;
   - saves the classification and sets the status to `CLASSIFIED`.
4. If the model call or validation fails, `attempt_count` goes up and the ticket stays `PENDING` for the next run.
   After 3 failed attempts it becomes `FAILED`.


## Project structure

```
src/main/kotlin/com/loura/tickets
├── Fariz.kt                      # Application entry point
├── controller/               # REST endpoints
├── dto/                      # Request/response models, raw and validated model output
├── enum/                     # TicketStatus, TicketCategory, TicketPriority
├── model/                    # JPA entity (Ticket)
├── repository/               # Spring Data repository
├── service/                  # Business logic, fake LLM, validator, injection detector
└── worker/                   # Scheduled classification job
src/main/resources
├── application.yml               # Datasource and SQL init config
└── schema.sql                    # Database schema
```

## API

Base path: `/api/tickets`

| Method | Path                | Result                                                                 |
|--------|---------------------|------------------------------------------------------------------------|
| POST   | `/api/tickets`      | `200` with the ticket (`PENDING`), `409` if the id exists, `400` if invalid |
| GET    | `/api/tickets/{id}` | `200` with the ticket, `404` if unknown                                |
| GET    | `/api/tickets`      | Paged list, filters `category` and `priority` are optional             |

```http
POST /api/tickets
Content-Type: application/json

{ "externalId": "TCK-1001", "subject": "Cannot log in", "body": "I get an error when I try to log in." }
```

```http
GET /api/tickets?category=TECHNICAL&priority=HIGH&page=0&size=10
```

Response:

```json
{ "externalId": "TCK-1001", "subject": "Cannot log in", "status": "CLASSIFIED",
  "category": "TECHNICAL", "priority": "HIGH", "summary": "Customer reports: Cannot log in" }
```

| Field    | Values                                     |
|----------|--------------------------------------------|
| status   | `PENDING`, `CLASSIFIED`, `FAILED`          |
| category | `BILLING`, `TECHNICAL`, `ACCOUNT`, `OTHER` |
| priority | `LOW`, `MEDIUM`, `HIGH`                    |

## Decisions

- **PostgreSQL.** Tickets survive restarts, and the UNIQUE `external_id` makes duplicates impossible.
- **Duplicate id returns `409`.** A duplicate never creates a row, so it is never classified twice.
- **Async via a scheduled job.** The `tickets` table is the queue (no broker).
- **One ticket at a time.** Simple, and keeps load on the model predictable, at the cost of throughput.
- **Restart-safe.** No in-memory state; an interrupted ticket stays `PENDING` and is retried.
- **Retries.** Model errors and invalid answers both increase `attempt_count`; after 3 the ticket is `FAILED`.
- **Strict validation.** Only allowed category/priority values and a 1–300 character summary are accepted. Invalid
  output is never stored or replaced with a default.
- **Prompt injection.** The validator limits the damage, `InjectionDetector` marks known attack phrases `FAILED`
  without calling the model, and the model has no tools or secrets.

## Suggestions for improvement
- If multiple application instances are running, use a Redis lock to ensure that the same ticket is not processed by more than one instance.
-  Add a failure-reason flag to identify why classification failed and make decisions based on the failure reason. For example, if the failure is due to Prompt Injection, we can introduce a scheduled job to block any email containing Prompt Injection content.
- Collect emails that the system fails to classify, manually classify them, and use them as training data to improve the system's accuracy over time.

## Development

| Task                    | Command                  |
|-------------------------|--------------------------|
| Format the code         | `./gradlew ktlintFormat` |
| Check formatting (lint) | `./gradlew ktlintCheck`  |
| Build the jar           | `./gradlew build`        |
