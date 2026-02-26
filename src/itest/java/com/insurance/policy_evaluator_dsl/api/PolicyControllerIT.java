package com.insurance.policy_evaluator_dsl.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PolicyControllerIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void createPolicy_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Policy",
                                  "eligibilityDsl": "age >= 18",
                                  "basePremium": 400,
                                  "variablePremiumDsl": "age * 7",
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Policy"));
    }

    @Test
    void listPolicies_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPolicyById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/policies/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePolicy_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/policies/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void evaluateApplicant_eligibleApplicant_returns200WithPremium() throws Exception {
        // TODO: seed a policy first, then evaluate against its id
    }

    @Test
    void evaluateApplicant_ineligibleApplicant_returns200WithReason() throws Exception {
        // TODO: seed a policy first, then evaluate an applicant that doesn't meet the DSL
    }
}
