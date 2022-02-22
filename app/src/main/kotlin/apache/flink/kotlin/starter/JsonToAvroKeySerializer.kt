package apache.flink.kotlin.starter

import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode
import java.nio.charset.StandardCharsets

class JsonToAvroKeySerializer: SerializationSchema<JsonNode> {
    override fun serialize(element: JsonNode): ByteArray {
        return "1".toByteArray(StandardCharsets.UTF_8)
    }
}
