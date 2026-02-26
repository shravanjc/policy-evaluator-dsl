package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;

public interface DslEvaluator {

    boolean evaluateEligibility(String dsl, Applicant applicant);

    BigDecimal evaluatePremium(String dsl, Applicant applicant);
}
