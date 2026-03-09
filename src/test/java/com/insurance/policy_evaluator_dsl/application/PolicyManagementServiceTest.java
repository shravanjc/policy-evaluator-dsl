package com.insurance.policy_evaluator_dsl.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyDefaults;
import com.insurance.policy_evaluator_dsl.domain.service.PolicyEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyManagementServiceTest {

    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyEvaluationService policyEvaluationService;
    @Mock
    private PolicyDefaults policyDefaults;

    @InjectMocks
    private PolicyManagementService policyManagementService;

    @Test
    void create_validPolicy_savesAndReturnsPolicy() {
        // given
        Policy policy = Policy.builder().name("Test").eligibilityDsl("age >= 18")
                .basePremium(BigDecimal.valueOf(400)).variablePremiumDsl("age * 7").currency("EUR").build();
        when(policyRepository.save(policy)).thenReturn(policy);

        // when
        Policy result = policyManagementService.create(policy);

        // then
        assertThat(result).isEqualTo(policy);
        verify(policyRepository).save(policy);
    }

    @Test
    void create_validPolicy_withoutVariableDsl_savesAndReturnsPolicy() {
        // given
        Policy policy = Policy.builder()
                .name("Test")
                .eligibilityDsl("age >= 18")
                .basePremium(BigDecimal.valueOf(400))
                .currency("EUR")
                .build();
        when(policyRepository.save(policy)).thenReturn(policy);

        // when
        Policy result = policyManagementService.create(policy);

        // then
        assertThat(result).isEqualTo(policy);
        verify(policyRepository).save(policy);
    }

    @Test
    void create_withoutEligibilityDsl_appliesDefault() {
        Policy policy = Policy.builder().name("Test")
                .basePremium(BigDecimal.valueOf(100)).variablePremiumDsl("age * 5").currency("EUR").build();
        when(policyDefaults.eligibilityDsl()).thenReturn("age >= 18 AND age <= 65");
        when(policyRepository.save(policy)).thenReturn(policy);

        policyManagementService.create(policy);

        assertThat(policy.getEligibilityDsl()).isEqualTo("age >= 18 AND age <= 65");
        verify(policyRepository).save(policy);
    }

    @Test
    void create_withoutBasePremium_appliesDefault() {
        Policy policy = Policy.builder().name("Test")
                .eligibilityDsl("age >= 18").variablePremiumDsl("age * 5").currency("EUR").build();
        when(policyDefaults.basePremium()).thenReturn(BigDecimal.valueOf(100));
        when(policyRepository.save(policy)).thenReturn(policy);

        policyManagementService.create(policy);

        assertThat(policy.getBasePremium()).isEqualByComparingTo("100");
        verify(policyRepository).save(policy);
    }

    @Test
    void create_withoutVariablePremiumDsl_appliesDefault() {
        Policy policy = Policy.builder().name("Test")
                .eligibilityDsl("age >= 18").basePremium(BigDecimal.valueOf(100)).currency("EUR").build();
        when(policyDefaults.variablePremiumDsl()).thenReturn("age * 5");
        when(policyRepository.save(policy)).thenReturn(policy);

        policyManagementService.create(policy);

        assertThat(policy.getVariablePremiumDsl()).isEqualTo("age * 5");
        verify(policyRepository).save(policy);
    }

    @Test
    void create_invalidEligibilityDsl_throwsIllegalArgumentException() {
        // given
        Policy policy = Policy.builder()
                .eligibilityDsl("bla >= 18")
                .variablePremiumDsl("age * 7")
                .build();
        final String invalidDsl = "Invalid DSL";
        doThrow(new IllegalArgumentException(invalidDsl))
                .when(policyEvaluationService).validateDsl(policy.getEligibilityDsl());

        // when and then
        assertThatThrownBy(() -> policyManagementService.create(policy))
                .hasMessageContaining(invalidDsl);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void create_invalidPremiumDsl_throwsIllegalArgumentException() {
        // given
        Policy policy = Policy.builder()
                .eligibilityDsl("age >= 18")
                .variablePremiumDsl("bla * 7")
                .build();
        final String invalidDsl = "Invalid DSL";
        doNothing().when(policyEvaluationService).validateDsl(policy.getEligibilityDsl());
        doThrow(new IllegalArgumentException(invalidDsl))
                .when(policyEvaluationService).validateDsl(policy.getVariablePremiumDsl());

        // when and then
        assertThatThrownBy(() -> policyManagementService.create(policy))
                .hasMessageContaining(invalidDsl);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void findById_existingId_returnsPolicy() {
        // given
        Policy policy = Policy.builder().id(1L).name("Test").build();
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // when / then
        assertThat(policyManagementService.findById(1L)).isEqualTo(policy);
    }

    @Test
    void findById_nonExistingId_throwsNoSuchElementException() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> policyManagementService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findAll_returnsAllPolicies() {
        List<Policy> policies = List.of(Policy.builder().id(1L).build(), Policy.builder().id(2L).build());
        when(policyRepository.findAll()).thenReturn(policies);
        assertThat(policyManagementService.findAll()).hasSize(2);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        policyManagementService.delete(1L);
        verify(policyRepository).deleteById(1L);
    }

    @Test
    void updatePremium_existingId_updatesAndReturnsSavedPolicy() {
        // given
        Policy existing = Policy.builder().id(1L).basePremium(BigDecimal.valueOf(300)).variablePremiumDsl("age * 5").currency("EUR").build();
        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(policyRepository.save(existing)).thenReturn(existing);

        // when
        Policy result = policyManagementService.updatePremium(1L, BigDecimal.valueOf(400), "age * 7", "EUR");

        // then
        assertThat(result.getBasePremium()).isEqualByComparingTo("400");
        assertThat(result.getVariablePremiumDsl()).isEqualTo("age * 7");
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void updatePremium_invalidDsl_throwsIllegalArgumentException() {
        // given
        Policy existing = Policy.builder().id(1L).basePremium(BigDecimal.valueOf(300)).variablePremiumDsl("age * 5").currency("EUR").build();
        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));
        final String invalidPremiumDsl = "bla * 7";
        doThrow(new IllegalArgumentException(invalidPremiumDsl))
                .when(policyEvaluationService).validateDsl(anyString());

        // when and then
        assertThatThrownBy(() -> policyManagementService.updatePremium(1L, BigDecimal.valueOf(400), invalidPremiumDsl, "EUR"))
                .hasMessageContaining(invalidPremiumDsl);
    }
}
