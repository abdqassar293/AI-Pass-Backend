# AI-Pass — Mini AI Orchestration Backend

A Spring Boot backend that demonstrates a simplified AI orchestration platform: users register, create AI tasks, run agents, and retrieve execution history. Built as a 24-hour technical evaluation.

## Live Demo

- **Live API:** `https://ai-pass-backend-7zli.onrender.com/`
- **Swagger UI:** `https://ai-pass-backend-7zli.onrender.com/swagger-ui.html`
- **Health:** `https://ai-pass-backend-7zli.onrender.com/health`
- **GitHub:** `https://github.com/abdqassar293/AI-Pass-Backend`

## Tech Stack

- **Java 21 / Spring Boot 4.1.0
- **Spring Security 6** with OAuth2 Resource Server (JWT via Nimbus JOSE)
- **Spring Data JPA + Hibernate**
- **PostgreSQL 16**
- **springdoc-openapi** (Swagger UI)
- **Lombok**
- **Docker / Docker Compose** for local Postgres

## Architecture

Clean layered architecture:

```
┌─────────────────────────────────────────┐
│  Controllers (REST endpoints)           │  AuthController, TaskController,
│                                         │  AgentController, HealthController
├─────────────────────────────────────────┤
│  Services (business logic)              │  TaskService, TokenService,
│                                         │  CustomUserDetailsService
├─────────────────────────────────────────┤
│  Engine / Agent layer (abstraction)     │  ExecutionEngine + AiTaskHandler[],
│                                         │  AgentRegistry + Agent[]
├─────────────────────────────────────────┤
│  Repositories (Spring Data JPA)         │  UserRepository, TaskRepository,
│                                         │  ExecutionRepository
├─────────────────────────────────────────┤
│  Entities                               │  User, Task, Execution
├─────────────────────────────────────────┤
│  PostgreSQL                             │
└─────────────────────────────────────────┘
```

DTOs are used at controller boundaries to decouple the API contract from JPA entities.

### Key design choices

- **Spring Security OAuth2 Resource Server** instead of a custom JWT filter. Tokens are issued by `TokenService` (using `JwtEncoder`) and validated automatically by Spring's built-in resource server filter chain — no `OncePerRequestFilter` boilerplate.
- **Handler pattern for task execution** (`AiTaskHandler` interface). Each task type is its own `@Component` (e.g. `DocumentSummaryHandler`, `InvoiceReviewHandler`). The `ExecutionEngine` dispatches by `TaskType` via Spring's auto-injected `List<AiTaskHandler>`. Adding a new task type = new `@Component`. No changes elsewhere.
- **Agent registry pattern** (`AgentRegistry` + `Agent` interface). Same idea as the engine but for callable, stateless agents. All `Agent` beans are auto-discovered at startup and indexed by name.
- **User-scoped data access.** Tasks are always loaded via `findByIdAndUser(...)` so a user can never read another user's data, even by guessing IDs.

## Database Schema

### `users`
| Column     | Type         | Notes                         |
|------------|--------------|-------------------------------|
| id         | BIGSERIAL PK |                               |
| email      | VARCHAR      | unique                        |
| password   | VARCHAR      | BCrypt-hashed                 |
| role       | VARCHAR      | `USER` or `ADMIN` (enum)      |

### `tasks`
| Column      | Type         | Notes                                    |
|-------------|--------------|------------------------------------------|
| id          | BIGSERIAL PK |                                          |
| title       | VARCHAR      |                                          |
| type        | VARCHAR      | `DOCUMENT_SUMMARY` / `INVOICE_REVIEW`    |
| input_text  | TEXT         |                                          |
| output_json | TEXT         | serialized structured result             |
| status      | VARCHAR      | `PENDING` / `COMPLETED` / `FAILED`       |
| created_at  | TIMESTAMP    |                                          |
| user_id     | BIGINT FK    | → `users.id`                             |

### `executions`
| Column            | Type         | Notes                       |
|-------------------|--------------|-----------------------------|
| id                | BIGSERIAL PK |                             |
| task_id           | BIGINT FK    | → `tasks.id`                |
| decision          | VARCHAR      | e.g. `PASS`, `FAIL`         |
| confidence        | DOUBLE       | 0.0 – 1.0                   |
| explanation       | TEXT         |                             |
| execution_time_ms | BIGINT       |                             |
| executed_at       | TIMESTAMP    |                             |

Schema is auto-managed by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

## API Endpoints

### Auth (public)
| Method | Path             | Description                          |
|--------|------------------|--------------------------------------|
| POST   | `/auth/register` | Create a new user, returns JWT       |
| POST   | `/auth/login`    | Authenticate, returns JWT            |

### Tasks (authenticated)
| Method | Path           | Description                                  |
|--------|----------------|----------------------------------------------|
| POST   | `/tasks`       | Create + execute a task, returns result      |
| GET    | `/tasks`       | List the current user's tasks (history)      |
| GET    | `/tasks/{id}`  | Get a single task by id                      |

### Agents (authenticated)
| Method | Path           | Description                                  |
|--------|----------------|----------------------------------------------|
| POST   | `/agents/run`  | Run a named agent on input                   |
| GET    | `/agents`      | List available agents                        |

### System (public)
| Method | Path     | Description     |
|--------|----------|-----------------|
| GET    | `/health`| Health check    |

### Structured task output

Every task returns the spec-required shape:

```json
{
  "status": "completed",
  "decision": "PASS",
  "confidence": 0.89,
  "explanation": "All required fields detected: invoice number, amount/total, date",
  "timestamp": "2026-06-22T14:23:45.123Z"
}
```

## API Documentation

Interactive Swagger UI is available at:

