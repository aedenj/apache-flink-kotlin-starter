package apache.flink.kotlin.starter.serde

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.formats.avro.typeutils.GenericRecordAvroTypeInfo
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import tech.allegro.schema.json2avro.converter.CompositeJsonToAvroReader
import tech.allegro.schema.json2avro.converter.JsonAvroConverter
import tech.allegro.schema.json2avro.converter.types.AvroTypeConverter
import java.util.*

class JsonAvroDeserializationSchema(avroSchema:String) : KafkaRecordDeserializationSchema<GenericRecord> {
    private val serialVersionUID = 1L

    private val schemaString  = avroSchema
    private val producedType  = GenericRecordAvroTypeInfo(Schema.Parser().parse(avroSchema))
    @Transient private lateinit var schema: Schema
    @Transient private lateinit var converter: JsonAvroConverter

    override fun open(context: DeserializationSchema.InitializationContext) {
        super.open(context)

        this.schema = Schema.Parser().parse(schemaString)
        this.converter = JsonAvroConverter(CompositeJsonToAvroReader(SecondsConverter()))
    }

    override fun deserialize(record: ConsumerRecord<ByteArray, ByteArray>, out: Collector<GenericRecord>) {
        out.collect(converter.convertToGenericDataRecord(record.value(), this.schema))
    }

    override fun getProducedType(): TypeInformation<GenericRecord> {
        return this.producedType
    }

    class SecondsConverter : AvroTypeConverter {
        override fun convert(
            field: Schema.Field,
            schema: Schema,
            jsonValue: Any,
            path: Deque<String>,
            silently: Boolean
        ): Any {
            return when(jsonValue) {
                is Int -> jsonValue
                is String -> jsonValue.toInt()
                else -> field.defaultVal()
            }
        }

        override fun canManage(schema: Schema, path: Deque<String>): Boolean {
            return "seconds" == path.last()
        }
    }
}
