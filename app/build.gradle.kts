import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    kotlin("jvm")

    // https://imperceptiblethoughts.com/shadow/introduction/
    id("com.github.johnrengelman.shadow") version "8.1.1"

    // https://plugins.gradle.org/plugin/com.adarshr.test-logger
    id("com.adarshr.test-logger") version "4.0.0"

    // https://docs.gradle.org/current/userguide/application_plugin.html
    application
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val entryPoint = "apache.flink.kotlin.starter.FlinkApp"

application {
    mainClass.set(entryPoint)
    version = "1.0"
    applicationDefaultJvmArgs = listOf(
        "-Dlog4j2.configurationFile=../conf/flink/log4j-local.properties"
        ,"-Dlog.file=./data/flink/logs" // log path for the flink webui when running dockerless
    )
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

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

    shadowJar {
        archiveFileName.set("${project.parent?.name}-${project.name}-${project.version}.jar")
        archiveClassifier.set("")
        manifest.attributes.apply {
            putAll(mapOf(
                "Main-Class" to entryPoint,
                "Project-Name" to rootProject.name,
                "Build-OsName" to System.getProperty("os.name"),
                "Build-OsVersion" to System.getProperty("os.version"),
                "Build-Jdk" to System.getProperty("java.version"),
                "Built-by" to System.getProperty("user.name")
            ))
        }

        configurations.clear()
        configurations.add(flinkShadowJar)
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
    val flinkVersion = "1.17.1"
    val log4jVersion = "2.17.0"
    val slf4jVersion = "1.7.32"
    val junitVersion = "5.9.0"
    val assertjVersion = "3.20.2"
    val kafkaJunitVersion = "3.2.2"
    val typesafeVersion = "1.4.2"

    // Basics
    listOf(
        platform("org.jetbrains.kotlin:kotlin-bom"), // Align versions of all Kotlin components
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    ).forEach { implementation(it) }

    // Flink Core
    listOf(
        "org.apache.flink:flink-streaming-java:$flinkVersion",
        "org.apache.flink:flink-runtime-web:$flinkVersion"
    ).forEach { implementation(it) }

    // Connectors & Formats
    listOf(
        "org.apache.flink:flink-connector-kafka:$flinkVersion"
    ).forEach { implementation(it) }

    // Logging
    listOf(
        "org.apache.logging.log4j:log4j-api:$log4jVersion",
        "org.apache.logging.log4j:log4j-core:$log4jVersion",
        "org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion",
        "org.slf4j:slf4j-log4j12:$slf4jVersion"
    ).forEach { implementation(it) }

    // Supplementary
    implementation("com.typesafe:config:$typesafeVersion")

    // Testing
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    listOf(
        "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
        "net.mguenther.kafka:kafka-junit:$kafkaJunitVersion",
        "org.assertj:assertj-core:$assertjVersion"
    ).forEach { testImplementation(it) }

    //Add required dependencies to the Flink shadow jar
    listOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.apache.flink:flink-connector-kafka:$flinkVersion",
        "com.typesafe:config:$typesafeVersion"
    ).forEach { flinkShadowJar(it) }
}