# Book Store

Full-stack bookstore demo built with **React + Spring Boot + MySQL**.

## Features

- Public responsive book catalog at `/`
- Search by title, author or category
- Staff management demo at `/admin`
- REST endpoints for books and staff
- Environment-based MySQL configuration
- Demo seed data for a fresh database
- Dockerized production build serving React and Spring Boot from one URL
- Railway-ready deployment configuration

## Run locally with Docker

```bash
docker compose up --build
```

Open http://localhost:8080 and http://localhost:8080/admin.

## Deploy

See [DEPLOY_RAILWAY.md](DEPLOY_RAILWAY.md).
