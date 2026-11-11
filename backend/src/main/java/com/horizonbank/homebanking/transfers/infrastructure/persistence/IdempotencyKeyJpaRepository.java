package com.horizonbank.homebanking.transfers.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, UUID> {

    @Modifying
    @Query(value = """
            insert into idempotency_keys (id, user_id, idempotency_key, request_hash)
            values (:id, :userId, :key, :hash)
            on conflict (user_id, idempotency_key) do nothing
            """, nativeQuery = true)
    int claim(@Param("id") UUID id, @Param("userId") UUID userId,
              @Param("key") String key, @Param("hash") String hash);

    Optional<IdempotencyKeyEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}