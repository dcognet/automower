package com.automower;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.automower", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.automower.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.automower.infrastructure..");
}
