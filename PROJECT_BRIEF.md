# FoodDecode — project brief (source of truth)

Use this file in every chat. Do not swap the stack. Do not skip build order.

## Identity

**FoodDecode** is an MCA mini-project: a HealthTech web app where a user scans, uploads, or searches a packaged food product and gets ingredient explanations in plain language, nutrition facts, a health score, and healthier alternatives.

## Stack (fixed)

| Layer | Technology |
|---|---|
| Frontend | React.js (Vite), plain CSS or Tailwind |
| Backend | Java 17+, Spring Boot 3.x |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + JWT (access + refresh) |
| API testing | Postman collection |
| Product data | Open Food Facts API (no key) |
| AI | Gemini (`gemini-1.5-flash` or `gemini-2.0-flash`) |
| Barcode | `html5-qrcode` or `zxing-js` |
| Email/OTP | Spring Boot Mail Starter |
| VCS | Git & GitHub |

Do not substitute MongoDB or Node/Express.

## Build order (strict)

1. Database — schema, Flyway, seed
2. Backend core — skeleton, entities, repositories, security config
3. Auth — register, login, forgot password + OTP, JWT
4. Product/scan — search, Open Food Facts, ingredients/nutrition
5. Favorites / history / profile / admin
6. AI layer — Gemini + `ai_response_cache`
7. Postman collection
8. Frontend pages
9. Integration pass (no mocks)
10. Polish

After every phase: say what was built, how to run/test, and what is still pending. Do not silently start the next phase.

## Database entities

`users`, `otp_verifications`, `products`, `ingredients`, `product_ingredients`, `ingredient_explanations`, `nutrition_facts`, `scan_history`, `favorites`, `ai_response_cache`, `admins`.

PKs are `BIGSERIAL`. Synopsis ENUMs are VARCHAR + CHECK (same labels) so JPA can use `EnumType.STRING`.

Indexes: `users.email`, `products.barcode`, `ai_response_cache.cache_key`.

`ON DELETE CASCADE`: `otp_verifications`, `scan_history`, `favorites`, `admins` when a user is deleted; product-owned rows cascade when a product is deleted.

## API modules (implement in later phases)

Consistent errors: `{ "success": false, "message": "...", "errors": [] }`.

**Auth:** `POST /api/auth/register|login|forgot-password|verify-otp|reset-password|refresh-token|logout`. OTP: hashed, 10 min, max 3 per 15 min per email.

**Products:** `GET /api/products/search`, `GET /api/products/barcode/{barcode}`, `POST /api/products/scan-image` (MVP helper, `// TODO: AI image analysis`), `GET /api/products/{id}`.

**Ingredients:** `GET /api/products/{id}/ingredients`, `GET /api/ingredients/{id}/explanation?lang=en`.

**Nutrition / compare / alternatives:** `GET /api/products/{id}/nutrition`, `GET /api/products/compare?ids=1,2`, `GET /api/products/{id}/alternatives`.

**AI:** `AiService` — explain, translate, summarize, alternatives; always check cache first.

**History / favorites / profile:** `/api/users/me/history`, `/api/scan-history`, `/api/favorites`, `/api/users/me`.

**Admin (`ROLE_ADMIN`):** `/api/admin/products`, `/api/admin/ingredients`, `/api/admin/analytics`.

**Security:** public `/api/auth/**` and guest `GET /api/products/**`; authenticated `/api/users/me/**`, `/api/scan-history/**`, `/api/favorites/**`; admin `/api/admin/**`. CORS: `http://localhost:5173`.

## Frontend pages (Phase 8)

Auth (login, register, 3-step forgot password) · Dashboard · Search results · Product detail (overview, ingredients + simplify, nutrition, health-score gauge, alternatives) · Scanner (`html5-qrcode` + manual barcode + image upload) · Favorites · History · Profile · Admin (tables + Recharts).

Axios instance with JWT interceptor; 401 → login.

## Health-score formula (viva)

Start at 100. −8 per harmful ingredient (cap −40). Extra −15 for `trans_fat`, −10 for `artificial_sweetener`. −20 if sugar_g > 25 else −10 if > 15. −15 if sodium_mg > 800 else −8 if > 400. −5 if calories > 300. +5 if fibre_g ≥ 3. +5 if protein_g ≥ 8. Clamp 0–100.

## Secrets

Never hardcode keys. Backend: gitignored `application-secrets.properties` (see `application-secrets.example.properties`). Frontend: `.env` from `.env.example`.

## Assumptions (flagged)

- Integer `BIGSERIAL` PKs (not UUID) so seed IDs and Postman examples stay simple.
- Synopsis ENUMs stored as VARCHAR + CHECK, not native PostgreSQL ENUM types.
- Refresh-token table is **not** in the ER diagram; it will be added in the auth phase if logout/invalidation needs a store.
- New database name is `fooddecode` (does not touch any older local `FoodDecodeDB`).
