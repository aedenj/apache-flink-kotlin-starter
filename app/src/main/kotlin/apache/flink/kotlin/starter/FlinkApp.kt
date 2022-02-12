@file:JvmName("FlinkApp")

package apache.flink.kotlin.starter

import org.apache.flink.api.java.utils.ParameterTool

fun main(args: Array<String>) {
    val params = ParameterTool.fromArgs(args)
    StreamingJob.run(JobConfig.getInstance(params.getRequired("env")))
}
