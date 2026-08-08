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

## Usuarios de Desenvolvimento

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
SEED_DEMO_DATA=true
```

Frontend em Docker:

```text
FRONTEND_API_BASE_URL=http://localhost:8080
```

## Rotas Principais

Autenticacao:

```http
POST /api/auth/login
POST /api/auth/candidate/register
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

## CI

O projeto possui um workflow de CI em `.github/workflows/ci.yml` que roda em push e pull request para as branches `main` e `master`.

Ele valida:

- testes do backend
- build do frontend
- build das imagens Docker
- arquivo `docker-compose.yml`

## Deploy automatico AWS

O workflow `.github/workflows/deploy-aws.yml` faz deploy automatico para a EC2 em todo push para `main` ou `master`.

Configure estes secrets no GitHub em `Settings > Secrets and variables > Actions`:

```text
AWS_EC2_HOST=3.92.70.142
AWS_EC2_USER=ec2-user
AWS_EC2_SSH_KEY=conteudo completo do arquivo rh-free-tier-key.pem
```

O deploy executa:

- testes do backend;
- build do frontend com `npm run build:aws`;
- empacotamento do backend, Nginx e build Angular;
- upload para `/home/ec2-user/rh`;
- rebuild da imagem `rh-api`;
- restart dos containers `rh-api` e `rh-api-proxy`.

O arquivo `.env.aws` permanece somente na EC2 e nao e enviado pelo GitHub Actions.

## Deploy AWS Free Tier

Arquitetura sugerida para evitar custo fixo alto no inicio:

```text
CloudFront
  /      -> S3 privado com build Angular
  /api/* -> EC2 na porta 80

EC2
  Nginx -> Spring Boot API
  PostgreSQL no mesmo Docker Compose
```

Evite nesta primeira versao: RDS, Load Balancer, NAT Gateway, ECS/Fargate, Elastic IP parado e dominio proprio.

Checklist inicial na AWS:

1. Ativar MFA no usuario root.
2. Criar Budget de alerta em `US$ 1`.
3. Usar uma unica regiao, preferencialmente `us-east-1`.
4. Criar uma EC2 marcada como `Free tier eligible`; os tipos elegiveis dependem da data de criacao da conta.
5. Security Group da EC2:
   - `22`: somente seu IP.
   - `80`: publico, para o CloudFront acessar a API.

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

Envie o conteudo de `frontend/dist/rh-frontend/browser` para um bucket S3 privado e crie uma distribuicao CloudFront com:

- origem S3 para o comportamento padrao `/*`;
- origem EC2 para o comportamento `/api/*`;
- Origin Access Control no S3;
- `CORS_ALLOWED_ORIGINS` na EC2 apontando para a URL final do CloudFront.
