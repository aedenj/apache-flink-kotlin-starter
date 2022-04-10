FROM flink:1.14.4-scala_2.11-java11

COPY ./app/build/libs/apache-flink-kotlin-starter-app-1.0.jar $FLINK_HOME/usrlib/
COPY ./conf/flink/log4j-console.properties $FLINK_HOME/conf/
