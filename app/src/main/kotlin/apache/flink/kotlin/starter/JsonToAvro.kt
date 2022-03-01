package apache.flink.kotlin.starter

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode

class JsonToAvro(s: String) : RichMapFunction<ObjectNode, GenericRecord>() {
    @Transient val schema = s

    override fun map(msg: ObjectNode): GenericRecord {
        println("HELP ME: " + this.schema)
        return GenericData.Record(Schema.Parser().parse(this.schema)).apply {
            put("name", msg.get("value").get("name").toString())
            put("device", msg.get("value").get("device").toString())
        }
    }
}
