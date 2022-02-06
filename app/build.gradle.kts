import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    kotlin("jvm")

    // https://imperceptiblethoughts.com/shadow/introduction/
    id("com.github.johnrengelman.shadow") version "7.1.2"

    // https://plugins.gradle.org/plugin/com.adarshr.test-logger
    id("com.adarshr.test-logger") version "3.0.0"

    // https://docs.gradle.org/current/userguide/application_plugin.html
    application
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val jarName = "flink-streaming-job.jar"
val entryPoint = "apache.flink.kotlin.starter.FlinkApp"

application {
    mainClass.set(entryPoint)
}

tasks {
    test {
        useJUnitPlatform()

        testlogger {
            theme = ThemeType.MOCHA
            slowThreshold = 5000
            showStandardStreams = true
            showFullStackTraces = false
            logLevel = LogLevel.QUIET
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    named<ShadowJar>("shadowJar") {
        archiveFileName.set(jarName)
        configurations.clear()
        configurations.add(flinkShadowJar)
        manifest.attributes.apply {
            putAll(mapOf(
                "Main-Class" to entryPoint,
                "Built-by" to System.getProperty("user.name"),
                "Build-Jdk" to System.getProperty("java.version")
            ))
        }
        isZip64 = true
        mergeServiceFiles()
        minimize()
    }
}

// NOTE: We cannot use "compileOnly" or "shadow" configurations since then we could not run code
// in the IDE or with "gradle run". We also cannot exclude transitive dependencies from the
// shadowJar yet (see https://github.com/johnrengelman/shadow/issues/159).
// -> Explicitly define the // libraries we want to be included in the "flinkShadowJar" configuration!
val flinkShadowJar: Configuration by configurations.creating {
    // always exclude these (also from transitive dependencies) since they are provided by Flink
    exclude(group = "org.apache.flink", module = "force-shading")
    exclude(group = "com.google.code.findbugs", module = "jsr305")
    exclude(group = "org.slf4j")
    exclude(group = "org.apache.logging.log4j")
}

configurations {
    all {
        // https://logging.apache.org/log4j/2.x/faq.html#exclusions
        // Good Explanation: https://stackoverflow.com/questions/42348755/class-path-contains-multiple-slf4j-bindings-error
        exclude(group = "log4j", module = "log4j")
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
}

dependencies {
    val flinkVersion = "1.14.3"
    val scalaVersion = "2.11"
    val log4jVersion = "2.17.0"
    val slf4jVersion = "1.7.32"
    val junitVersion = "5.8.2"
    val assertjVersion = "3.20.2"

    // Basics
    listOf(
        platform("org.jetbrains.kotlin:kotlin-bom"), // Align versions of all Kotlin components
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    ).forEach { implementation(it) }

    // Flink Core
    listOf(
        "org.apache.flink:flink-streaming-java_$scalaVersion:$flinkVersion",
        "org.apache.flink:flink-runtime-web_$scalaVersion:$flinkVersion"
    ).forEach { implementation(it) }

    // Logging
    listOf(
        "org.apache.logging.log4j:log4j-api:$log4jVersion",
        "org.apache.logging.log4j:log4j-core:$log4jVersion",
        "org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion",
        "org.slf4j:slf4j-log4j12:$slf4jVersion"
    ).forEach { implementation(it) }

    // Testing
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    listOf(
        "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
        "org.assertj:assertj-core:$assertjVersion"
    ).forEach { testImplementation(it) }
}