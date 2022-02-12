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

        env.apply {
            fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")
            .sinkTo(sink)
            .name("Destination Topic")
        }.execute("Kotlin Flink Starter")
    }
}

