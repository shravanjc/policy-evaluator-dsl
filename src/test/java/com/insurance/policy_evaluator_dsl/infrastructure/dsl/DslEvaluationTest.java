package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import com.insurance.policy_evaluator_dsl.domain.service.DslEvaluator;
import com.insurance.policy_evaluator_dsl.infrastructure.dsl.SpelDslEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;

class DslEvaluationTest {

    private final DslEvaluator dslEvaluator = new SpelDslEvaluator();

    @ParameterizedTest
    @CsvFileSource(resources = "/evaluation_test_details.csv", numLinesToSkip = 1, nullValues = "NULL")
    void evaluatePolicyTest(
            String eligibilityDsl,
            String variablePremiumDsl,
            Integer age,
            Gender gender,
            Integer claimFreeYears,
            boolean expectedEligible,
            BigDecimal expectedPremium) {

        Applicant applicant = Applicant.of(age, gender, claimFreeYears);

        final boolean isEligible = dslEvaluator.evaluateEligibility(eligibilityDsl, applicant);
        assertThat(isEligible).isEqualTo(expectedEligible);

        final BigDecimal premium = dslEvaluator.evaluatePremium(variablePremiumDsl, applicant);
        assertThat(premium).isEqualTo(expectedPremium);
    }
}
