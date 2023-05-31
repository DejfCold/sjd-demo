CREATE TABLE IF NOT EXISTS quotation
(
    id                       UUID PRIMARY KEY,
    beginning_of_insurance   DATE   NOT NULL,

    /*
     The insured amount using the smallest subunit to prevent rounding errors and accommodate varying decimal precision.
     See: https://en.wikipedia.org/wiki/Denomination_(currency)#Subunit_and_super_unit
     */
    insured_amount           BIGINT NOT NULL,
    date_of_signing_mortgage DATE   NOT NULL,
    customer_id              UUID   NOT NULL
);

ALTER TABLE quotation
    ADD FOREIGN KEY (customer_id) REFERENCES customer (id);