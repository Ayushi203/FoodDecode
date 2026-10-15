-- Seed: 2 users, 8 packaged products, ingredients, nutrition, sample history/favorites.
-- Test login (after Phase 3 auth):  user@fooddecode.dev / Password@123
--                                   admin@fooddecode.dev / Password@123
-- Password hashes are BCrypt of Password@123.

-- Health-score formula used to precompute products.health_score (same formula
-- will live in Java in the product module):
--   start at 100
--   −8 per harmful ingredient (capped at −40)
--   extra −15 if any ingredient harm_category = trans_fat
--   extra −10 if any ingredient harm_category = artificial_sweetener
--   −20 if sugar_g > 25, else −10 if sugar_g > 15
--   −15 if sodium_mg > 800, else −8 if sodium_mg > 400
--   −5  if calories > 300
--   +5  if fibre_g >= 3
--   +5  if protein_g >= 8
--   clamp to 0–100

INSERT INTO users (id, full_name, email, password_hash, role, is_verified, preferred_language)
VALUES
    (1, 'Nikisha Gupta', 'user@fooddecode.dev',
        '$2b$10$ztnp8g17bkoroVBDrDKcXO4ICSLbUKKNQ/bToX9I3GW0IyYjrP4gu',
        'USER', TRUE, 'en'),
    (2, 'FoodDecode Admin', 'admin@fooddecode.dev',
        '$2b$10$ztnp8g17bkoroVBDrDKcXO4ICSLbUKKNQ/bToX9I3GW0IyYjrP4gu',
        'ADMIN', TRUE, 'en');

SELECT setval('users_id_seq', 2);

INSERT INTO admins (user_id, permissions)
VALUES (2, '{"products":"write","ingredients":"write","analytics":"read"}');

INSERT INTO ingredients (id, name, is_harmful, harm_category) VALUES
    (1,  'Wheat flour',                          FALSE, NULL),
    (2,  'Palm oil',                             TRUE,  'trans_fat'),
    (3,  'Sugar',                                FALSE, NULL),
    (4,  'Monosodium glutamate (MSG)',           TRUE,  'additive'),
    (5,  'TBHQ (tert-Butylhydroquinone)',        TRUE,  'preservative'),
    (6,  'Aspartame',                            TRUE,  'artificial_sweetener'),
    (7,  'High fructose corn syrup',             TRUE,  'added_sugar'),
    (8,  'Salt',                                 FALSE, NULL),
    (9,  'Rolled oats',                          FALSE, NULL),
    (10, 'Cocoa powder',                         FALSE, NULL),
    (11, 'Milk solids',                          FALSE, NULL),
    (12, 'Phosphoric acid',                      TRUE,  'preservative'),
    (13, 'Carbonated water',                     FALSE, NULL),
    (14, 'Potato',                               FALSE, NULL),
    (15, 'Sunflower oil',                        FALSE, NULL),
    (16, 'Orange juice from concentrate',        FALSE, NULL),
    (17, 'Toned milk',                           FALSE, NULL),
    (18, 'Partially hydrogenated vegetable oil', TRUE,  'trans_fat'),
    (19, 'Refined wheat flour (maida)',          FALSE, NULL),
    (20, 'Butter',                               FALSE, NULL);

SELECT setval('ingredients_id_seq', 20);

INSERT INTO ingredient_explanations (ingredient_id, language_code, simple_explanation, ai_generated) VALUES
    (2,  'en', 'Palm oil is a cheap cooking fat. Repeated processing can create trans fats, which are linked to heart disease. Prefer products that use sunflower or olive oil instead.', FALSE),
    (4,  'en', 'MSG is a flavour booster. It is generally recognised as safe in small amounts, but some people get headaches or flushing after eating a lot of it.', FALSE),
    (5,  'en', 'TBHQ is a synthetic preservative that stops oils from going rancid. Food regulators allow only tiny amounts; frequent intake from packaged snacks is best limited.', FALSE),
    (6,  'en', 'Aspartame is an artificial sweetener used instead of sugar. It has almost no calories, but some people prefer to avoid it and choose unsweetened drinks.', FALSE),
    (7,  'en', 'High fructose corn syrup is a liquid sweetener. It adds a lot of sugar quickly and is linked to weight gain when drinks like soda are consumed often.', FALSE),
    (12, 'en', 'Phosphoric acid gives cola its sharp taste. Regular high intake from soft drinks is associated with lower bone mineral density.', FALSE),
    (18, 'en', 'Partially hydrogenated oils are the main source of industrial trans fat. Trans fat raises bad cholesterol (LDL) and lowers good cholesterol (HDL).', FALSE);

