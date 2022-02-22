package apache.flink.kotlin.starter

import org.apache.avro.Schema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.formats.avro.AvroSerializationSchema
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema

object StreamingJob {
    fun run(config: JobConfig) {
        val env = StreamExecutionEnvironment.getExecutionEnvironment().apply {
            getConfig().disableForceKryo()
        }

        val schema = Schema.Parser().parse(
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
        )
        val source = KafkaSource
            .builder<ObjectNode>()
            .setBootstrapServers(config.brokers())
            .setTopics("source")
            .setDeserializer(KafkaRecordDeserializationSchema.of(JSONKeyValueDeserializationSchema(false)))
            .setProperties(config.consumer())
            .build()

        val sink = KafkaSink
            .builder<JsonNode>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<JsonNode>()
                    .setTopic("destination")
                    .setValueSerializationSchema(JsonToAvroValueSerializer(AvroSerializationSchema.forGeneric(schema)))
                    .setKeySerializationSchema(JsonToAvroKeySerializer())
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.NONE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val sinkTwo = KafkaSink
            .builder<JsonNode>()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<JsonNode>()
                    .setTopic("destination-two")
                    .setValueSerializationSchema(JsonToAvroValueSerializer(AvroSerializationSchema.forGeneric(schema)))
                    .setKeySerializationSchema(JsonToAvroKeySerializer())
                    .build()
            )
            .setDeliverGuarantee(DeliveryGuarantee.NONE)
            .setKafkaProducerConfig(config.producer())
            .build()

        val stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Source Topic")

        stream
            .filter { it.get("value").get("name").toString() == "typeA"  }
            .map { it.get("value") }
            .returns(JsonNode::class.java)
            .sinkTo(sink).name("Destination Topic")

        stream
            .filter { it.get("value").get("name").toString() != "typeA"  }
            .map { it.get("value") }
            .returns(JsonNode::class.java)
            .sinkTo(sinkTwo).name("Destination Two Topic")

        env.execute("Kotlin Flink Starter")
    }
}

