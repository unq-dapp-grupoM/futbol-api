package com.dapp.futbol_api.arquitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

@AnalyzeClasses(packages = "com.dapp.futbol_api", importOptions = { ImportOption.DoNotIncludeTests.class })
public class ArchitectureTest {

    private static final String CONTROLLERS_PACKAGE = "..webservice..";
    private static final String SERVICES_PACKAGE = "..service..";
    private static final String REPOSITORIES_PACKAGE = "..repositories..";
    private static final String MODEL_PACKAGE = "..model..";
    private static final String CONFIG_PACKAGE = "..config..";
    private static final String SECURITY_PACKAGE = "..security..";

    @ArchTest
    public static final ArchRule layeredArchitectureRule =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controllers").definedBy(CONTROLLERS_PACKAGE)
                    .layer("Security").definedBy(SECURITY_PACKAGE)
                    .layer("Services").definedBy(SERVICES_PACKAGE)
                    .layer("Repositories").definedBy(REPOSITORIES_PACKAGE)

                    .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Security")
                    .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services");

    @ArchTest
    public static final ArchRule controllerNamingAndAnnotationRule =
            classes()
                    .that().resideInAPackage(CONTROLLERS_PACKAGE)
                    .should().haveSimpleNameEndingWith("Controller")
                    .andShould().beAnnotatedWith(RestController.class)
                    .as("Controllers should end with 'Controller' and be annotated with @RestController");

    @ArchTest
    public static final ArchRule serviceNamingAndAnnotationRule =
            classes()
                    .that().resideInAPackage(SERVICES_PACKAGE) 
                    .and().areNotAnonymousClasses()
                    .should().haveSimpleNameEndingWith("Service")
                    .andShould().beAnnotatedWith(Service.class)
                    .as("Services should end with 'Service' and be annotated with @Service");

    @ArchTest
    public static final ArchRule repositoryNamingAndAnnotationRule =
            classes()
                    .that().resideInAPackage(REPOSITORIES_PACKAGE)
                    .should().haveSimpleNameEndingWith("Repository")
                    .andShould().beAnnotatedWith(Repository.class)
                    .orShould().beInterfaces() // JPA Repositories are often interfaces
                    .as("Repositories should end with 'Repository' and be interfaces or be annotated with @Repository");

    @ArchTest
    public static final ArchRule servicesShouldOnlyBeAccessedByControllersOrOtherServices =
            classes()
                    .that().resideInAPackage(SERVICES_PACKAGE)
                    .should().onlyBeAccessed().byAnyPackage(CONTROLLERS_PACKAGE, SERVICES_PACKAGE, CONFIG_PACKAGE, SECURITY_PACKAGE)
                    .as("Services should only be accessed by controllers, other services, config, or security");

    @ArchTest
    public static final ArchRule repositoriesShouldOnlyBeAccessedByServices =
            classes()
                    .that().resideInAPackage(REPOSITORIES_PACKAGE)
                    .should().onlyBeAccessed().byClassesThat().resideInAPackage(SERVICES_PACKAGE)
                    .as("Repositories should only be accessed by services");

    @ArchTest
    public static final ArchRule domainModelsShouldNotDependOnOtherLayers =
            noClasses().that().resideInAPackage(MODEL_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage(CONTROLLERS_PACKAGE, SERVICES_PACKAGE, REPOSITORIES_PACKAGE);

    // ### General Coding Rules ###

    @ArchTest
    public static final ArchRule noFieldInjection =
            noFields()
                    .should().beAnnotatedWith(Autowired.class)
                    .as("Field injection is discouraged, use constructor injection instead");

    @ArchTest
    public static final ArchRule noStandardStreamsUsage =
            noClasses()
                    .should(ACCESS_STANDARD_STREAMS)
                    .as("Use a logger instead of writing to standard streams");

    // ### JPA/Entity Rules ###

    @ArchTest
    public static final ArchRule entitiesMustResideInModelPackage =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage(MODEL_PACKAGE)
                    .as("Entities must reside in the '..model..' package");

    // ### Controller Rules ###

    @ArchTest
    public static final ArchRule publicMethodsInControllersShouldReturnResponseEntity =
            methods()
                    .that().arePublic()
                    .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().haveRawReturnType(ResponseEntity.class)
                    .as("Public methods in controllers should return ResponseEntity");

    // ### Service Rules ###

    @ArchTest
    public static final ArchRule servicesShouldNotDependOnWebLayer =
            noClasses()
                    .that().resideInAPackage(SERVICES_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..")
                    .as("Services should not depend on the web layer (jakarta.servlet)");

    // ### More General Coding Rules ###

    @ArchTest
    public static final ArchRule noJodaTime = NO_CLASSES_SHOULD_USE_JODATIME;

    @ArchTest
    public static final ArchRule useSlf4jForLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
}
