package com.insurance.policy_evaluator_dsl.api;

import java.math.BigDecimal;

import com.insurance.policy_evaluator_dsl.BaseITest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class PolicyControllerITest extends BaseITest {

    @Test
    void listPolicies_whenNoPoliciesExist_returnsEmptyArray() {
        given()
                .when()
                .get("/api/v1/policies")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(0));
    }

    @Test
    void updatePremium_isReflectedOnSubsequentGet() {
        long id = createPolicy("My Policy", "age >= 18", BigDecimal.valueOf(400.0), "age * 7", "EUR");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "basePremium": 750.0,
                          "variablePremiumDsl": "age * 12",
                          "currency": "GBP"
                        }
                        """)
                .when()
                .put("/api/v1/policies/{id}/update-premium", id)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/api/v1/policies/{id}", id)
                .then()
                .statusCode(200)
                .body("basePremium", equalTo(750.0f))
                .body("variablePremiumDsl", equalTo("age * 12"))
                .body("currency", equalTo("GBP"));
    }
}
