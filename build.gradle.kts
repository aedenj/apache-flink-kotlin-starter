plugins{
    base

    idea

    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.6.10"
}

allprojects {
    repositories {
        // Use Maven Central for resolving dependencies.
        mavenCentral()
    }

    tasks {
        withType<Wrapper> {
            gradleVersion = "7.3.3"
        }
    }
}

tasks {

    register<Exec>("startjob") {
        commandLine("docker-compose", "-p", "flink", "-f", "docker/flink-job-cluster.yml", "up", "-d")
    }

    register<Exec>("stopjob") {
        commandLine("docker-compose", "-p", "flink", "-f", "docker/flink-job-cluster.yml", "down")
    }
}