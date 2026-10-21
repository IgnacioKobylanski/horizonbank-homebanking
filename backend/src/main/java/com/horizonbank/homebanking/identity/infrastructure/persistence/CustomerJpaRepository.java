package com.horizonbank.homebanking.identity.infrastructure.persistence;

import com.horizonbank.homebanking.identity.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}