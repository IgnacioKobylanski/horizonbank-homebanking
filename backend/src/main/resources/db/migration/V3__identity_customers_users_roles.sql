CREATE TABLE customers (
                           id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                           first_name      VARCHAR(100) NOT NULL,
                           last_name       VARCHAR(100) NOT NULL,
                           document_type   VARCHAR(10)  NOT NULL,
                           document_number VARCHAR(20)  NOT NULL,
                           phone           VARCHAR(30),
                           birth_date      DATE         NOT NULL,
                           created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                           updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                           version         BIGINT       NOT NULL DEFAULT 0,
                           CONSTRAINT uq_customers_document UNIQUE (document_type, document_number),
                           CONSTRAINT ck_customers_document_type CHECK (document_type IN ('DNI', 'PASSPORT'))
);

CREATE TABLE users (
                       id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       customer_id   UUID         NOT NULL REFERENCES customers (id),
                       email         VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       version       BIGINT       NOT NULL DEFAULT 0,
                       CONSTRAINT uq_users_customer UNIQUE (customer_id),
                       CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'BLOCKED'))
);

CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email));

CREATE TABLE roles (
                       id   SMALLINT    PRIMARY KEY,
                       name VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (id, name) VALUES (1, 'CUSTOMER'), (2, 'ADMIN');

CREATE TABLE user_roles (
                            user_id UUID     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role_id SMALLINT NOT NULL REFERENCES roles (id),
                            PRIMARY KEY (user_id, role_id)
);