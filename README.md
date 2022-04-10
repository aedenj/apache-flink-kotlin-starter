Apache Flink Starter in Kotlin 
========================
A starting point for an [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) project using Kotlin.

<!-- toc-begin -->
* [Pre-requisites](#pre-requisites)
* [Up & Running](#up--running)
* [Running The Job](#running-the-job)
    * [Standalone Mode](#standalone-mode)
    * [Using a Job Cluster](#using-a-job-cluster)
* [Observing the Job in Action](#observing-the-job-in-action)
* [Additional Containers](#additional-containers)
* [Useful Commands](#useful-commands)
* [Useful References](#useful-references)
* Additional Topics - Visit Wiki 
  * [Deploying with Flink Kubernetes Operator](https://github.com/aedenj/apache-flink-kotlin-starter/wiki/Deploying-with-the-Flink-Kubernetes-Operator)
  * [Profiling Flink with JDK Mission Control](https://github.com/aedenj/apache-flink-kotlin-starter/wiki/Profiling-Flink-with-JDK-Mission-Control)
<!-- toc-end -->
  
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

### Standalone Mode 

For quick feedback it's easiest to run the job locally,

1. If you're using Intellij, use the usual methods.
2. On the command line run `make run`

### Using a Job Cluster

1. Run `make flink-start`. 
2. Verify the cluster is running by [navigating to the UI](http://localhost:8081/)

This will run the job with the cluster defined in [`flink-job-cluster.yml`](https://github.com/aedenj/apache-flink-kotlin-starter/blob/main/docker/flink-job-cluster.yml). 
By default the cluster runs one task manager and one slot however this can be changed. The `make flink-start` 
command accepts two parameters `NUM_TASK_MANAGERS` and `NUM_TASK_SLOTS`. For example, one can run

```
make flink-start NUM_TASK_MANAGERS=2 NUM_TASK_SLOTS=3 
```

and this will result in a job cluster running two task managers with three slots each and a default parallelism of six.

In order to stop the job run `make flink-stop`

### Observing the Job in Action

After starting the job with one of the methods above, let's observe it reading and writing a message from one topic to another.

1. Start the job using one of the methods above.
2. If Kafka Cat isn't installed, run `brew install kcat`
3. To populate the source topic run `head -n 10 nyctaxi-dataset/yellowcab-trips-sample.json | kcat -b localhost -t source -T -P -l`
4. Navigate to the [Kafka UI](http://localhost:8001/) and view the messages both the `source` and `destination` topics. 

You should see the same messages in both topics.

## Monitoring Flink

Both Prometheus and Grafana are available via Docker. In addition the Prometheus exporter has been enabled in the 
local cluster. In order to see this all in action,

1. Ensure Kafka is running. If it isn't run, `make kafka-start`. I prefer to see the output. However, if you don't, 
   you can add the `-d` option to the `kafka-start` command in the [Makefile](https://github.com/aedenj/apache-flink-kotlin-starter/blob/main/Makefile#L21).
2. If the topics for the default Flink Job don't already exist run `make create-topics`
3. Now let's start up Grafana and Prometheus by running `make monitor-start`
4. Start the Flink Cluster by executing `make flink-start`
5. Navigate to the [example dashboard](http://localhost:9003/d/wKbnD5Gnk/flink?orgId=1)

You may not see results immediately. Wait a minute or so and you should start seeing results. Lastly, if you run
the Flink cluster with more than one task manager that node will be automatically discovered the discovery
service setup in prometheus.

Here are a list links of useful links,

| Description                                                         | Link                                                |
|---------------------------------------------------------------------|-----------------------------------------------------|
| Prometheus - Useful for exploring the raw measuresments of a metric | http://localhost:9090                               |
| Grafana - Home of all the dashboard                                 | http://localhost:9003                               |
| Job Manager Prometheus Exporter                                     | http://localhost:9249                               |
| Task Manager Prometheus Exporter                                    | Run `docker ps`. Identify containers mapped to 9249 |

## Additional Containers

- **ZooNavigator** - A web-based UI for ZooKeeper that you can start and stop using `make zoonav-start` and 
  `make zoonav-stop`. Navigate to [http://localhost:9001/](http://localhost:9001/) for access. 
 
## Useful Commands

| Service       | Command(s)                                                                                                                                                      |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Kafka Cluster | `make kafka-start`, `make kafka-stop`                                                                                                                           |
 | Kafka        | `make create-default-topics`, `make delete-default-topics`, `make create-topics topic='"topicA:2:3" "topicB:5:1"'`, `make delete-topics topic='"topicA" "topicB"'` |
| Flink Cluster | `make flink-start`, ` make flink-start NUM_TASK_SLOTS=2 NUM_TASK_MANAGERS=3`, `make flink-stop`                                                                 |
| Monitoring | `make monitor-start`, `make monitor-stop`                                                                                                                       |
| ZooNavigator | `make zoonav-start`, `make zoonav-stop`                                                                                                                         |


## Useful References

- [kcat](https://github.com/edenhill/kcat#running-in-docker) (Formly KafkaCat) - a generic non-JVM producer and consumer
  for Apache Kafka >=0.8, think of it as a netcat for Kafka. In addition the [Confluent Docs](https://docs.confluent.io/4.0.1/app-development/kafkacat-usage.html#) 
  has helpful examples.
- [Kafka UI](https://github.com/provectus/kafka-ui) - A feature rich UI on par with the Confluent UI to monitor and 
  manage Kafka clusters.
