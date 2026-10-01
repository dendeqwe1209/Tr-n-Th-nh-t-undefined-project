# Deploy Book Store to Railway

This repository is prepared so the React frontend and Spring Boot backend are served from **one public URL**. MySQL runs as a separate Railway service.

## 1. Push this repository to GitHub

Railway can deploy directly from a GitHub repository. The root `Dockerfile` builds React, bundles it into Spring Boot, and starts a single web service.

## 2. Create the Railway project

1. In Railway, create a **New Project**.
2. Add a **MySQL** service.
3. Add a service from your **GitHub repository**.
4. Railway will automatically detect the root `Dockerfile`.

## 3. Add variables to the app service

In the application service > Variables, add references to the MySQL service values:

```text
MYSQL_HOST=${{MySQL.MYSQLHOST}}
MYSQL_PORT=${{MySQL.MYSQLPORT}}
MYSQL_DATABASE=${{MySQL.MYSQLDATABASE}}
MYSQL_USER=${{MySQL.MYSQLUSER}}
MYSQL_PASSWORD=${{MySQL.MYSQLPASSWORD}}
APP_SEED_DEMO=true
CORS_ALLOWED_ORIGINS=*
```

If your Railway database service has a different name, replace `MySQL` with that service name.

`APP_SEED_DEMO=true` inserts four demo books only when the book table is empty. Set it to `false` later if you do not want demo seed data.

## 4. Configure health check

In the application service settings, set:

```text
Healthcheck path: /api/health
```

## 5. Generate the public link

Open the app service > **Settings > Networking > Generate Domain**.

Your public URL will look similar to:

```text
https://your-book-store.up.railway.app
```

Share that URL. The home page is the public catalog and `/admin` opens the current admin demo.

## Run the deploy-ready version locally

With Docker installed:

```bash
docker compose up --build
```

Then open:

```text
http://localhost:8080
```

For separate frontend/backend development, start MySQL and Spring Boot on port 8080, then run React with `npm start` on port 3000. The frontend automatically calls `http://localhost:8080` in development.