INSERT INTO products (id, barcode, name, brand, category, image_url, source, health_score) VALUES
    (1, '8901058851010', '2-Minute Noodles Masala', 'Maggi', 'instant_noodles',
        'https://images.openfoodfacts.org/images/products/890/105/885/1010/front_en.jpg', 'ADMIN_ADDED', 41),
    (2, '5449000000996', 'Coca-Cola Classic 330ml', 'Coca-Cola', 'soft_drink',
        'https://images.openfoodfacts.org/images/products/544/900/000/0996/front_en.jpg', 'ADMIN_ADDED', 64),
    (3, '8901491101837', 'Classic Salted Potato Chips', 'Lay''s', 'snacks',
        'https://images.openfoodfacts.org/images/products/890/149/110/1837/front_en.jpg', 'ADMIN_ADDED', 92),
    (4, '3017620422003', 'Nutella Hazelnut Spread', 'Ferrero', 'spreads',
        'https://images.openfoodfacts.org/images/products/301/762/042/2003/front_en.jpg', 'ADMIN_ADDED', 52),
    (5, '8901491000079', 'Rolled Oats', 'Quaker', 'breakfast_cereal',
        'https://images.openfoodfacts.org/images/products/890/149/100/0079/front_en.jpg', 'ADMIN_ADDED', 100),
    (6, '8901262200127', 'Taaza Toned Milk 500ml', 'Amul', 'dairy',
        'https://images.openfoodfacts.org/images/products/890/126/220/0127/front_en.jpg', 'ADMIN_ADDED', 100),
    (7, '8901030865484', '100% Orange Juice', 'Tropicana', 'juice',
        'https://images.openfoodfacts.org/images/products/890/103/086/5484/front_en.jpg', 'ADMIN_ADDED', 100),
    (8, '8901063123456', 'Good Day Butter Cookies', 'Britannia', 'biscuits',
        'https://images.openfoodfacts.org/images/products/890/106/301/2345/front_en.jpg', 'ADMIN_ADDED', 52);

SELECT setval('products_id_seq', 8);

-- product_ingredients (position_in_list = order on pack)
INSERT INTO product_ingredients (product_id, ingredient_id, position_in_list) VALUES
    -- Maggi
    (1, 1, 1), (1, 2, 2), (1, 8, 3), (1, 4, 4), (1, 5, 5),
    -- Coke
    (2, 13, 1), (2, 7, 2), (2, 12, 3),
    -- Lay's
    (3, 14, 1), (3, 15, 2), (3, 8, 3),
    -- Nutella
    (4, 3, 1), (4, 2, 2), (4, 11, 3), (4, 10, 4),
    -- Oats
    (5, 9, 1),
    -- Amul milk
    (6, 17, 1),
    -- Tropicana
    (7, 16, 1), (7, 3, 2),
    -- Good Day
    (8, 19, 1), (8, 3, 2), (8, 20, 3), (8, 18, 4);

INSERT INTO nutrition_facts
    (product_id, calories, protein_g, carbs_g, fats_g, sugar_g, sodium_mg, fibre_g, serving_size)
VALUES
    (1, 320.00, 7.00, 47.00, 11.00, 1.50,  920.00, 1.20, '70 g (1 pack)'),
    (2, 139.00, 0.00, 35.00,  0.00, 35.00,   15.00, 0.00, '330 ml'),
    (3, 540.00, 7.00, 53.00, 33.00,  0.50,  520.00, 4.00, '100 g'),
    (4, 539.00, 6.30, 57.50, 30.90, 56.30,   40.00, 0.00, '100 g'),
    (5, 379.00, 13.20, 67.70, 6.50,  0.80,    2.00, 10.00, '40 g'),
    (6, 58.00,  3.30,  4.70,  3.00,  4.70,   50.00, 0.00, '100 ml'),
    (7, 48.00,  0.70, 11.00,  0.00, 10.00,    5.00, 0.30, '100 ml'),
    (8, 502.00, 6.50, 67.00, 23.00, 28.00,  280.00, 1.80, '100 g');

INSERT INTO scan_history (user_id, product_id, scan_method, scanned_at) VALUES
    (1, 1, 'BARCODE', NOW() - INTERVAL '2 days'),
    (1, 5, 'SEARCH',  NOW() - INTERVAL '1 day'),
    (1, 2, 'BARCODE', NOW() - INTERVAL '3 hours');

INSERT INTO favorites (user_id, product_id) VALUES
    (1, 5),
    (1, 6);
