package apache.flink.kotlin.starter

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object JobConfig
{
    private val config:Config

    init {
        val appConfig = ConfigFactory.load()
        val envConfig = ConfigFactory.load(
            "application." + System.getenv("FLINK_ENV") + ".conf"
        )
        config = appConfig.withFallback(envConfig)
        config.checkValid(ConfigFactory.defaultReference())
    }

    fun brokers(): String {
        return config.getString("kafka.endpoints")
    }

    fun consumer(): Properties? {
        val props = Properties()
        props.setProperty("group.id", config.getString("kafka.consumer.groupId"))
        return props
    }

    fun producer(): Properties? {
        val props = Properties()
        props.setProperty("transaction.timeout.ms",  config.getString("kafka.producer.transTimeout"))
        return props
    }
}
