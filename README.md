# Policy Evaluation DSL Service

Spring Boot 4.0.3 · Java 25 · Gradle 9.3.1 · DDD (Domain driven development) · Custom DSL

- Evaluates insurance policy eligibility for member details and computes premiums using a custom
  DSL 'rule engine' (Spring Expression Language).
- Default rules are set in [application.properties](./src/main/resources/application.properties)
    - They can be updated at runtime (without a server restart to simulate an external config setup
      in production)
- Policy specific rules can be set to override that.
- Uses H2 in-memory SQL database for demo purposes.

---

## Quick Start

```bash
# Run the application. It also runs the ./gradlew openApiGenerate task which generates the controllers 
# and dtos based on the: Openapi spec: ./src/resources/openapi.yaml 
./gradlew bootRun

# OpenAPI UI
open http://localhost:8080/swagger-ui.html
```

---

## Build & Test

```bash
# All unit + integration tests
./gradlew test
```

---

## DSL Reference

Three variables are available in all expressions:

| Variable         | Type     | Required?      | Example               |
|------------------|----------|----------------|-----------------------|
| `age`            | `int`    | Y              | `age >= 18`           |
| `gender`         | `String` | Y              | `gender == 'MALE'`    |
| `claimFreeYears` | `int`    | N (default: 0) | `claimFreeYears >= 2` |

**Eligibility DSL** – must return `boolean`

```
age >= 18 AND age <= 65 AND claimFreeYears >= 2
age >= 18 AND (gender == 'MALE' OR gender == 'FEMALE')
(age < 25 OR age > 60) AND claimFreeYears >= 1
```

**Premium DSL** – must return a number; rounded to 2 decimal places

```
500 + (age * 8) - (claimFreeYears * 20)
gender == 'FEMALE' ? 400 + (age * 6) : 420 + (age * 7)
age < 30 ? 250 : 500
```

---

## API

| Method   | Path                                   | Description        |
|----------|----------------------------------------|--------------------|
| `POST`   | `/api/v1/policies`                     | Create policy      |
| `GET`    | `/api/v1/policies`                     | List all           |
| `GET`    | `/api/v1/policies/{id}`                | Get by ID          |
| `PUT`    | `/api/v1/policies/{id}/update-premium` | Update premium     |
| `DELETE` | `/api/v1/policies/{id}`                | Delete             |
| `POST`   | `/api/v1/policies/{id}/evaluate`       | Evaluate applicant |

----------------------------------------------------------------------------------------------
**Create policy:**
POST /api/v1/policies

```json
{
  "name": "My Policy",
  "description": "Description",
  "eligibilityDsl": "age >= 18 AND age <= 60 AND claimFreeYears >= 1",
  "basePremium": 400,
  "variablePremium": "(age * 7) - (claimFreeYears * 15)",
  "currency": "EUR"
}
```

----------------------------------------------------------------------------------------------
**Update premium:**
POST /api/v1/policies/{id}/update-premium

```json
{
  "basePremium": 400,
  "variablePremium": "(age * 7) - (claimFreeYears * 15)",
  "currency": "EUR"
}
```

----------------------------------------------------------------------------------------------
**Evaluate request:**
POST /api/v1/policies/{id}/evaluate

```json
{
  "gender": "MALE",
  "age": 35,
  "claimFreeYears": 5
}
```

**Eligible response:**

```json
{
  "policyId": 123,
  "eligible": true,
  "premium": 640.00,
  "currency": "EUR",
  "reason": null
}
```

**Ineligible response:**

```json
{
  "policyId": 123,
  "eligible": false,
  "reason": "Not eligible"
}
```

---

## Architecture (DDD)

```
api/          – PolicyController, EvaluationController, GlobalExceptionHandler
application/
  dto/        – Request/Response records
  usecase/    – EvaluationUseCase, PolicyManagementUseCase
domain/
  model/      – Applicant, Policy, EligibilityResult, Gender
  repository/ – PolicyRepository
  service/    – PolicyEvaluationService, SpelDslEvaluator, DslEvaluator
infrastructure/
  persistence/– JPA entity, Spring Data repo, adapter, mapper
  config/     – PolicyProperties, DefaultPolicySeeder
```

---