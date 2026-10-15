-- Future-scope tables: allergens, diet preference, cart, reviews, recalls.

ALTER TABLE users
    ADD COLUMN diet_preference VARCHAR(20) NOT NULL DEFAULT 'NONE';

ALTER TABLE users
    ADD CONSTRAINT ck_users_diet CHECK (diet_preference IN (
        'NONE', 'VEGETARIAN', 'VEGAN', 'LOW_SUGAR', 'HIGH_PROTEIN'));

CREATE TABLE allergens (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(80) NOT NULL,
    CONSTRAINT uk_allergens_name UNIQUE (name)
);

CREATE TABLE ingredient_allergens (
    ingredient_id  BIGINT NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    allergen_id    BIGINT NOT NULL REFERENCES allergens (id) ON DELETE CASCADE,
    PRIMARY KEY (ingredient_id, allergen_id)
);

CREATE TABLE user_allergens (
    user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    allergen_id  BIGINT NOT NULL REFERENCES allergens (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, allergen_id)
);

INSERT INTO allergens (id, name) VALUES
    (1, 'peanuts'),
    (2, 'gluten'),
    (3, 'dairy'),
    (4, 'soy'),
    (5, 'eggs'),
    (6, 'tree nuts'),
    (7, 'fish'),
    (8, 'shellfish');

SELECT setval('allergens_id_seq', 8);

INSERT INTO ingredient_allergens (ingredient_id, allergen_id)
SELECT i.id, a.id
FROM ingredients i
CROSS JOIN allergens a
WHERE (a.name = 'gluten' AND (
        lower(i.name) LIKE '%wheat%'
        OR lower(i.name) LIKE '%maida%'
        OR lower(i.name) LIKE '%gluten%'
        OR lower(i.name) LIKE '%barley%'
        OR lower(i.name) LIKE '%rye%'))
   OR (a.name = 'dairy' AND (
        lower(i.name) LIKE '%milk%'
        OR lower(i.name) LIKE '%butter%'
        OR lower(i.name) LIKE '%whey%'
        OR lower(i.name) LIKE '%casein%'
        OR lower(i.name) LIKE '%cheese%'
        OR lower(i.name) LIKE '%dahi%'
        OR lower(i.name) LIKE '%yoghurt%'
        OR lower(i.name) LIKE '%yogurt%'))
   OR (a.name = 'peanuts' AND lower(i.name) LIKE '%peanut%')
   OR (a.name = 'soy' AND (lower(i.name) LIKE '%soy%' OR lower(i.name) LIKE '%soya%'))
   OR (a.name = 'eggs' AND lower(i.name) LIKE '%egg%')
   OR (a.name = 'tree nuts' AND (
        lower(i.name) LIKE '%almond%'
        OR lower(i.name) LIKE '%cashew%'
        OR lower(i.name) LIKE '%hazelnut%'
        OR lower(i.name) LIKE '%walnut%'))
   OR (a.name = 'fish' AND lower(i.name) LIKE '%fish%')
   OR (a.name = 'shellfish' AND (lower(i.name) LIKE '%shrimp%' OR lower(i.name) LIKE '%prawn%' OR lower(i.name) LIKE '%crab%'));

CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_carts_user UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity    INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_cart_items_product UNIQUE (cart_id, product_id),
    CONSTRAINT ck_cart_qty CHECK (quantity >= 1)
);

CREATE TABLE reviews (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating      INTEGER NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_reviews_user_product UNIQUE (user_id, product_id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_product ON reviews (product_id);

ALTER TABLE products
    ADD COLUMN recalled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE products
    ADD COLUMN recall_reason TEXT;
