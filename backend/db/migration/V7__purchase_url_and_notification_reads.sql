-- Purchase links on packs, plus per-user notification read state.

ALTER TABLE products
    ADD COLUMN purchase_url TEXT;

UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=8901058851010' WHERE id = 1;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=5449000000996' WHERE id = 2;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=8901491101837' WHERE id = 3;
UPDATE products SET purchase_url = 'https://www.bigbasket.com/ps/?q=Nutella' WHERE id = 4;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=8901491000079' WHERE id = 5;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=8901262200127' WHERE id = 6;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=8901030865484' WHERE id = 7;
UPDATE products SET purchase_url = 'https://www.amazon.in/s?k=Britannia+Good+Day+Butter+Cookies' WHERE id = 8;

CREATE TABLE notification_reads (
    user_id   BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kind      VARCHAR(40) NOT NULL,
    ref_id    BIGINT NOT NULL,
    read_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, kind, ref_id)
);
