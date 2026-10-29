package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "currencies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrencyEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "minor_units", nullable = false)
    private Short minorUnits;
}