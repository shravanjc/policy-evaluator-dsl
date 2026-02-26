package com.insurance.policy_evaluator_dsl.api;

import com.insurance.policy_evaluator_dsl.api.mapper.PolicyApiMapper;
import com.insurance.policy_evaluator_dsl.application.EvaluationService;
import com.insurance.policy_evaluator_dsl.domain.model.Applicant;
import com.insurance.policy_evaluator_dsl.generated.dto.EvaluationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult.eligible;
import static com.insurance.policy_evaluator_dsl.domain.model.EligibilityResult.ineligible;
import static java.math.BigDecimal.valueOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for {@link EvaluationController}.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private PolicyApiMapper policyApiMapper;

    @InjectMocks
    private EvaluationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void evaluateApplicant_eligibleResult_returnsPremiumAndCurrency() throws Exception {
        //given
        EvaluationResponse response = new EvaluationResponse()
                .policyId(1L).eligible(true).premium(640.0).currency("EUR");

        when(policyApiMapper.toApplicant(any())).thenReturn(Applicant.of(35, com.insurance.policy_evaluator_dsl.domain.model.Gender.MALE, 5));
        when(evaluationService.evaluate(eq(1L), any())).thenReturn(eligible(valueOf(640), "EUR"));
        when(policyApiMapper.toEvaluationResponse(any(), eq(1L))).thenReturn(response);

        //when and then
        mockMvc.perform(post("/api/v1/policies/1/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gender": "MALE",
                                  "age": 35,
                                  "claimFreeYears": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value(1))
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.premium").value(640.0))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.reason").doesNotExist());
    }

    @Test
    void evaluateApplicant_ineligibleResult_returnsReasonWithoutPremium() throws Exception {
        // given
        EvaluationResponse response = new EvaluationResponse()
                .policyId(1L)
                .eligible(false)
                .reason("Applicant age below minimum threshold");

        when(policyApiMapper.toApplicant(any())).thenReturn(Applicant.of(16, com.insurance.policy_evaluator_dsl.domain.model.Gender.MALE, 0));
        when(evaluationService.evaluate(eq(1L), any())).thenReturn(ineligible("Applicant age below minimum threshold"));
        when(policyApiMapper.toEvaluationResponse(any(), eq(1L))).thenReturn(response);

        // when and then
        mockMvc.perform(post("/api/v1/policies/1/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gender": "MALE",
                                  "age": 16,
                                  "claimFreeYears": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.policyId").value(1))
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.reason").value("Applicant age below minimum threshold"))
                .andExpect(jsonPath("$.premium").doesNotExist())
                .andExpect(jsonPath("$.currency").doesNotExist());
    }
}
