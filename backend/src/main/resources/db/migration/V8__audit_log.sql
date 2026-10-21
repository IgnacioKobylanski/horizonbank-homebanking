CREATE TABLE audit_log (
                           id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           actor_user_id UUID        REFERENCES users (id),
                           action        VARCHAR(50) NOT NULL,
                           entity_type   VARCHAR(50) NOT NULL,
                           entity_id     VARCHAR(64),
                           payload       JSONB,
                           created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_actor ON audit_log (actor_user_id, created_at DESC);