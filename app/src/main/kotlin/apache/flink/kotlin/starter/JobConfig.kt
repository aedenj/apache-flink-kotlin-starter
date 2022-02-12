package apache.flink.kotlin.starter

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class JobConfig private constructor(env: String) {
    companion object {
        @Volatile
        private var INSTANCE: JobConfig? = null

        @Synchronized
        fun getInstance(env: String): JobConfig = INSTANCE ?: JobConfig(env).also { INSTANCE = it }
    }

    private val config:Config

    init {
        val appConfig = ConfigFactory.load()
        val envConfig = ConfigFactory.load("application.$env.conf")

        config = envConfig.withFallback(appConfig)
        config.checkValid(ConfigFactory.defaultReference())
    }

    fun brokers(): String {
        return config.getString("kafka.endpoints")
    }

    fun consumer(): Properties {
        return Properties().apply {
            setProperty("group.id", config.getString("kafka.consumer.groupId"))
        }
    }

    fun producer(): Properties {
        return Properties().apply {
            setProperty("transaction.timeout.ms",  config.getString("kafka.producer.transTimeout"))
        }
    }
}
