.SILENT:

default:
	docker -v
	java -version

run:
	FLINK_ENV="local" ./gradlew run

clean:
	./gradlew clean

kafka-start:
	docker-compose -p kafka -f docker/kafka-cluster.yml up

kafka-stop:
	docker-compose -p kafka -f docker/kafka-cluster.yml down

create-topics:
	./scripts/create-topics.sh -b "broker-1:19092" "source:1:1" "destination:1:1"

delete-topics:
	./scripts/delete-topics.sh -b "broker-1:19092" "source" "destination"

flink-start:
	docker-compose -p flink -f docker/flink-job-cluster.yml up -d

flink-stop:
	docker-compose -p flink -f docker/flink-job-cluster.yml down

start-producer:
	docker exec -i kafka-tools kafka-console-producer --broker-list broker-1:19092 --topic source --property "parse.key=true" --property "key.separator=:"
