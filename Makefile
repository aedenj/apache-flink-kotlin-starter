.SILENT:

NUM_TASK_MANAGERS := 1
NUM_TASK_SLOTS := 1
DEFAULT_PARALLELISM := $$(( $(NUM_TASK_MANAGERS) * $(NUM_TASK_SLOTS) ))

default:
	docker -v
	java -version

run:
	./gradlew run --args="--env local"

# Start Continuous Testing
test-t:
	./gradlew -t test

clean:
	./gradlew clean

shadowjar:
	./gradlew shadowjar

#------------------------------------
# Kafka
#------------------------------------
kafka-start:
	docker-compose -p kafka -f docker/kafka-cluster.yml up

kafka-stop:
	docker-compose -p kafka -f docker/kafka-cluster.yml down

create-topics:
	./scripts/create-topics.sh -b "broker-1:19092" $(topic)

delete-topics:
	./scripts/delete-topics.sh -b "broker-1:19092" $(topic)

create-default-topics:
	$(MAKE) create-topics topic='"source:1:1" "destination:1:1"'

delete-default-topics:
	$(MAKE) delete-topics topic='"source" "destination"'

recreate-default-topics:
	$(MAKE) delete-topics topic='"source" "destination"'
	$(MAKE) create-topics topic='"source:1:1" "destination:1:1"'

start-producer:
	docker exec -i kafka-tools kafka-console-producer --broker-list broker-1:19092 --topic $(topic) \
		--property "parse.key=true" --property "key.separator=:"

#-------------------------------------------
# ZooKeeper
#-------------------------------------------
zoonav-start:
	docker-compose -p zoonav -f docker/zoonav.yml up -d

zoonav-stop:
	docker-compose -p zoonav -f docker/zoonav.yml down

#------------------------------------
# Flink
#------------------------------------
flink-start: flink-stop shadowjar
	NUM_TASK_SLOTS=$(NUM_TASK_SLOTS) DEFAULT_PARALLELISM=$(DEFAULT_PARALLELISM) docker-compose -p flink -f docker/flink-job-cluster.yml up -d --scale taskmanager=$(NUM_TASK_MANAGERS)

flink-stop:
	# These environment variables are only needed to satisfy docker and don't do anything here
	NUM_TASK_SLOTS=0 DEFAULT_PARALLELISM=0 docker-compose -p flink -f docker/flink-job-cluster.yml down

#------------------------------------
# Monitoring
#------------------------------------
monitor-start:
	docker-compose -p monitoring -f docker/prometheus-grafana.yml up

monitor-stop:
	docker-compose -p monitoring -f docker/prometheus-grafana.yml down