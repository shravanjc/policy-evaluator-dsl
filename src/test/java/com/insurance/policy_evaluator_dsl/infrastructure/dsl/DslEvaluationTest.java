package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import com.insurance.policy_evaluator_dsl.domain.service.DslEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest(name = "eligibility DSL with unknown variable {0} throws IllegalArgumentException")
    @ValueSource(strings = {
            "salary > 50000",                    // fully unknown variable
            "age >= 18 AND salary > 50000",      // mix of known and unknown
            "foo == 'MALE'",                     // unknown string-typed variable
    })
    void evaluateEligibility_unknownVariable_throwsIllegalArgumentException(String invalidDsl) {
        Applicant applicant = Applicant.of(25, Gender.MALE, 3);

        assertThatThrownBy(() -> dslEvaluator.evaluateEligibility(invalidDsl, applicant))
                .hasMessageContaining("unknown variable");
    }

    @ParameterizedTest(name = "premium DSL with unknown variable {0} throws IllegalArgumentException")
    @ValueSource(strings = {
        "salary * 0.1",                      // fully unknown variable
        "age * multiplier",                   // mix of known and unknown
    })
    void evaluatePremium_unknownVariable_throwsIllegalArgumentException(String invalidDsl) {
        Applicant applicant = Applicant.of(25, Gender.MALE, 3);

        assertThatThrownBy(() -> dslEvaluator.evaluatePremium(invalidDsl, applicant))
                .hasMessageContaining("unknown variable");
    }
}
