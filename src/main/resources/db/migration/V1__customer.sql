CREATE TABLE IF NOT EXISTS customer
(
    id           UUID PRIMARY KEY,
    first_name   VARCHAR NOT NULL,
    last_name    VARCHAR NOT NULL,
    middle_name  VARCHAR,
    email        VARCHAR NOT NULL,
    phone_number VARCHAR NOT NULL,
    birth_date   DATE
);
