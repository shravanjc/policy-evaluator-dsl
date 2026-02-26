package com.insurance.policy_evaluator_dsl.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.insurance.policy_evaluator_dsl")
class ArchitectureRulesTest {

    private static final String API_PACKAGE = "com.insurance.policy_evaluator_dsl.api";

    @ArchTest
    static final ArchRule controllers_must_implement_a_generated_api_interface =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should(implementAtLeastOneInterfaceFromPackage(API_PACKAGE));

    @ArchTest
    static final ArchRule layer_dependencies_are_respected =
            layeredArchitecture().consideringOnlyDependenciesInAnyPackage(
                            "com.insurance.policy_evaluator_dsl..")
                    .layer("api").definedBy("..api..")
                    .layer("application").definedBy("..application..")
                    .layer("domain").definedBy("..domain..")
                    .layer("infrastructure").definedBy("..infrastructure..")
                    .whereLayer("api").mayNotBeAccessedByAnyLayer()
                    .whereLayer("application").mayOnlyBeAccessedByLayers("api")
                    .whereLayer("domain").mayOnlyBeAccessedByLayers("api", "application", "infrastructure")
                    .whereLayer("infrastructure").mayNotBeAccessedByAnyLayer();

    private static ArchCondition<JavaClass> implementAtLeastOneInterfaceFromPackage(String pkg) {
        return new ArchCondition<>("implement at least one interface from package " + pkg) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                boolean implementsOne = item.getInterfaces().stream()
                        .anyMatch(i -> i.toErasure().getPackageName().startsWith(pkg));
                if (!implementsOne) {
                    events.add(SimpleConditionEvent.violated(item,
                            item.getSimpleName() + " does not implement any API interface from " + pkg));
                }
            }
        };
    }
}
