# Handoff — SmartRecruit AI (VCube Hack Quest 1.0, Problem 1; deadline 21 Nov 2026, 3:00 PM IST)

## State
- Backend: Spring Boot 4.1.1, Java 25, JPA, Spring Security, JWT (oauth2-resource-server, HS256, roles in `roles` claim), BCrypt, MySQL (TiDB Cloud), PDFBox, LLM via OpenAI-compatible `/chat/completions` (Gemini default). Package `com.smartrecruit`.
- Roles: CANDIDATE, RECRUITER (HR), ADMIN (seeded from ADMIN_EMAIL/ADMIN_PASSWORD; cannot self-register).
- Tables: users, candidates, jobs, applications, resume_analysis.
- AI: analyze-resume, match, interview-questions, improve-resume (+ recruiter screening).
- Frontend: React 19 + Vite 8, LinkedIn-style UI, landing page for guests (`pages/Landing.jsx`), two-pane jobs board, admin pages.
- Optional demo data: set `SEED_DEMO=true` (only when jobs table empty).
- Tests: backend `RecruitFlowTest`, `SecurityTest` (H2, LLM mocked) — NEVER COMPILED in the authoring sandbox (Maven Central blocked); run `./mvnw test` and fix errors. Frontend: `npm test` (Vitest, 6 passing).
- Deliverables present: Dockerfile, render.yaml, docs/er-diagram.png, docs/SmartRecruit.postman_collection.json, README.md.

## Rules
- Never paste secrets in chat; they go into Render/Vercel env vars. Old Aiven password must be rotated.
- Boot 4 test imports: `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`, `org.springframework.test.context.bean.override.mockito.MockitoBean`. Test `application.properties` shadows main one.

## Remaining
1. User runs `./mvnw test`, fixes any compile/test errors.
2. Run locally against TiDB, walk through all three roles.
3. Deploy: Render (Docker, env vars in README) then Vercel (`VITE_API_BASE_URL`), then set CORS_ALLOWED_ORIGINS.
4. Optional hardening: security headers, rate limiting on AI endpoints.
5. Record demo, submit GitHub URL, live URL, ER diagram, Postman collection, README.
