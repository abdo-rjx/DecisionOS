# DecisionOS

Decision-support simulator under uncertainty.

- Backend: Spring Boot 3.x (Java 21, Maven) — `decisionos-backend/`
- Frontend: Next.js 14+ (TypeScript, App Router, Tailwind) — `decisionos-frontend/`
- Infra: Docker Compose (Postgres 16 + backend + frontend)

See `project.md` for the full blueprint (single source of truth).

## Quickstart

```bash
cp .env.example .env
docker compose up --build
```

- Backend health: http://localhost:8080/api/v1/health
- LLM test: http://localhost:8080/api/v1/llm-test
- Frontend: http://localhost:3000
