package com.insurance.policy_evaluator_dsl.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.domain.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyManagementServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyManagementService policyManagementService;

    @Test
    void create_validPolicy_savesAndReturnsPolicy() {
        // given
        Policy policy = Policy.builder().name("Test").eligibilityDsl("age >= 18")
                .basePremium(400.0).variablePremiumDsl("age * 7").currency("EUR").build();
        when(policyRepository.save(policy)).thenReturn(policy);

        // when
        Policy result = policyManagementService.create(policy);

        // then
        assertThat(result).isEqualTo(policy);
        verify(policyRepository).save(policy);
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
        when(policyRepository.existsById(1L)).thenReturn(true);
        policyManagementService.delete(1L);
        verify(policyRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNoSuchElementException() {
        when(policyRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> policyManagementService.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updatePremium_existingId_updatesAndReturnsSavedPolicy() {
        // given
        Policy existing = Policy.builder().id(1L).basePremium(300.0).variablePremiumDsl("age * 5").currency("EUR").build();
        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(policyRepository.save(existing)).thenReturn(existing);

        // when
        Policy result = policyManagementService.updatePremium(1L, 400.0, "age * 7", "EUR");

        // then
        assertThat(result.getBasePremium()).isEqualTo(400.0);
        assertThat(result.getVariablePremiumDsl()).isEqualTo("age * 7");
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }
}
