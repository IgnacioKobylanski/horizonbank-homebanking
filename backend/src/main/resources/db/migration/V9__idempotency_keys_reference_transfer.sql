ALTER TABLE idempotency_keys
DROP COLUMN response_status,
    DROP COLUMN response_body,
    ADD COLUMN transfer_id UUID REFERENCES transfers (id);

CREATE UNIQUE INDEX uq_idempotency_keys_transfer
    ON idempotency_keys (transfer_id) WHERE transfer_id IS NOT NULL;