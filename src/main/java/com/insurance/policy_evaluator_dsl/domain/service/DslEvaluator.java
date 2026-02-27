package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;

public interface DslEvaluator {

    boolean evaluateEligibility(final String dsl, final Applicant applicant);

    BigDecimal evaluatePremium(final String dsl, final Applicant applicant);

    void validateAndParseDsl(final String dsl);
}
