Apache Flink Starter in Kotlin
========================
A starting point for an [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) project using Kotlin.

# (In Progress)

## Pre-Requisites

1. Docker on [Mac](https://download.docker.com/mac/stable/Docker.dmg)

1. [Gradle](https://gradle.org) - You have a few options here
    + If you're using Intellij, just make sure it's enabled.
    + Run `brew install gradle`

## Up & Running

Let's first clone the repo and fire up our system,

```
git clone git@github.com:aedenj/apache-flink-kotlin-starter.git ~/projects/apache-flink-kotlin-starter
cd ~/projects/apache-flink-kotlin-starter;make kafka-start 
```
Now you have a single node Kafka cluster with various admin tools to make life a little easier. See the [Kafka cluster repo](https://github.com/aedenj/kafka-cluster-starter) for its operating details.

## Running the App

The sample job in this repo reads from a topic named `source` and writes to a topic named `destination`.
There are a couple of ways of running this job depending on what you're trying to accomplish.

First, let's setup the kafka topics. Run `make create-topics`.

### Locally

For quick feedback it's easiest to run the job locally,

1. If you're using Intellij, use the usual methods.
1. On the command line run `make run`