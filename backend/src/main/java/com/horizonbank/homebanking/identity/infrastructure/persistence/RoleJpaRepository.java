package com.horizonbank.homebanking.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Short> {

    Optional<RoleEntity> findByName(String name);
}