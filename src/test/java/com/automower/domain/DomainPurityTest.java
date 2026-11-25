package com.automower.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.automower", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainPurityTest {

    @ArchTest
    static final ArchRule domainShouldOnlyDependOnJavaLangAndUtil = classes()
            .that().resideInAPackage("com.automower.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("com.automower.domain..", "java.lang..", "java.util..");
}
