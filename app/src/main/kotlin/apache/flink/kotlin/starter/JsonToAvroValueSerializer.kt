package apache.flink.kotlin.starter

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.formats.avro.AvroSerializationSchema
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode

class JsonToAvroValueSerializer(serializer: AvroSerializationSchema<GenericRecord>) : SerializationSchema<JsonNode> {
    private val serializer = serializer

    override fun serialize(element: JsonNode): ByteArray {
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
        val record = GenericData.Record(schema).apply {
            put("name", element.get("name").toString())
            put("device", element.get("device").toString())
        }

        return serializer.serialize(record)
    }
}
