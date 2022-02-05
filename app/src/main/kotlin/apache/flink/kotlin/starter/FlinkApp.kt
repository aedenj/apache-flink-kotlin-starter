@file:JvmName("FlinkApp")

package apache.flink.kotlin.starter

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.fromElements(1, 2, 3)
        .map { it * 5 }.returns(Int::class.java)
        .print()

    env.execute("Kotlin Flink Starter")
}
