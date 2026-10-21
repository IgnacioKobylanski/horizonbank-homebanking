CREATE TABLE ledger_transactions (
                                     id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                     type        VARCHAR(20)  NOT NULL,
                                     description VARCHAR(255),
                                     created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                                     CONSTRAINT ck_ledger_transactions_type CHECK (type IN ('DEPOSIT', 'TRANSFER'))
);

CREATE TABLE ledger_entries (
                                id             BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                transaction_id UUID           NOT NULL REFERENCES ledger_transactions (id),
                                account_id     UUID           NOT NULL REFERENCES accounts (id),
                                direction      VARCHAR(6)     NOT NULL,
                                amount         NUMERIC(19, 4) NOT NULL,
                                created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
                                CONSTRAINT ck_ledger_entries_direction CHECK (direction IN ('DEBIT', 'CREDIT')),
                                CONSTRAINT ck_ledger_entries_amount CHECK (amount > 0)
);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries (transaction_id);
CREATE INDEX idx_ledger_entries_account_created ON ledger_entries (account_id, created_at DESC);

CREATE TABLE account_balances (
                                  account_id UUID           PRIMARY KEY REFERENCES accounts (id),
                                  balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
                                  updated_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
                                  version    BIGINT         NOT NULL DEFAULT 0
);

INSERT INTO accounts (id, account_number, alias, currency_code, type) VALUES
                                                                          ('00000000-0000-0000-0000-000000000001', '0000000000000000000001', 'SYSTEM.CASH.ARS', 'ARS', 'SYSTEM'),
                                                                          ('00000000-0000-0000-0000-000000000002', '0000000000000000000002', 'SYSTEM.CASH.USD', 'USD', 'SYSTEM');

INSERT INTO account_balances (account_id) VALUES
                                              ('00000000-0000-0000-0000-000000000001'),
                                              ('00000000-0000-0000-0000-000000000002');