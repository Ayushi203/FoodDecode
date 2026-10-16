-- Recategorise Open Food Facts rows that landed in unusable tags,
-- and add extra peers so each packed-food type has real higher-score swaps.

UPDATE products
SET category = 'seasoning'
WHERE lower(name) LIKE '%masala magic%'
   OR lower(name) LIKE '%masala-ae-magic%'
   OR lower(name) LIKE '%masala ae magic%';

UPDATE products
SET category = 'instant_noodles'
WHERE (lower(name) LIKE '%noodle%' OR lower(name) LIKE '%ramen%' OR lower(name) LIKE '%yippee%')
  AND category IS DISTINCT FROM 'seasoning';

UPDATE products
SET category = 'biscuits'
WHERE lower(name) LIKE '%biscuit%'
   OR lower(name) LIKE '%cookie%'
   OR lower(name) LIKE '%parle-g%'
   OR lower(name) LIKE '%parle g%'
   OR lower(name) LIKE '%good day%'
   OR lower(name) LIKE '%marie%';

UPDATE products
SET category = 'snacks'
WHERE lower(name) LIKE '%chip%'
   OR lower(name) LIKE '%crisp%'
   OR lower(name) LIKE '%kurkure%'
   OR lower(name) LIKE '%namkeen%';

UPDATE products
SET category = 'breakfast_cereal'
WHERE lower(name) LIKE '%oat%'
   OR lower(name) LIKE '%muesli%'
   OR lower(name) LIKE '%corn flake%';

UPDATE products
SET category = 'soft_drink'
WHERE lower(name) LIKE '%cola%'
   OR lower(name) LIKE '%pepsi%'
   OR lower(name) LIKE '%sprite%'
   OR lower(name) LIKE '%soda water%';

UPDATE products
SET category = 'juice'
WHERE lower(name) LIKE '%juice%'
   OR lower(name) LIKE '%coconut water%';

UPDATE products
SET category = 'dairy'
WHERE lower(name) LIKE '%milk%'
   OR lower(name) LIKE '%dahi%'
   OR lower(name) LIKE '%curd%'
   OR lower(name) LIKE '%yoghurt%'
   OR lower(name) LIKE '%yogurt%';

UPDATE products
SET category = 'spreads'
WHERE lower(name) LIKE '%nutella%'
   OR lower(name) LIKE '%peanut butter%'
   OR lower(name) LIKE '%spread%';

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901058899001', 'Masala-ae-Magic', 'Maggi', 'seasoning', NULL, 'ADMIN_ADDED', 62
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901058899001');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901207011111', 'Kitchen King Masala', 'Everest', 'seasoning', NULL, 'ADMIN_ADDED', 84
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901207011111');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901552022222', 'Kitchen King Masala', 'MDH', 'seasoning', NULL, 'ADMIN_ADDED', 80
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901552022222');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901725111111', 'Yippee Magic Masala Noodles', 'Sunfeast', 'instant_noodles', NULL, 'ADMIN_ADDED', 48
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901725111111');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901233222222', 'Top Ramen Curry', 'Nissin', 'instant_noodles', NULL, 'ADMIN_ADDED', 38
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901233222222');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901058333333', 'Nutri-licious Atta Noodles', 'Maggi', 'instant_noodles', NULL, 'ADMIN_ADDED', 74
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901058333333');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901063444444', 'NutriChoice Digestive', 'Britannia', 'biscuits', NULL, 'ADMIN_ADDED', 78
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901063444444');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901491555555', 'Kurkure Masala Munch', 'Kurkure', 'snacks', NULL, 'ADMIN_ADDED', 44
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901491555555');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901491666666', 'Multigrain Chips', 'Too Yumm', 'snacks', NULL, 'ADMIN_ADDED', 82
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901491666666');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '5449000777777', 'Coca-Cola Zero Sugar 330ml', 'Coca-Cola', 'soft_drink', NULL, 'ADMIN_ADDED', 86
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '5449000777777');

INSERT INTO nutrition_facts
    (product_id, calories, protein_g, carbs_g, fats_g, sugar_g, sodium_mg, fibre_g, serving_size)
SELECT p.id, v.calories, v.protein_g, v.carbs_g, v.fats_g, v.sugar_g, v.sodium_mg, v.fibre_g, v.serving_size
FROM products p
JOIN (VALUES
    ('8901058899001', 280.00, 8.00, 40.00, 8.00, 6.00, 1800.00, 4.00, '5 g'),
    ('8901207011111', 320.00, 10.00, 45.00, 10.00, 4.00,  900.00, 8.00, '5 g'),
    ('8901552022222', 330.00, 9.00, 46.00, 11.00, 5.00,  950.00, 7.00, '5 g'),
    ('8901725111111', 450.00, 8.00, 62.00, 16.00, 3.00,  980.00, 2.00, '70 g'),
    ('8901233222222', 460.00, 8.00, 63.00, 17.00, 3.50, 1100.00, 1.80, '70 g'),
    ('8901058333333', 360.00, 10.00, 58.00, 8.00, 2.00,  720.00, 5.00, '70 g'),
    ('8901063444444', 430.00, 8.00, 68.00, 12.00, 10.00, 380.00, 6.50, '100 g'),
    ('8901491555555', 540.00, 6.00, 52.00, 32.00, 2.00,  850.00, 3.00, '100 g'),
    ('8901491666666', 470.00, 8.00, 58.00, 20.00, 1.00,  420.00, 7.00, '100 g'),
    ('5449000777777',   1.00, 0.00,  0.00,  0.00, 0.00,   10.00, 0.00, '330 ml')
) AS v(barcode, calories, protein_g, carbs_g, fats_g, sugar_g, sodium_mg, fibre_g, serving_size)
    ON p.barcode = v.barcode
WHERE NOT EXISTS (SELECT 1 FROM nutrition_facts n WHERE n.product_id = p.id);

INSERT INTO product_ingredients (product_id, ingredient_id, position_in_list)
SELECT p.id, v.ingredient_id, v.pos
FROM products p
JOIN (VALUES
    ('8901058899001', 8, 1), ('8901058899001', 4, 2),
    ('8901207011111', 8, 1),
    ('8901552022222', 8, 1),
    ('8901725111111', 1, 1), ('8901725111111', 2, 2), ('8901725111111', 8, 3),
    ('8901233222222', 1, 1), ('8901233222222', 2, 2), ('8901233222222', 8, 3),
    ('8901058333333', 1, 1), ('8901058333333', 8, 2),
    ('8901063444444', 19, 1), ('8901063444444', 9, 2),
    ('8901491555555', 14, 1), ('8901491555555', 2, 2), ('8901491555555', 8, 3),
    ('8901491666666', 14, 1), ('8901491666666', 15, 2),
    ('5449000777777', 13, 1), ('5449000777777', 6, 2)
) AS v(barcode, ingredient_id, pos) ON p.barcode = v.barcode
WHERE NOT EXISTS (
    SELECT 1 FROM product_ingredients pi
    WHERE pi.product_id = p.id AND pi.ingredient_id = v.ingredient_id
);
