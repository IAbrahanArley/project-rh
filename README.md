# RH - Recrutamento Interno

Aplicação full stack para recrutamento interno. O sistema permite autenticar usuários, consultar vagas, cadastrar e editar vagas como RH, candidatar-se como colaborador, acompanhar candidaturas, atualizar status, registrar avaliações e receber notificações.

## Deploy

O projeto está publicado na AWS e pode ser acessado sem configuração local:

```text
http://3.92.70.142
```
## Stack

- Backend: Java 21, Spring Boot 3.5, Spring Security, JWT, JPA, Flyway e PostgreSQL
- Frontend: Angular com Reactive Forms
- Infra: Docker, Docker Compose, Nginx, AWS EC2, S3 e GitHub Actions

## Como Rodar Localmente Com Docker

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

O Docker Compose sobe três serviços:

- `postgres`: banco PostgreSQL
- `api`: backend Spring Boot
- `frontend`: aplicação Angular servida por Nginx

Observação: o ambiente local sobe a aplicação, banco e API. Para testar upload real de currículo localmente, também é necessário configurar as variáveis do S3 descritas abaixo.

## Como Rodar Localmente Separado

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

URLs padrão:

```text
Frontend dev: http://localhost:5173
API:          http://localhost:8080
```

## Dados Iniciais

A aplicação não cria usuários ou vagas automaticamente. Crie contas de candidato pelo fluxo público de cadastro.
Usuários administrativos devem ser provisionados diretamente no banco ou por uma rotina interna controlada.

## Variáveis Principais

Backend:

```text
DB_URL=jdbc:postgresql://localhost:5432/rh
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-development-secret-with-at-least-32-chars
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173
RESUME_BUCKET=nome-do-bucket-privado
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=chave-com-acesso-ao-bucket
AWS_SECRET_ACCESS_KEY=segredo-com-acesso-ao-bucket
RESUME_KEY_PREFIX=resumes
RESUME_MAX_SIZE_BYTES=10485760
RESUME_UPLOAD_URL_EXPIRATION_SECONDS=300
RESUME_DOWNLOAD_URL_EXPIRATION_SECONDS=300
```
Frontend em Docker:

```text
FRONTEND_API_BASE_URL=http://localhost:8080
```

Em produção, deixe `FRONTEND_API_BASE_URL` vazio quando frontend e API estiverem na mesma origem.

## Rotas Principais

Autenticação:

```http
POST /api/auth/login
POST /api/auth/candidate/register
GET /api/auth/me
```

Currículos:

```http
GET /api/candidates/me/resume
POST /api/candidates/me/resume/upload-url
POST /api/candidates/me/resume/complete
GET /api/candidates/me/resume/download-url
GET /api/candidates/{candidateId}/resume/download-url
```

O bucket de currículos deve ser privado. Para upload direto pelo navegador via URL assinada, configure CORS no bucket
permitindo `PUT` a partir da origem do frontend e o header `Content-Type`.

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

`GET /api/jobs` aceita filtros opcionais:

```http
GET /api/jobs?status=OPEN&q=java&department=Tecnologia&location=Remoto&page=0&size=10
```

Candidaturas:

```http
GET /api/applications/me
PATCH /api/applications/{id}/status
POST /api/applications/{id}/evaluation
```

Notificações:

```http
GET /api/notifications/me
GET /api/notifications/me/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/read-all
