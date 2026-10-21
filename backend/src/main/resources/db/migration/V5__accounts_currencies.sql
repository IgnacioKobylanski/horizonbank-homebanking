CREATE TABLE currencies (
                            code        CHAR(3)     PRIMARY KEY,
                            name        VARCHAR(50) NOT NULL,
                            minor_units SMALLINT    NOT NULL,
                            CONSTRAINT ck_currencies_minor_units CHECK (minor_units BETWEEN 0 AND 4)
);

INSERT INTO currencies (code, name, minor_units) VALUES
                                                     ('ARS', 'Peso argentino', 2),
                                                     ('USD', 'Dólar estadounidense', 2);

CREATE TABLE accounts (
                          id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          customer_id    UUID        REFERENCES customers (id),
                          account_number VARCHAR(22) NOT NULL,
                          alias          VARCHAR(30),
                          currency_code  CHAR(3)     NOT NULL REFERENCES currencies (code),
                          type           VARCHAR(20) NOT NULL,
                          status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          version        BIGINT      NOT NULL DEFAULT 0,
                          CONSTRAINT uq_accounts_number UNIQUE (account_number),
                          CONSTRAINT ck_accounts_type CHECK (type IN ('SAVINGS', 'CHECKING', 'SYSTEM')),
                          CONSTRAINT ck_accounts_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),
                          CONSTRAINT ck_accounts_owner CHECK (
                              (type = 'SYSTEM' AND customer_id IS NULL)
                                  OR (type <> 'SYSTEM' AND customer_id IS NOT NULL)
                              )
);

CREATE UNIQUE INDEX uq_accounts_alias_lower ON accounts (lower(alias)) WHERE alias IS NOT NULL;
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);