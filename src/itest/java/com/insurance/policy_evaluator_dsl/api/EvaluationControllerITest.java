package com.insurance.policy_evaluator_dsl.api;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.BaseITest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class EvaluationControllerITest extends BaseITest {

    @Test
    void evaluate_compoundDsl_applicantIneligible() {
        // Applicant is 30 (passes age >= 18) but has 0 claim-free years (fails claimFreeYears >= 2)
        long id = createPolicy("Strict Policy", "age >= 18 AND claimFreeYears >= 2", BigDecimal.valueOf(400.0), "age * 7", "EUR");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "gender": "MALE",
                          "age": 30,
                          "claimFreeYears": 0
                        }
                        """)
        .when()
                .post("/api/v1/policies/{id}/evaluate", id)
        .then()
                .statusCode(200)
                .body("eligible", equalTo(false))
                .body("reason", notNullValue())
                .body("premium", nullValue());
    }

    @Test
    void evaluate_genderConditionalPremiumDsl_withUpdatedPremiumDsl_isEligible() {
        // variablePremiumDsl branches on gender: FEMALE → age * 5, MALE → age * 7
        final long id = createPolicy("Gendered Policy", "age >= 18", BigDecimal.valueOf(400.0),
                "gender == 'FEMALE' ? age * 5 : age * 7", "EUR");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "gender": "FEMALE", "age": 30, "claimFreeYears": 0 }
                        """)
        .when()
                .post("/api/v1/policies/{id}/evaluate", id)
        .then()
                .statusCode(200)
                .body("eligible", equalTo(true))
                .body("premium", equalTo(550.0f)); //For age=30: FEMALE → 400 + 150 = 550

        // update variable rates
        // variablePremiumDsl branches on gender: MALE → age * 6, FEMALE → age * 7
        updatePremium(id, BigDecimal.valueOf(450),"gender == 'MALE' ? age * 6 : age * 7","EUR");
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "gender": "FEMALE", "age": 30, "claimFreeYears": 0 }
                        """)
                .when()
                .post("/api/v1/policies/{id}/evaluate", id)
                .then()
                .statusCode(200)
                .body("eligible", equalTo(true))
                .body("premium", equalTo(660.0f)); //For age=30: FEMALE → 450 + 210 = 660
    }
}
