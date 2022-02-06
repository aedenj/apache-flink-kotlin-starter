.SILENT:

default:
	docker -v
	java -version
	node -v

kafka-start:
	docker-compose -p kafka -f docker/kafka-cluster.yml up

kafka-stop:
	docker-compose -p kafka -f docker/kafka-cluster.yml down

create-topics:
	./scripts/create-topics.sh -b "broker-1:19092" "source:1:1" "destination:1:1"

delete-topics:
	./scripts/delete-topics.sh -b "broker-1:19092" "source" "destination"

flink-start:
	docker-compose -p flink -f docker/flink-cluster.yml up -d

flink-stop:
	docker-compose -p flink -f docker/flink-cluster.yml down

