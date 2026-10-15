-- Companion packs so each seeded category has more than one product.
-- Drop the broken Good Day image path (barcode and OFF image folder did not match).
-- Inserts are keyed by barcode so they do not collide with Open Food Facts rows.

UPDATE products
SET image_url = NULL
WHERE barcode = '8901063123456'
  AND image_url LIKE '%890/106/301/2345%';

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901058000001', 'Atta Noodles Masala', 'Maggi', 'instant_noodles', NULL, 'ADMIN_ADDED', 68
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901058000001');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901030000002', 'Soda Water 750ml', 'Kinley', 'soft_drink', NULL, 'ADMIN_ADDED', 92
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901030000002');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901491000003', 'Baked Potato Crisps', 'Lay''s', 'snacks', NULL, 'ADMIN_ADDED', 78
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901491000003');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901030860004', 'Natural Peanut Butter', 'Pintola', 'spreads', NULL, 'ADMIN_ADDED', 86
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901030860004');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901491000005', 'Muesli Fruit & Nut', 'Quaker', 'breakfast_cereal', NULL, 'ADMIN_ADDED', 88
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901491000005');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901262200006', 'Masti Dahi 400g', 'Amul', 'dairy', NULL, 'ADMIN_ADDED', 94
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901262200006');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901030860007', 'Coconut Water 200ml', 'Raw Pressery', 'juice', NULL, 'ADMIN_ADDED', 96
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901030860007');

INSERT INTO products (barcode, name, brand, category, image_url, source, health_score)
SELECT '8901063000008', 'Marie Gold Biscuits', 'Britannia', 'biscuits', NULL, 'ADMIN_ADDED', 71
WHERE NOT EXISTS (SELECT 1 FROM products WHERE barcode = '8901063000008');

INSERT INTO product_ingredients (product_id, ingredient_id, position_in_list)
SELECT p.id, v.ingredient_id, v.pos
FROM products p
JOIN (VALUES
    ('8901058000001', 1, 1),
    ('8901058000001', 8, 2),
    ('8901030000002', 13, 1),
    ('8901491000003', 14, 1),
    ('8901491000003', 15, 2),
    ('8901491000003', 8, 3),
    ('8901030860004', 3, 1),
    ('8901491000005', 9, 1),
    ('8901491000005', 3, 2),
    ('8901262200006', 17, 1),
    ('8901030860007', 16, 1),
    ('8901063000008', 19, 1),
    ('8901063000008', 3, 2),
    ('8901063000008', 8, 3)
) AS v(barcode, ingredient_id, pos) ON p.barcode = v.barcode
WHERE NOT EXISTS (
    SELECT 1 FROM product_ingredients pi
    WHERE pi.product_id = p.id AND pi.ingredient_id = v.ingredient_id
);

INSERT INTO nutrition_facts
    (product_id, calories, protein_g, carbs_g, fats_g, sugar_g, sodium_mg, fibre_g, serving_size)
SELECT p.id, v.calories, v.protein_g, v.carbs_g, v.fats_g, v.sugar_g, v.sodium_mg, v.fibre_g, v.serving_size
FROM products p
JOIN (VALUES
    ('8901058000001', 290.00, 8.00, 52.00, 4.50, 1.20, 640.00, 3.00, '70 g'),
    ('8901030000002', 0.00, 0.00, 0.00, 0.00, 0.00, 20.00, 0.00, '200 ml'),
    ('8901491000003', 460.00, 6.50, 60.00, 20.00, 0.40, 380.00, 4.50, '100 g'),
    ('8901030860004', 590.00, 25.00, 20.00, 49.00, 3.00, 8.00, 8.00, '32 g'),
    ('8901491000005', 370.00, 10.00, 64.00, 7.00, 12.00, 40.00, 8.00, '40 g'),
    ('8901262200006', 60.00, 3.50, 4.50, 3.20, 4.50, 45.00, 0.00, '100 g'),
    ('8901030860007', 19.00, 0.00, 4.70, 0.00, 4.50, 25.00, 0.00, '100 ml'),
    ('8901063000008', 440.00, 7.50, 76.00, 10.00, 12.00, 320.00, 2.80, '100 g')
) AS v(barcode, calories, protein_g, carbs_g, fats_g, sugar_g, sodium_mg, fibre_g, serving_size)
    ON p.barcode = v.barcode
WHERE NOT EXISTS (SELECT 1 FROM nutrition_facts n WHERE n.product_id = p.id);
