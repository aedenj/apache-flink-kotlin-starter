package apache.flink.kotlin.starter

import apache.flink.kotlin.starter.serde.JsonAvroDeserializationSchema
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.formats.avro.AvroSerializationSchema
import org.apache.flink.formats.avro.typeutils.GenericRecordAvroTypeInfo
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object StreamingJob {
    fun run(config: JobConfig) {
        val job = StreamExecutionEnvironment.getExecutionEnvironment().apply {
            getConfig().disableForceKryo()
        }

        val schemaString =
            """
            {
                "type": "record",
                "name": "Event",
                "fields": [
                    { "name": "name", "type": "string" },
                    { "name": "device", "type": "string" },
                    { "name": "seconds", "type": "int", "default": 0 }
                ]
           };
           """.trimIndent()
        val schema = Schema.Parser().parse(schemaString)

        val source = KafkaSource
            .builder<GenericRecord>()
            .setBootstrapServers(config.brokers())
            .setTopics("source")
            .setDeserializer(JsonAvroDeserializationSchema(schemaString))
            .setProperties(config.consumer())
            .build()

        val sink = KafkaSink
            .builder<GenericRecord>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<GenericRecord>()
                    .setTopic("destination")
                    .setValueSerializationSchema(AvroSerializationSchema.forGeneric(schema))
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val sinkTwo = KafkaSink
            .builder<GenericRecord>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<GenericRecord>()
                    .setTopic("destination-two")
                    .setValueSerializationSchema(AvroSerializationSchema.forGeneric(schema))
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val stream = job.fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")

        stream
            .filter { it.get("name") == "typeA" }
            .setParallelism(2)
            .sinkTo(sink).name("Destination Topic")

        stream
            .filter { it.get("name") != "typeA"  }
            .setParallelism(2)
            .sinkTo(sinkTwo).name("Destination Two Topic")

        job.execute("Kotlin Flink Starter")
    }
}

