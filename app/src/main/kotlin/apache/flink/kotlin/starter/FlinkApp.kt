@file:JvmName("FlinkApp")

package apache.flink.kotlin.starter

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.connector.kafka.source.KafkaSource

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment();

    val source = KafkaSource
        .builder<String>()
        .setBootstrapServers(JobConfig.brokers())
        .setTopics("source")
        .setValueOnlyDeserializer(SimpleStringSchema())
        .setProperties(JobConfig.consumer())
        .build()

    val sink = KafkaSink
        .builder<String>()
        .setBootstrapServers(JobConfig.brokers())
        .setRecordSerializer(
            KafkaRecordSerializationSchema.builder<String>()
                .setTopic("destination")
                .setValueSerializationSchema(SimpleStringSchema())
                .build()
        )
        .setDeliverGuarantee(DeliveryGuarantee.NONE)
        .setKafkaProducerConfig(JobConfig.producer())
        .build();

    env.fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")
        .sinkTo(sink)
        .name("Destination Topic");

    env.execute("Kotlin Flink Starter")
}
