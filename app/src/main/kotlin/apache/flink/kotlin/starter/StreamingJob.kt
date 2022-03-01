package apache.flink.kotlin.starter

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.formats.avro.AvroSerializationSchema
import org.apache.flink.formats.avro.typeutils.GenericRecordAvroTypeInfo
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema

object StreamingJob {
    fun run(config: JobConfig) {
        val env = StreamExecutionEnvironment.getExecutionEnvironment().apply {
            getConfig().disableForceKryo()
        }

        val schemaString =
            """
            {
                "type": "record",
                "name": "Event",
                "fields": [
                    { "name": "name", "type": "string" },
                    { "name": "device", "type": "string" }
                ]
           };
           """.trimIndent()
        val schema = Schema.Parser().parse(schemaString)

        val source = KafkaSource
            .builder<ObjectNode>()
            .setBootstrapServers(config.brokers())
            .setTopics("source")
            .setDeserializer(KafkaRecordDeserializationSchema.of(JSONKeyValueDeserializationSchema(false)))
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

        val stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")

        stream
            .filter { it.get("value").get("name").textValue().equals("typeA") }
            .map(JsonToAvro(schemaString), GenericRecordAvroTypeInfo(schema))
            .sinkTo(sink).name("Destination Topic")

        stream
            .filter { it.get("value").get("name").textValue() != "typeA"  }
            .map(JsonToAvro(schemaString), GenericRecordAvroTypeInfo(schema))
            .sinkTo(sinkTwo).name("Destination Two Topic")

        env.execute("Kotlin Flink Starter")
    }
}

