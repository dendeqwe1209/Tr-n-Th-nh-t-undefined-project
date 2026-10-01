# Build React frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/book_store_frontend/package.json ./
RUN npm install --no-audit --no-fund
COPY frontend/book_store_frontend/ ./
RUN npm run build

# Build Spring Boot backend and bundle the React build into the JAR
FROM maven:3.9.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY backend/book_store_backend/pom.xml ./pom.xml
COPY backend/book_store_backend/src ./src
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static
RUN mvn -DskipTests clean package

# Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
