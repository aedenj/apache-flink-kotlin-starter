package apache.flink.kotlin.starter

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode

class JsonToAvro(s: String) : RichMapFunction<ObjectNode, GenericRecord>() {
    private val schemaString = s
    @Transient private lateinit var schema: Schema

    override fun open(parameters: Configuration?) {
        super.open(parameters)
        this.schema = Schema.Parser().parse(schemaString)
   }

    override fun map(msg: ObjectNode): GenericRecord {
        return GenericData.Record(this.schema).apply {
            put("name", msg.get("value").get("name").toString())
            put("device", msg.get("value").get("device").toString())
        }
    }
}