```
<YOUR_LIVE_URL>/swagger-ui.html
```

Click **Authorize** in the top-right, paste a JWT (without `Bearer`), and all protected endpoints become testable from the browser.

OpenAPI JSON spec: `<YOUR_LIVE_URL>/v3/api-docs`

## AI Integration Approach

This implementation uses a **rule-based engine** for AI tasks and agents — explicitly permitted by the task spec. The reasoning:

- Deterministic, no external API dependency, no API keys leaking to a public repo.
- Reliable in a free-tier deployment (no quota or rate-limit surprises).
- Architecture is provider-agnostic: swapping in Gemini or OpenAI later is a single new `AiTaskHandler` or `Agent` implementation — no changes to controllers, services, or DTOs.

### Disclosure — AI-generated logic

The internal logic of the **execution engine handlers** (`DocumentSummaryHandler`, `InvoiceReviewHandler`) and the **agent implementations** (`DocumentAnalystAgent`, `InvoiceAuditorAgent`) was generated with AI assistance. This includes the regex patterns for invoice field detection, the confidence-scoring formulas, and the summarization heuristics. The surrounding architecture (interfaces, registry, dispatch, persistence, security, controllers, DTOs) was designed and assembled by me. I list this transparently because the eval guidance values honesty over the appearance of solo work.

### Task types implemented

- **`DOCUMENT_SUMMARY`** — extracts the first 1–2 sentences as a summary, returns word/sentence stats and a heuristic confidence score scaled by input length.
- **`INVOICE_REVIEW`** — regex-checks for required fields (invoice number, amount/total, date). Decision is `PASS` if all present, `FAIL` otherwise. Confidence = fraction of fields found.

### Agents implemented

- **`DocumentAnalyst`** — produces a structural report of a document (word/sentence/paragraph counts, opening line, readability estimate).
- **`InvoiceAuditor`** — extracts amounts and invoice numbers, flags missing fields with `WARNING` markers.

## What's Real vs. Mocked

| Component                       | Status                                                    |
|---------------------------------|-----------------------------------------------------------|
| Authentication (JWT)            | **Real** — Spring Security OAuth2 Resource Server, BCrypt |
| RBAC (USER / ADMIN)             | **Real** — enforced via `JwtAuthenticationConverter`      |
| Database persistence            | **Real** — PostgreSQL via Spring Data JPA                 |
| Task & execution history        | **Real** — stored in DB, scoped per user                  |
| Swagger / OpenAPI docs          | **Real** — springdoc auto-generated                       |
| Task execution engine           | **Mock** (not an LLM call) — AI Generated logic           |
| Agents                          | **Mock** (not an LLM call) — AI Generated Logic           |
| Async task queue / events       | Not implemented (synchronous execution only)              |

### Environment variables

| Variable                | Default                            | Purpose                  |
|-------------------------|------------------------------------|--------------------------|
| `JWT_SECRET`            | dev fallback in `application.properties` | HMAC signing key (≥32 bytes) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/aipass` | Postgres connection      |
| `SPRING_DATASOURCE_USERNAME` | `aipass`                       | DB user                  |
| `SPRING_DATASOURCE_PASSWORD` | `aipass`                       | DB password              |

In production, **override `JWT_SECRET`** with a strong random value.

## Deployment

Deployed to **Render** with a managed PostgreSQL instance.

### Deployment steps (Render)

1. Push the repo to GitHub.
2. On Render: **New → PostgreSQL** → copy the internal connection string.
3. **New → Web Service** → connect the GitHub repo.
   - **Build command (Included in Docker file):** `./mvnw clean package -DskipTests`
   - **Start command (Included in Docker file):** `java -jar target/*.jar`
   - **Environment variables:**
     - `SPRING_DATASOURCE_URL` = ``
     - `SPRING_DATASOURCE_USERNAME` = ``
     - `SPRING_DATASOURCE_PASSWORD` = ``
     - `JWT_SECRET` = ``
4. Deploy. First boot Hibernate auto-creates the schema.

## Future Improvements

If time allowed, the next steps would be:

- **Real AI provider integration** — implement `GeminiTaskHandler` / `OpenAiTaskHandler` behind the existing `AiTaskHandler` interface. Switchable per task type via config.
- **Workflow chaining** — let users define multi-step pipelines (e.g. `DocumentSummary → RiskAssessment → notification`) as DAGs persisted in the DB.
- **Audit + observability** — Micrometer metrics, structured JSON logging, request tracing.
- **Database migrations** — replace `ddl-auto=update` with Flyway for proper schema versioning.
- **Admin endpoints** — `/admin/users`, `/admin/tasks` for role-promotion and full visibility.
- **Rate limiting** — per-user request throttling to protect AI provider quotas.
- **Refresh tokens** — currently only access tokens are issued; refresh tokens would extend sessions safely.

## Evaluation Checklist

| Requirement                          | Status         |
|--------------------------------------|----------------|
| Authentication module (JWT)          | ✅ done        |
| User + Admin roles (RBAC)            | ✅ done        |
| `POST /tasks` with task types        | ✅ done        |
| Execution engine                     | ✅ rule-based  |
| Structured output                    | ✅ done        |
| Task history endpoint                | ✅ done        |
| `POST /agents/run`                   | ✅ done        |
| PostgreSQL persistence               | ✅ done        |
| Swagger / OpenAPI                    | ✅ done        |
| `GET /health`                        | ✅ done        |
| Layered architecture                 | ✅ done        |
| Live deployment                      | ✅ done        |
| Docker support (bonus)               | ✅ Compose for DB |
| AI provider abstraction (bonus)      | ✅ handler/agent interfaces |
| Agent registry (bonus)               | ✅ done        |
