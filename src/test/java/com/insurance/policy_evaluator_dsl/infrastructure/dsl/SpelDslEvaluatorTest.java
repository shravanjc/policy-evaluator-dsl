package com.insurance.policy_evaluator_dsl.infrastructure.dsl;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpelDslEvaluatorTest {

    private SpelDslEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SpelDslEvaluator();
    }

    @Test
    void evaluateEligibility_applicantMeetsCondition_returnsTrue() {
        Applicant applicant = Applicant.of(35, Gender.MALE, 5);
        assertThat(evaluator.evaluateEligibility("age >= 18 AND claimFreeYears >= 2", applicant)).isTrue();
    }

    @Test
    void evaluateEligibility_applicantDoesNotMeetCondition_returnsFalse() {
        Applicant applicant = Applicant.of(16, Gender.MALE, 0);
        assertThat(evaluator.evaluateEligibility("age >= 18", applicant)).isFalse();
    }

    @Test
    void evaluateEligibility_genderCondition_evaluatesCorrectly() {
        Applicant male = Applicant.of(30, Gender.MALE, 0);
        Applicant female = Applicant.of(30, Gender.FEMALE, 0);
        assertThat(evaluator.evaluateEligibility("gender == 'MALE'", male)).isTrue();
        assertThat(evaluator.evaluateEligibility("gender == 'MALE'", female)).isFalse();
    }

    @Test
    void evaluatePremium_fixedExpression_returnsAmount() {
        Applicant applicant = Applicant.of(35, Gender.MALE, 5);
        assertThat(evaluator.evaluatePremium("500 + (age * 8) - (claimFreeYears * 20)", applicant))
                .isEqualByComparingTo("680");
    }

    @Test
    void evaluatePremium_ternaryExpression_evaluatesCorrectlyForGender() {
        Applicant female = Applicant.of(30, Gender.FEMALE, 0);
        BigDecimal result = evaluator.evaluatePremium("gender == 'FEMALE' ? 400 + (age * 6) : 420 + (age * 7)", female);
        assertThat(result).isEqualByComparingTo("580");
    }

    @Test
    void evaluateEligibility_invalidExpression_throwsException() {
        Applicant applicant = Applicant.of(35, Gender.MALE, 0);
        assertThatThrownBy(() -> evaluator.evaluateEligibility("notAVariable > 5", applicant))
                .isInstanceOf(Exception.class);
    }
}
