-- FoodDecode PostgreSQL schema (synopsis ER diagram)
-- Enums are stored as VARCHAR + CHECK so Spring Data JPA can use EnumType.STRING
-- without extra Hibernate enum mappers. Values match the synopsis ENUMs.

CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    full_name           VARCHAR(150) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    preferred_language  VARCHAR(10)  NOT NULL DEFAULT 'en',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE otp_verifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    otp_code    VARCHAR(255) NOT NULL,
    purpose     VARCHAR(30)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_otp_purpose CHECK (purpose IN ('PASSWORD_RESET', 'EMAIL_VERIFY'))
);

CREATE INDEX idx_otp_user_id ON otp_verifications (user_id);

CREATE TABLE products (
    id            BIGSERIAL PRIMARY KEY,
    barcode       VARCHAR(64),
    name          VARCHAR(255) NOT NULL,
    brand         VARCHAR(150),
    category      VARCHAR(100),
    image_url     TEXT,
    source        VARCHAR(30)  NOT NULL DEFAULT 'ADMIN_ADDED',
    health_score  INTEGER,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_products_barcode UNIQUE (barcode),
    CONSTRAINT ck_products_source CHECK (source IN ('OPEN_FOOD_FACTS', 'ADMIN_ADDED')),
    CONSTRAINT ck_products_health_score CHECK (health_score IS NULL OR (health_score BETWEEN 0 AND 100))
);

CREATE INDEX idx_products_barcode ON products (barcode);

CREATE TABLE ingredients (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    is_harmful     BOOLEAN      NOT NULL DEFAULT FALSE,
    harm_category  VARCHAR(50),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ingredients_name UNIQUE (name)
);

CREATE TABLE product_ingredients (
    product_id        BIGINT  NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    ingredient_id     BIGINT  NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    position_in_list  INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (product_id, ingredient_id)
);

CREATE TABLE ingredient_explanations (
    id                   BIGSERIAL PRIMARY KEY,
    ingredient_id        BIGINT       NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    language_code        VARCHAR(10)  NOT NULL DEFAULT 'en',
    simple_explanation   TEXT         NOT NULL,
    ai_generated         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ingredient_lang UNIQUE (ingredient_id, language_code)
);

CREATE TABLE nutrition_facts (
    id            BIGSERIAL PRIMARY KEY,
    product_id    BIGINT        NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    calories      NUMERIC(10, 2),
    protein_g     NUMERIC(10, 2),
    carbs_g       NUMERIC(10, 2),
    fats_g        NUMERIC(10, 2),
    sugar_g       NUMERIC(10, 2),
    sodium_mg     NUMERIC(10, 2),
    fibre_g       NUMERIC(10, 2),
    serving_size  VARCHAR(50),
    CONSTRAINT uk_nutrition_product UNIQUE (product_id)
);

CREATE TABLE scan_history (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id   BIGINT      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    scan_method  VARCHAR(20) NOT NULL,
    scanned_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_scan_method CHECK (scan_method IN ('BARCODE', 'IMAGE', 'SEARCH'))
);

CREATE INDEX idx_scan_history_user_id ON scan_history (user_id);

CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id  BIGINT      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_favorites_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_favorites_user_id ON favorites (user_id);

CREATE TABLE ai_response_cache (
    id             BIGSERIAL PRIMARY KEY,
    cache_key      VARCHAR(64)  NOT NULL,
    request_type   VARCHAR(30)  NOT NULL,
    response_text  TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_ai_cache_key UNIQUE (cache_key),
    CONSTRAINT ck_ai_request_type CHECK (request_type IN ('EXPLANATION', 'TRANSLATION', 'SUMMARY', 'ALTERNATIVE'))
);

CREATE INDEX idx_ai_cache_key ON ai_response_cache (cache_key);

CREATE TABLE admins (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    permissions  TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_admins_user_id UNIQUE (user_id)
);
