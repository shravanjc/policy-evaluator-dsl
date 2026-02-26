package com.insurance.policy_evaluator_dsl.application;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult;
import com.insurance.policy_evaluator_dsl.domain.model.Gender;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyEvaluationService policyEvaluationService;

    @InjectMocks
    private EvaluationService service;

    @Test
    void evaluate_existingPolicy_returnsEvaluationResult() {
        // given
        Policy policy = Policy.builder().id(1L).eligibilityDsl("age >= 18")
                .basePremium(400.0).variablePremiumDsl("age * 7").currency("EUR").build();
        Applicant applicant = Applicant.of(35, Gender.MALE, 5);
        EligibilityResult expected = EligibilityResult.eligible(BigDecimal.valueOf(645), "EUR");

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyEvaluationService.evaluate(policy, applicant)).thenReturn(expected);

        // when
        EligibilityResult result = service.evaluate(1L, applicant);

        // then
        assertThat(result.eligible()).isTrue();
        assertThat(result.premium()).isEqualByComparingTo("645");
    }

    @Test
    void evaluate_nonExistingPolicy_throwsNoSuchElementException() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.evaluate(99L, Applicant.of(35, Gender.MALE, 0)))
                .isInstanceOf(NoSuchElementException.class);
    }
}
