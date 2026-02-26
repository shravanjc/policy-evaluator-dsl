package com.insurance.policy_evaluator_dsl.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eligibilityDsl;

    @Column(nullable = false)
    private BigDecimal basePremium;

    @Column(columnDefinition = "TEXT")
    private String variablePremiumDsl;

    @Column(nullable = false, length = 3)
    private String currency;
}
