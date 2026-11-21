# SmartRecruit AI — AI-Powered Smart Recruitment & Talent Management

VCube Hack Quest 1.0, Problem 1. Java full-stack: **Spring Boot 4 + JPA + Spring Security (JWT) + MySQL + React**.

## Roles
| Role | Can do |
|---|---|
| Candidate | Profile, upload resume (PDF/TXT), search jobs, apply, AI features, track applications |
| HR / Recruiter | Post/edit/close jobs, view applicants, AI screening, change status, analytics |
| Administrator | Dashboard, enable/disable users, close/reopen any job. Seeded from env vars; cannot self-register |

## AI features (Spring Boot backend, real data from the database)
1. Resume analysis (`POST /api/ai/analyze-resume`)
2. Resume to job match score (`POST /api/ai/match`)
3. Interview question generation (`POST /api/ai/interview-questions`)
4. Resume improvement suggestions (`POST /api/ai/improve-resume`)
Recruiters also get AI applicant screening. The LLM is called through an OpenAI-compatible endpoint (Gemini by default).

## Database (5 tables) — see `docs/er-diagram.png`
`users`, `candidates`, `jobs`, `applications`, `resume_analysis`.

## Security
BCrypt password hashing, stateless JWT (resource server filter), role-based `@PreAuthorize`, login lockout (5 failures / 15 min), CORS allow-list, upload type/size checks, resume text treated as untrusted in prompts.

## Run locally
```bash
# backend (needs MySQL database `recruit`)
export DB_URL=jdbc:mysql://localhost:3306/recruit DB_USERNAME=root DB_PASSWORD=...
export JWT_SECRET=<32+ random chars> ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=<8+ chars>
export AI_API_KEY=<gemini key>
./mvnw spring-boot:run
# frontend
cd frontend && npm install && npm run dev   # proxies /api to :8080
```
Tests: `./mvnw test` (H2, LLM mocked).

## Environment variables
`DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD, AI_API_KEY, AI_BASE_URL, AI_MODEL, CORS_ALLOWED_ORIGINS`. Never commit secrets.

## Deploy
- **API (Render):** New Web Service from this repo (Docker) or Blueprint using `render.yaml`; set the env vars above; health check `/actuator/health`. For TiDB/Aiven use a TLS JDBC URL (e.g. `jdbc:mysql://HOST:4000/recruit?sslMode=VERIFY_IDENTITY`).
- **Frontend (Vercel):** root `frontend`, env `VITE_API_BASE_URL=<render url>`; then set `CORS_ALLOWED_ORIGINS` on Render to the Vercel URL.

## API testing
Import `docs/SmartRecruit.postman_collection.json` (variables `baseUrl`, `token`; login requests save the token).
