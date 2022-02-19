Apache Flink Starter in Kotlin (In Progress)
========================
A starting point for an [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) project using Kotlin.

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

First, let's setup the kafka topics. Run `make create-default-topics`.

### Locally

For quick feedback it's easiest to run the job locally,

1. If you're using Intellij, use the usual methods.
2. On the command line run `make run`


## Monitoring Flink

Both Prometheus and Grafana are available via Docker. In addition the Prometheus exporter has been enabled in the 
local cluster. In order to see this all in action,

1. Ensure Kafka is running. If it isn't run, `make kafka-start`. I prefer to see the output. However, if you don't, you can add the `-d` option to the `kafka-start` command in the [Makefile](https://github.com/aedenj/apache-flink-kotlin-starter/blob/main/Makefile#L21).
2. If the topics for the default Flink Job don't already exist run `make create-topics`
3. Now let's start up Grafana and Prometheus by running `make monitor-start`
4. Start the Flink Cluster by executing `make flink-start`
5. Navigate to the [example dashboard](http://localhost:9003/d/veLveEOiz/flink?orgId=1&refresh=5s&from=now-3h&to=now)

You may not see results immediately. Wait a minute or so and you should start seeing results.

Here are a list links,

| Description                                                         | Link |
|---------------------------------------------------------------------|--------|
| Prometheus - Useful for exploring the raw measuresments of a metric | http://localhost:9090 |
| Grafana - Home of all the dashboard                                 | http://localhost:9003 |
| Job Manager Prometheus Exporter                                     | http://localhost:9249 |
| Task Manager Prometheus Exporter                                    | http://localhost:9250 |
