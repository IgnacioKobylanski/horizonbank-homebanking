CREATE TABLE app_info (
                          id SERIAL PRIMARY KEY,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);