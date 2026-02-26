package com.insurance.policy_evaluator_dsl.api;

import java.util.NoSuchElementException;

import com.insurance.policy_evaluator_dsl.api.mapper.PolicyApiMapper;
import com.insurance.policy_evaluator_dsl.application.PolicyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private PolicyManagementService policyManagementService;
    @Mock
    private PolicyApiMapper policyApiMapper;

    @InjectMocks
    private PolicyController policyController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(policyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void noSuchElementException_returns404WithMessage() throws Exception {
        when(policyManagementService.findById(99L))
                .thenThrow(new NoSuchElementException("Policy not found"));

        mockMvc.perform(get("/api/v1/policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Policy not found"));
    }

    @Test
    void illegalArgumentException_returns400WithMessage() throws Exception {
        when(policyManagementService.updatePremium(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("foo is not okay"));

        mockMvc.perform(put("/api/v1/policies/1/update-premium")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "basePremium": 400,
                                  "currency": "EUR",
                                  "variablePremiumDsl": "(age * 7) - (claimFreeYears * 15)"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("foo is not okay"));
    }

    @Test
    void methodArgumentNotValid_returns400WithFieldErrors() throws Exception {
        // missing required fields in CreatePolicyRequest triggers @Valid failure
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("name")));
    }

    @Test
    void unexpectedException_returns500WithGenericMessage_doesNotLeakInternals() throws Exception {
        when(policyManagementService.findAll())
                .thenThrow(new RuntimeException("DB connection timed out"));

        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
