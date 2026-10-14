package com.acme.transport.ops.customsauditor

import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.getMetricSeriesRulesRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.scoreYardMoveRequest
import com.acme.yards.v1.suspendYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.metrics.v1.MetricSeriesValidationService/GetMetricSeriesRules and hands what it keeps to the sink of its kind. */
suspend fun settleMetricSeriesRulesName(channel: ManagedChannel) {
    val origin = MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub(channel)
    val reply = origin.getMetricSeriesRules(getMetricSeriesRulesRequest { this.metricSeriesId = "main-settle-metric-series-rules-name" })
    val record = SettleMetricSeriesRulesNameRecord(
        name = reply.metricSeries.name,
        correlationId = reply.result.correlationId,
        metricSeriesId = reply.metricSeries.metricSeriesId,
        code = reply.metricSeries.code,
    )
    dispatchSettleMetricSeriesRulesName("yard-move-lifecycle", record, channel)
}

/** Where settleMetricSeriesRulesName hands each record. */
interface SettleMetricSeriesRulesNameSink {
    suspend fun write(rec: SettleMetricSeriesRulesNameRecord)
}

/** What settleMetricSeriesRulesName keeps of each GetMetricSeriesRules response. */
data class SettleMetricSeriesRulesNameRecord(val name: String, val correlationId: String, val metricSeriesId: String, val code: String)

/** Writes records to acme.yards.v1.YardMoveLifecycleService. */
class SettleMetricSeriesRulesNameYardMoveLifecycle(private val stub: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub) : SettleMetricSeriesRulesNameSink {
    override suspend fun write(rec: SettleMetricSeriesRulesNameRecord) {
        val request = suspendYardMoveRequest {
            this.yardMoveId = rec.metricSeriesId
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/SuspendYardMove") { stub.suspendYardMove(request) }
    }
}

/** Sends records to acme.yards.v1.YardMoveValidationService. */
class SettleMetricSeriesRulesNameYardMoveValidation(private val stub: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub) : SettleMetricSeriesRulesNameSink {
    override suspend fun write(rec: SettleMetricSeriesRulesNameRecord) {
        stub.scoreYardMove(scoreYardMoveRequest { this.yardMove = yardMove { this.yardMoveId = rec.correlationId } })
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleMetricSeriesRulesNameLogOnly : SettleMetricSeriesRulesNameSink {
    override suspend fun write(rec: SettleMetricSeriesRulesNameRecord) {
        LoggerFactory.getLogger("SettleMetricSeriesRulesNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchSettleMetricSeriesRulesName(kind: String, rec: SettleMetricSeriesRulesNameRecord, channel: ManagedChannel) {
    var write: suspend (SettleMetricSeriesRulesNameRecord) -> Unit = SettleMetricSeriesRulesNameLogOnly()::write
    when (kind) {
        "yard-move-lifecycle" -> write = SettleMetricSeriesRulesNameYardMoveLifecycle(YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(channel))::write
        "yard-move-validation" -> write = SettleMetricSeriesRulesNameYardMoveValidation(YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
