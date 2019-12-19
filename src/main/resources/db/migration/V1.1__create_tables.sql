 --CREATE SCHEMA schema_zilch
--    AUTHORIZATION postgres;

--CREATE SEQUENCE IF NOT EXISTS schema_zilch.HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS currency
(
    id SERIAL PRIMARY KEY,
    name character varying(3) UNIQUE NOT NULL,
    --id character varying(3)  PRIMARY KEY,
    last_updated TIMESTAMP DEFAULT now(),
	last_updated_by VARCHAR DEFAULT 'admin'
);


CREATE TABLE IF NOT EXISTS  card
(
id SERIAL PRIMARY KEY,
user_id VARCHAR NOT NULL,
balance NUMERIC(15,2) DEFAULT 0 NOT NULL,
--currency_id	VARCHAR(3) REFERENCES currency (id) NOT NULL,
currency_id	integer REFERENCES currency (id) NOT NULL,
last_updated TIMESTAMP DEFAULT now(),
last_updated_by VARCHAR DEFAULT 'admin'
);


CREATE TABLE IF NOT EXISTS transaction_type
(
id VARCHAR PRIMARY KEY,
description TEXT,
last_updated TIMESTAMP DEFAULT now(),
last_updated_by VARCHAR DEFAULT 'admin'
);


CREATE TABLE IF NOT EXISTS transaction
(
id SERIAL PRIMARY KEY,
global_id VARCHAR UNIQUE NOT NULL,
type_id VARCHAR NOT NULL REFERENCES transaction_type (id),
amount NUMERIC(15,2) NOT NULL,
card_id integer REFERENCES card(id),
--currency_id	VARCHAR(3) REFERENCES currency (id) NOT NULL,
currency_id	integer REFERENCES currency (id) NOT NULL,
description TEXT,
last_updated TIMESTAMP DEFAULT now(),
last_updated_by VARCHAR DEFAULT 'admin'
);