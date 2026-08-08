# RH - Recrutamento Interno

Aplicação full stack para recrutamento interno. O sistema permite autenticar usuários, consultar vagas, cadastrar e editar vagas como RH, candidatar-se como colaborador, acompanhar candidaturas, atualizar status, registrar avaliações e receber notificações.

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

O Docker Compose sobe três serviços:

- `postgres`: banco PostgreSQL
- `api`: backend Spring Boot
- `frontend`: aplicação Angular servida por Nginx

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
RESUME_MAX_SIZE_BYTES=10485760
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
```

## CI

O projeto possui um workflow de CI em `.github/workflows/ci.yml` que roda em push e pull request para as branches `main` e `master`.

Ele valida:

- testes do backend
- build do frontend
- build das imagens Docker
- arquivo `docker-compose.yml`

## Deploy automático AWS

O workflow `.github/workflows/deploy-aws.yml` faz deploy automático para a EC2 em todo push para `main` ou `master`.

Configure estes secrets no GitHub em `Settings > Secrets and variables > Actions`:

```text
AWS_EC2_HOST=3.92.70.142
AWS_EC2_USER=ec2-user
AWS_EC2_SSH_KEY=conteúdo completo da chave privada SSH da EC2
```

O deploy executa:

- testes do backend;
- build do frontend com `npm run build:aws`;
- empacotamento do backend, Nginx e build Angular;
- upload para `/home/ec2-user/rh`;
- rebuild da imagem `rh-api`;
- restart dos containers `rh-api` e `rh-api-proxy`.

O arquivo `.env.aws` permanece somente na EC2 e não é enviado pelo GitHub Actions.

## Deploy AWS

Arquitetura sugerida para uma publicação simples:

```text
CloudFront
  /      -> S3 privado com build Angular
  /api/* -> EC2 na porta 80

EC2
  Nginx -> Spring Boot API
  PostgreSQL no mesmo Docker Compose
```

Checklist inicial na AWS:

1. Ativar MFA no usuário root.
2. Criar Budget de alerta em `US$ 1`.
3. Usar uma única região, preferencialmente `us-east-1`.
4. Criar uma EC2 adequada ao volume esperado.
5. Security Group da EC2:
   - `22`: somente seu IP.
   - `80`: público para acesso HTTP.

Na EC2, copie `.env.aws` para `.env.aws`, preencha os segredos e suba a API:

```bash
docker compose --env-file .env.aws -f docker-compose.aws.yml up -d --build
```

Para o frontend AWS:

```bash
cd frontend
npm ci
npm run build:aws
```

Envie o conteúdo de `frontend/dist/rh-frontend/browser` para um bucket S3 privado e crie uma distribuição CloudFront com:

- origem S3 para o comportamento padrão `/*`;
- origem EC2 para o comportamento `/api/*`;
- Origin Access Control no S3;
- `CORS_ALLOWED_ORIGINS` na EC2 apontando para a URL final do CloudFront.
