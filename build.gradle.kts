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

    register<Exec>("kafkaup") {
        commandLine("docker-compose", "-p", "kafka", "-f", "docker/kafka-cluster.yml", "up")
    }

    register<Exec>("kafkadown") {
        commandLine("docker-compose", "-p", "kafka", "-f", "docker/kafka-cluster.yml", "down")
    }

    register<Exec>("createtopics") {
        commandLine("./scripts/create-topics.sh", "-b", "broker-1:19092", "source:1:1", "destination:1:1")
    }

    register<Exec>("deletetopics") {
        commandLine ("./scripts/delete-topics.sh", "-b", "broker-1:19092", "source", "destination")
    }

    register<GradleBuild>("recreatetopics") {
        tasks = listOf("deletetopics", "createtopics")
    }
}