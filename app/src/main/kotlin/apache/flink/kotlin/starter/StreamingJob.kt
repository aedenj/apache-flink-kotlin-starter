package apache.flink.kotlin.starter

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object StreamingJob {
    fun run(config: JobConfig) {
        val env = StreamExecutionEnvironment.getExecutionEnvironment()

        val source = KafkaSource
            .builder<String>()
            .setBootstrapServers(config.brokers())
            .setTopics("source")
            .setValueOnlyDeserializer(SimpleStringSchema())
            .setProperties(config.consumer())
            .build()

        val sink = KafkaSink
            .builder<String>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<String>()
                    .setTopic("destination")
                    .setValueSerializationSchema(SimpleStringSchema())
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.NONE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val sinkTwo = KafkaSink
            .builder<String>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<String>()
                    .setTopic("destination-two")
                    .setValueSerializationSchema(SimpleStringSchema())
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.NONE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")

        stream
            .filter { it == "foo" }
            .sinkTo(sink).name("Destination Topic")

        stream
            .filter { it == "bar" }
            .sinkTo(sinkTwo).name("Destination Two Topic")

        env.execute("Kotlin Flink Starter")
    }
}

