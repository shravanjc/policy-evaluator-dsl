package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyEvaluationServiceTest {

    @Mock
    private DslEvaluator dslEvaluator;

    private PolicyEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new PolicyEvaluationService(dslEvaluator);
    }

    @Test
    void evaluate_eligibleApplicant_returnsEligibleResult() {
        // given
        Policy policy = Policy.builder()
                .eligibilityDsl("age >= 18")
                .basePremium(400.0)
                .variablePremium("age * 7")
                .currency("EUR")
                .build();
        Applicant applicant = Applicant.of(35, Gender.MALE, 5);

        when(dslEvaluator.evaluateEligibility(policy.getEligibilityDsl(), applicant)).thenReturn(true);
        when(dslEvaluator.evaluatePremium(policy.getVariablePremium(), applicant)).thenReturn(BigDecimal.valueOf(245));

        // when
        EligibilityResult result = service.evaluate(policy, applicant);

        // then
        assertThat(result.eligible()).isTrue();
        assertThat(result.premium()).isEqualByComparingTo("645.00");
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void evaluate_ineligibleApplicant_returnsIneligibleResult() {
        // given
        Policy policy = Policy.builder()
                .eligibilityDsl("age >= 18")
                .basePremium(400.0)
                .variablePremium("age * 7")
                .currency("EUR")
                .build();
        Applicant applicant = Applicant.of(16, Gender.MALE, 0);

        when(dslEvaluator.evaluateEligibility(policy.getEligibilityDsl(), applicant)).thenReturn(false);

        // when
        EligibilityResult result = service.evaluate(policy, applicant);

        // then
        assertThat(result.eligible()).isFalse();
        assertThat(result.premium()).isNull();
        assertThat(result.reason()).isNotBlank();
    }

    @Test
    void evaluate_eligibleApplicant_premiumIsBasePlusVariableRoundedToTwoDecimalPlaces() {
        // given
        Policy policy = Policy.builder()
                .eligibilityDsl("age >= 18")
                .basePremium(400.0)
                .variablePremium("age * 7")
                .currency("EUR")
                .build();
        Applicant applicant = Applicant.of(35, Gender.MALE, 5);

        when(dslEvaluator.evaluateEligibility(policy.getEligibilityDsl(), applicant)).thenReturn(true);
        when(dslEvaluator.evaluatePremium(policy.getVariablePremium(), applicant)).thenReturn(new BigDecimal("245.555"));

        // when
        EligibilityResult result = service.evaluate(policy, applicant);

        // then
        assertThat(result.premium().scale()).isEqualTo(2);
        assertThat(result.premium()).isEqualByComparingTo("645.56");
    }
}
