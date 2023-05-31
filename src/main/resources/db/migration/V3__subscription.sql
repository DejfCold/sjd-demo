CREATE TABLE IF NOT EXISTS subscription (
    id UUID PRIMARY KEY,
    quotation_id UUID NOT NULL,
    start_date DATE NOT NULL,
    valid_until DATE NOT NULL
);

ALTER TABLE subscription
    ADD FOREIGN KEY (quotation_id) REFERENCES quotation (id);