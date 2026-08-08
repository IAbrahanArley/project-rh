# RH - Recrutamento Interno

Aplicacao full stack para recrutamento interno. O sistema permite autenticar usuarios, consultar vagas, cadastrar e editar vagas como RH, candidatar-se como colaborador, acompanhar candidaturas, atualizar status, registrar avaliacoes e receber notificacoes.

## Stack

- Backend: Java 21, Spring Boot 3.5, Spring Security, JWT, JPA, Flyway e PostgreSQL
- Frontend: Angular com Reactive Forms
- Infra: Docker e Docker Compose

## Como Iniciar Com Docker

Na raiz do projeto, execute:

```bash
docker compose up --build
```

Depois acesse:

```text
Frontend: http://localhost:4200
API:      http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
```

O Docker Compose sobe tres servicos:

- `postgres`: banco PostgreSQL
- `api`: backend Spring Boot
- `frontend`: aplicacao Angular servida por Nginx

## Como Iniciar Separado

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

URLs padrao:

```text
Frontend dev: http://localhost:5173
API:          http://localhost:8080
```

## Usuarios Demo

Administrador:

```text
usuario: admin
senha: admin123
```

Colaborador:

```text
usuario: colaborador
senha: user123
```

## Variaveis Principais

Backend:

```text
DB_URL=jdbc:postgresql://localhost:5432/rh
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-development-secret-with-at-least-32-chars
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173
```

Frontend em Docker:

```text
FRONTEND_API_BASE_URL=http://localhost:8080
```

## Rotas Principais

Autenticacao:

```http
POST /api/auth/login
GET /api/auth/me
```

Vagas:

```http
GET /api/jobs
GET /api/jobs/{id}
POST /api/jobs
PUT /api/jobs/{id}
DELETE /api/jobs/{id}
POST /api/jobs/{id}/applications
GET /api/jobs/{id}/applications
```

Candidaturas:

```http
GET /api/applications/me
PATCH /api/applications/{id}/status
POST /api/applications/{id}/evaluation
```

Notificacoes:

```http
GET /api/notifications/me
GET /api/notifications/me/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/read-all
```

