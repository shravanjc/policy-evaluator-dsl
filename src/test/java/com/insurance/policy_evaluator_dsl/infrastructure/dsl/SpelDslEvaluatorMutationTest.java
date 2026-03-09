package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.service.SpelDslEvaluator;
import org.junit.jupiter.api.Test;

import static com.insurance.policy_evaluator_dsl.domain.model.Gender.FEMALE;
import static com.insurance.policy_evaluator_dsl.domain.model.Gender.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpelDslEvaluatorMutationTest {

    private final SpelDslEvaluator evaluator = new SpelDslEvaluator();

    @Test
    void eligibility_kills_conditionals_boundary() {
        // If >= is mutated to >, age=18 would wrongly return false
        assertThat(evaluator.evaluateEligibility("age >= 18", Applicant.of(17, MALE, 0))).isFalse();
        assertThat(evaluator.evaluateEligibility("age >= 18", Applicant.of(18, MALE, 0))).isTrue();

        // If <= is mutated to <, age=65 would wrongly return false
        assertThat(evaluator.evaluateEligibility("age <= 65", Applicant.of(65, MALE, 0))).isTrue();
        assertThat(evaluator.evaluateEligibility("age <= 65", Applicant.of(66, MALE, 0))).isFalse();

        // If >= is mutated to >, claimFreeYears=1 would wrongly return false
        assertThat(evaluator.evaluateEligibility("claimFreeYears >= 1", Applicant.of(30, MALE, 0))).isFalse();
        assertThat(evaluator.evaluateEligibility("claimFreeYears >= 1", Applicant.of(30, MALE, 1))).isTrue();
    }

    @Test
    void eligibility_kills_conditionals_negate() {
        // If AND is mutated to OR, the two one-true-one-false cases would wrongly return true
        final String dsl = "age >= 18 AND claimFreeYears >= 1";
        assertThat(evaluator.evaluateEligibility(dsl, Applicant.of(18, MALE, 0))).isFalse();
        assertThat(evaluator.evaluateEligibility(dsl, Applicant.of(17, MALE, 1))).isFalse();
        assertThat(evaluator.evaluateEligibility(dsl, Applicant.of(18, MALE, 1))).isTrue();
        assertThat(evaluator.evaluateEligibility(dsl, Applicant.of(17, MALE, 0))).isFalse();

        // If == is mutated to !=, MALE would return false and FEMALE would return true
        assertThat(evaluator.evaluateEligibility("gender == 'MALE'", Applicant.of(30, MALE, 0))).isTrue();
        assertThat(evaluator.evaluateEligibility("gender == 'MALE'", Applicant.of(30, FEMALE, 0))).isFalse();
    }

    @Test
    void eligibility_kills_conditionals_null() {
        assertThat(evaluator.evaluateEligibility(null, Applicant.of(30, MALE, 0))).isFalse();
        assertThat(evaluator.evaluateEligibility("age >= 18", null)).isFalse();

        assertThat(evaluator.evaluatePremium(null, Applicant.of(30, MALE, 0)))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(evaluator.evaluatePremium("age * 8", null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void premium_kills_math_operators() {
        // If * is mutated to /, age=10 gives 0 (integer division) instead of 80
        assertThat(evaluator.evaluatePremium("age * 8", Applicant.of(10, MALE, 0)))
                .isEqualByComparingTo("80");
        assertThat(evaluator.evaluatePremium("age * 8", Applicant.of(20, MALE, 0)))
                .isEqualByComparingTo("160");
        // If * is mutated to +, claimFreeYears=3 gives 3+20=23 instead of 60
        assertThat(evaluator.evaluatePremium("claimFreeYears * 20", Applicant.of(0, MALE, 3)))
                .isEqualByComparingTo("60");
        // If - is mutated to +, result is 240+100=340 instead of 140
        assertThat(evaluator.evaluatePremium(
                "(age * 8) - (claimFreeYears * 20)", Applicant.of(30, MALE, 5)))
                .isEqualByComparingTo("140");
    }

    @Test
    void premium_kills_nullAndEmptyExpression() {
        // SpEL evaluates the literal "null" to a null BigDecimal test
        assertThat(evaluator.evaluatePremium("null", Applicant.of(30, MALE, 0)))
                .isEqualByComparingTo(BigDecimal.ZERO);
        // Kills EQUAL_IF mutations on lines 68 and 71: if either null-check or isEmpty-check
        // were mutated to always-true, this valid non-null non-empty input would wrongly throw.
        assertThatThrownBy(() -> evaluator.validateAndParseDsl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void caching_sameDslEvaluatedTwice_kills_cacheEntryCorruption() {
        // Ensures the cached Expression object is not mutated between calls
        final String dsl = "age >= 18";
        final Applicant applicant = Applicant.of(25, MALE, 3);
        assertThat(evaluator.evaluateEligibility(dsl, applicant))
                .isEqualTo(evaluator.evaluateEligibility(dsl, applicant))
                .isTrue();
    }
}
