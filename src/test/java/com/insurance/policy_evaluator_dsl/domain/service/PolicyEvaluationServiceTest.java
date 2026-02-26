package com.insurance.policy_evaluator_dsl.domain.service;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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

    @ParameterizedTest
    @CsvFileSource(resources = "/policy_premium_details.csv", numLinesToSkip = 1, nullValues = "NULL")
    void evaluatePolicyTest(
            String scenarioName,
            boolean isEligible,
            Double basePremium,
            Double variablePremium,
            String currency,
            int age,
            Gender gender,
            int claimFreeYears,
            BigDecimal expectedPremium,
            String expectedReason) {

        Policy policy = Policy.builder()
                .basePremium(basePremium)
                .currency(currency)
                .build();
        Applicant applicant = Applicant.of(age, gender, claimFreeYears);

        when(dslEvaluator.evaluateEligibility(any(), any())).thenReturn(isEligible);
        lenient().when(dslEvaluator.evaluatePremium(any(), any())).thenReturn(BigDecimal.valueOf(variablePremium));

        EligibilityResult result = service.evaluate(policy, applicant);

        assertThat(result.eligible()).isEqualTo(isEligible);
        if (isEligible) {
            assertThat(result.currency()).isEqualTo(policy.getCurrency());
            assertThat(result.premium()).isEqualByComparingTo(expectedPremium);
        } else {
            assertThat(result.reason()).isEqualTo(expectedReason);
        }
    }
}
