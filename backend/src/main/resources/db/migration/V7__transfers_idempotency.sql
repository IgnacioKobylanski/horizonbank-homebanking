CREATE TABLE transfers (
                           id                     UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                           source_account_id      UUID           NOT NULL REFERENCES accounts (id),
                           destination_account_id UUID           NOT NULL REFERENCES accounts (id),
                           amount                 NUMERIC(19, 4) NOT NULL,
                           status                 VARCHAR(20)    NOT NULL,
                           concept                VARCHAR(140),
                           ledger_transaction_id  UUID           REFERENCES ledger_transactions (id),
                           created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
                           updated_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
                           CONSTRAINT ck_transfers_amount CHECK (amount > 0),
                           CONSTRAINT ck_transfers_distinct_accounts CHECK (source_account_id <> destination_account_id),
                           CONSTRAINT ck_transfers_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_transfers_source ON transfers (source_account_id, created_at DESC);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id, created_at DESC);

CREATE TABLE idempotency_keys (
                                  id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id         UUID         NOT NULL REFERENCES users (id),
                                  idempotency_key VARCHAR(100) NOT NULL,
                                  request_hash    VARCHAR(64)  NOT NULL,
                                  response_status SMALLINT,
                                  response_body   JSONB,
                                  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                                  CONSTRAINT uq_idempotency_user_key UNIQUE (user_id, idempotency_key)
);