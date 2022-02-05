
plugins {
    idea

    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.6.10"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    val flinkVersion = "1.14.3"
    val scalaVersion = "2.11"

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

    // Testing
    listOf(
        "org.jetbrains.kotlin:kotlin-test",
        "org.jetbrains.kotlin:kotlin-test-junit"
    ).forEach { testImplementation(it) }
}

application {
    mainClass.set("apache.flink.kotlin.starter.FlinkApp")
}
