import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.Input

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("com.github.johnrengelman.shadow") version "4.0.4"
    
    kotlin("jvm") version "1.3.21"

    // Apply the application plugin to add support for building a CLI application.
    application

    antlr
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    antlr("org.antlr:antlr4:4.8")
    
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    compile("com.yuvalshavit:antlr-denter:1.1")
}

application {
    mainClassName = "tasks.YAMLKt"
}

tasks.named("cleanTest") { 
    group = "verification"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-no-listener", "-visitor")
}

tasks.named("compileKotlin") {
    dependsOn(":generateGrammarSource")
}

task("print", JavaExec::class) {
    main = "tasks.PrintKt"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        lifecycle {
            events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }
        info.events = lifecycle.events
        info.exceptionFormat = lifecycle.exceptionFormat
    }

    val failedTests = mutableListOf<TestDescriptor>()
    val skippedTests = mutableListOf<TestDescriptor>()

    // See https://github.com/gradle/kotlin-dsl/issues/836
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            when (result.resultType) {
                TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
                TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
                else -> Unit
            }
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // root suite
                logger.lifecycle("----")
                logger.lifecycle("Test result: ${result.resultType}")
                logger.lifecycle(
                        "Test summary: ${result.testCount} tests, " +
                        "${result.successfulTestCount} succeeded, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped")
                failedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tFailed Tests")
                skippedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tSkipped Tests:")
            }
        }

        private infix fun List<TestDescriptor>.prefixedSummary(subject: String) {
                logger.lifecycle(subject)
                forEach { test -> logger.lifecycle("\t\t${test.displayName()}") }
        }

        private fun TestDescriptor.displayName() = parent?.let { "${it.name} - $name" } ?: "$name"
    })
}

sourceSets {
    create("benchmarks") {
        kotlin {
            compileClasspath += main.get().output + configurations.runtimeClasspath
            runtimeClasspath += output + compileClasspath
        }
    }
}

open class BenchmarkTask: JavaExec() {
    private val newargs = mutableMapOf<String, String>();
    private val arguments = listOf("policyDepth")

    @get:Input
    var policyDepth: String = ""

    @Option(option = "policyDepth", description = "Max depth of the main policy")
    public fun setPolicyDepth(policyDepth: String): Void? {
        this.policyDepth = policyDepth;
        newargs["policyDepth"] = policyDepth;
        return null
    }

    @TaskAction
    public fun fillArguments(): Void? {
        this.args(this.newargs)
        return null
    }

}

task("benchmarks", BenchmarkTask::class) {
    // sourceSets["benchmarks"].runtimeClasspath.forEach{println(it)}
    description = "Runs a set of random policies to make a benchmark"
    classpath = sourceSets["benchmarks"].runtimeClasspath + sourceSets["main"].runtimeClasspath
    main = "hapi.BenchmarkKt"
    fillArguments()
}
