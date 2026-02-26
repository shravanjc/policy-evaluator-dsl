package com.insurance.policy_evaluator_dsl.application;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.insurance.policy_evaluator_dsl.domain.model.Gender.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private PolicyManagementService policyManagementService;

    @Mock
    private PolicyEvaluationService policyEvaluationService;

    @InjectMocks
    private EvaluationService service;

    @Test
    void evaluateForExistingPolicy() {
        //given
        Policy policy = new Policy();
        Applicant applicant = Applicant.of(1, MALE, 0);
        EligibilityResult eligibilityResult = EligibilityResult.eligible(BigDecimal.valueOf(120.50), "EUR");

        when(policyManagementService.findById(any())).thenReturn(policy);
        when(policyEvaluationService.evaluate(policy, applicant)).thenReturn(eligibilityResult);

        //when
        EligibilityResult result = service.evaluate(1L, applicant);

        //then
        assertThat(result.eligible()).isEqualTo(eligibilityResult.eligible());
        assertThat(result.currency()).isEqualTo(eligibilityResult.currency());
    }

    @Test
    void evaluateForMissingPolicy() {
        //given
        Applicant applicant = Applicant.of(1, MALE, 0);
        when(policyManagementService.findById(any())).thenThrow(NoSuchElementException.class);

        //when and then
        assertThatThrownBy(() -> service.evaluate(99L, applicant))
                .isInstanceOf(NoSuchElementException.class);
    }
}
