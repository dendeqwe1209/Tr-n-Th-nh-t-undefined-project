# Book Store — deploy-ready demo

Stack: React 18 + Spring Boot 3.2 / Java 17 + MySQL.

## Added/fixed

- Public bookstore catalog at `/` with search and responsive book cards.
- Public API at `/api/books`.
- Health endpoint at `/api/health`.
- Fixed Staff API URLs and DTO field-name mismatches.
- Fixed Add/Edit Staff modal integration.
- Environment-based database/CORS configuration.
- Optional demo seed data (`APP_SEED_DEMO=true`).
- Multi-stage Dockerfile that serves React + Spring Boot from one URL.
- Docker Compose for local testing.
- Railway deployment guide in `DEPLOY_RAILWAY.md`.

## Local Docker run

```bash
docker compose up --build
```

Open `http://localhost:8080`.
