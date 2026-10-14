# FoodDecode

MCA mini-project: scan, upload, or search a packaged food product and get plain-language ingredient explanations, nutrition facts, a health score, and healthier alternatives.

**Stack (fixed):** React (Vite) · Spring Boot 3.x · PostgreSQL · Spring Data JPA · Spring Security + JWT · Open Food Facts · Gemini · `html5-qrcode`

Follow `PROJECT_BRIEF.md` for the full module spec. Build order is strict: database → backend modules → Postman → frontend.

---

## Phase 1 status — Database

**Done:** PostgreSQL schema, Flyway migrations, seed data, matching JPA entities.

**Schema ownership:** Flyway. Hibernate is set to `ddl-auto: validate` (it will not create or alter tables).

### 1. One-time: create the empty database

PostgreSQL 17 is expected at `localhost:5432`. In pgAdmin or `psql`:

```sql
CREATE DATABASE fooddecode;
```

PowerShell example:

```powershell
$env:PGPASSWORD = "your_postgres_password"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE fooddecode;"
```

### 2. Secrets file

```powershell
copy backend\src\main\resources\application-secrets.example.properties backend\src\main\resources\application-secrets.properties
```

Edit `application-secrets.properties` and set `spring.datasource.password` (and later JWT / Gemini / mail). That file is gitignored.

### 3. Apply schema + seed (recommended: Flyway)

Open the `backend` folder in IntelliJ or Cursor and run `FoodDecodeApplication`. On first start, Flyway runs:

- `V1__create_schema.sql` — all tables, FKs, indexes
- `V2__seed_data.sql` — 2 users, 8 products, ingredients, nutrition, sample history/favorites

You need **JDK 17** on `PATH`. This machine has it at `C:\Program Files\Java\jdk-17`. If `java` is not recognised:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

If you prefer pgAdmin instead of Flyway, run `database/schema.sql` then `database/seed.sql` against `fooddecode`. Do **not** also start the app with Flyway after that (tables would already exist and `CREATE TABLE` would fail). Pick one path.

### 4. Verify in pgAdmin / psql

```sql
SELECT name, brand, health_score FROM products ORDER BY health_score DESC;
SELECT COUNT(*) FROM ingredients;
SELECT p.name, n.calories, n.sugar_g, n.sodium_mg
FROM products p JOIN nutrition_facts n ON n.product_id = p.id;
```

Seed logins:

| Email | Password | Role |
|---|---|---|
| `user@fooddecode.dev` | `Password@123` | USER |
| `admin@fooddecode.dev` | `Password@123` | ADMIN |

## API testing (Postman)

Import `postman/FoodDecode.postman_collection.json`. Collection variables: `baseUrl` (`http://localhost:8080`), `accessToken`, `adminAccessToken`, `refreshToken`. **Login (user)** and **Login (admin)** scripts store JWTs so authenticated folders work without pasting tokens.

## Scan history (frontend note)

The API does **not** auto-create history rows after search or barcode lookup. The React client must call `POST /api/scan-history` with `{ "productId": <id>, "scanMethod": "SEARCH" | "BARCODE" | "IMAGE" }` after every successful lookup. `GET /api/users/me/history` is paginated.

Image scan (`POST /api/products/scan-image`) is a **manual-entry stub** (`// TODO: AI image analysis`) — no OCR yet.

## Still pending

8–10. React frontend, integration, polish

### Health-score formula (precomputed on seed rows)

Start at 100, then: −8 per harmful ingredient (cap −40); extra −15 if any `trans_fat`; extra −10 if any `artificial_sweetener`; −20 if sugar &gt; 25 g else −10 if sugar &gt; 15 g; −15 if sodium &gt; 800 mg else −8 if sodium &gt; 400 mg; −5 if calories &gt; 300; +5 if fibre ≥ 3 g; +5 if protein ≥ 8 g; clamp 0–100.

---

## Project layout

```
database/schema.sql          # same as Flyway V1 (for viva / pgAdmin)
database/seed.sql            # same as Flyway V2
backend/                    # Spring Boot 3.3, Java 17
frontend/                    # not created yet (Phase 8)
```

## Run the frontend (Phase H)

```powershell
cd frontend
npm install
npm run dev
```

Vite serves `http://localhost:5173` and proxies `/api` to `http://localhost:8080`. Keep the backend running.

Seed UI logins: `user@fooddecode.dev` / `Password@123` and `admin@fooddecode.dev` / `Password@123`.
