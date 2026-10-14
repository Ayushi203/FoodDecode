-- Run this while connected to the maintenance database "postgres" (not fooddecode).
-- pgAdmin: Query Tool on server → postgres → Execute.
CREATE DATABASE fooddecode
    WITH ENCODING = 'UTF8'
    TEMPLATE = template0;
