# Homebanking

Full-stack home banking app built as a **modular monolith**: Spring Boot 4 + PostgreSQL + Flyway on the backend, Angular frontend planned.

> Work in progress. The backend currently covers authentication, accounts and simulated deposits on top of a double-entry ledger. Transfers and the frontend are next.

## Stack

- Java 21, Spring Boot 4, Spring Security (stateless, JWT HS256)
- PostgreSQL 16 (Docker Compose), Flyway migrations
- Testcontainers for integration tests

## Architecture

One deployable, split into business modules that talk through public interfaces and never share JPA entities:

```
identity   customers, users, roles, registration, login, refresh tokens
accounts   accounts, balances, currencies (exposes AccountsApi)
ledger     double-entry ledger, deposits
shared     error handling (RFC 7807), security helpers
```

Each module follows `domain / application / infrastructure / web`.

## Running locally

Requirements: JDK 21 and Docker.

```bash
cp .env.example .env              # then set a password
docker compose up -d
export JWT_SECRET="$(openssl rand -base64 48)"
export POSTGRES_PASSWORD=<same value as in .env>
cd backend
./mvnw spring-boot:run
```

Tests also need Docker (they start a throwaway Postgres):

```bash
./mvnw test
```

## API (so far)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Create customer and user |
| POST | `/api/v1/auth/login` | Access token (15 min) and refresh token |
| POST | `/api/v1/auth/refresh` | Rotate refresh token |
| POST | `/api/v1/accounts` | Create an account |
| GET | `/api/v1/accounts` | List own accounts |
| GET | `/api/v1/accounts/{id}` | Account detail (404 if not owned) |
| POST | `/api/v1/accounts/{id}/deposits` | Simulated deposit |
| POST | `/api/v1/transfers` | Transfer between accounts (requires `Idempotency-Key` header) |

## Design decisions

- **Modular monolith over microservices.** Transfers need ACID transactions across accounts; a single process and database keep that simple and correct.
- **Double-entry ledger.** Balances come from immutable entries. `account_balances` is a materialized projection updated in the same transaction, and a reconciliation query checks both agree.
- **Concurrency.** Balance rows are locked with `SELECT ... FOR UPDATE` in a fixed order, and `@Version` acts as a second barrier. A test fires 200 concurrent deposits and fails if the lock is removed.
- **Refresh token rotation with reuse detection.** Only a SHA-256 hash of each token is stored. Reusing a revoked token revokes all of the user's tokens.
- **Money as `NUMERIC(19,4)` / `BigDecimal`**, never floating point.
- **Ownership checks return 404**, not 403, so account ids can't be probed.
- **Idempotent transfers.** The `Idempotency-Key` is claimed with `INSERT ... ON CONFLICT DO NOTHING` inside the same transaction as the transfer, so concurrent retries serialize and a failed transfer releases its key. Reusing a key with a different payload returns 422.

## Known limitations

- Registration reveals whether an email is already registered.
- Account numbers are random, with no check digits.

## Roadmap

- [X] Transfers with `Idempotency-Key`
- [ ] Audit log
- [ ] Angular frontend
- [ ] CI with GitHub Actions
- [ ] Architecture diagram and ADRs