package com.insurance.policy_evaluator_dsl.api;

import java.util.List;

import com.insurance.policy_evaluator_dsl.api.mapper.PolicyApiMapper;
import com.insurance.policy_evaluator_dsl.application.PolicyManagementService;
import com.insurance.policy_evaluator_dsl.domain.model.Policy;
import com.insurance.policy_evaluator_dsl.generated.dto.PolicyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for {@link PolicyController}.
 */
@ExtendWith(MockitoExtension.class)
class PolicyControllerTest {

    @Mock
    private PolicyManagementService policyManagementService;

    @Mock
    private PolicyApiMapper policyApiMapper;

    @InjectMocks
    private PolicyController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createPolicy_mapsRequestAndReturnsPolicy() throws Exception {
        // given
        PolicyResponse response = new PolicyResponse()
                .id(1L).name("My Policy").eligibilityDsl("age >= 18")
                .basePremium(400.0).variablePremiumDsl("age * 7").currency("EUR");

        when(policyApiMapper.toDomain(any())).thenReturn(new Policy());
        when(policyManagementService.create(any())).thenReturn(new Policy());
        when(policyApiMapper.toResponse(any())).thenReturn(response);

        // when and then
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "My Policy",
                                  "eligibilityDsl": "age >= 18",
                                  "basePremium": 400.0,
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Policy"))
                .andExpect(jsonPath("$.eligibilityDsl").value("age >= 18"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void listPolicies_returnsArrayOfPolicies() throws Exception {
        // given
        PolicyResponse p1 = new PolicyResponse().id(1L).name("Policy A").currency("EUR");
        PolicyResponse p2 = new PolicyResponse().id(2L).name("Policy B").currency("GBP");

        when(policyManagementService.findAll()).thenReturn(List.of(new Policy(), new Policy()));
        when(policyApiMapper.toResponse(any())).thenReturn(p1, p2);

        // when and then
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getPolicyById_returnsPolicyForGivenId() throws Exception {
        // given
        PolicyResponse response = new PolicyResponse()
                .id(42L).name("Named Policy").currency("EUR");

        when(policyManagementService.findById(42L)).thenReturn(new Policy());
        when(policyApiMapper.toResponse(any())).thenReturn(response);

        // when and then
        mockMvc.perform(get("/api/v1/policies/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Named Policy"));
    }

    @Test
    void deletePolicy_delegatesToServiceAndReturns200() throws Exception {
        // given not needed as its delegated

        // when and then
        mockMvc.perform(delete("/api/v1/policies/7"))
                .andExpect(status().isOk());
        verify(policyManagementService).delete(7L);
    }

    @Test
    void updatePremium_mapsRequestAndReturnsUpdatedPolicy() throws Exception {
        // given
        PolicyResponse response = new PolicyResponse()
                .id(3L).basePremium(500.0).variablePremiumDsl("age * 9").currency("GBP");

        when(policyManagementService.updatePremium(eq(3L), any(), any(), any()))
                .thenReturn(new Policy());
        when(policyApiMapper.toResponse(any())).thenReturn(response);

        // when and then
        mockMvc.perform(put("/api/v1/policies/3/update-premium")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "basePremium": 500.0,
                                  "variablePremiumDsl": "age * 9",
                                  "currency": "GBP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.basePremium").value(500.0))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }
}
