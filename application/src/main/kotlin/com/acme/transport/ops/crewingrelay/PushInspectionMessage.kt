package com.acme.transport.ops.crewingrelay

import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.estimateInspectionRequest
import com.acme.metrics.v1.ExplainMetricSeriesDecisionRequestKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.metrics.v1.explainMetricSeriesDecisionRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.InspectionCalculationService/EstimateInspection and hands what it keeps to the sink of its kind. */
suspend fun pushInspectionMessage(channel: ManagedChannel) {
    val source = InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub(channel)
    val current = source.estimateInspection(estimateInspectionRequest { this.inspectionId = "live-push-inspection-message" })
    val rec = PushInspectionMessageRecord(
        message = normalize(current.result.message),
        subject = current.inspection.subject,
    )
    actOnPushInspectionMessage("metric-series-validation", rec, channel)
}

/** Where pushInspectionMessage hands each record. */
interface PushInspectionMessageSink {
    suspend fun forward(rec: PushInspectionMessageRecord)
}

/** What pushInspectionMessage keeps of each EstimateInspection response. */
data class PushInspectionMessageRecord(val message: String, val subject: String)

/** Hands records to acme.driverapp.v1.DriverInboxReportingService. */
class PushInspectionMessageDriverInboxReporting(private val stub: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub) : PushInspectionMessageSink {
    override suspend fun forward(rec: PushInspectionMessageRecord) {
        stub.scheduleDriverInboxReport(scheduleDriverInboxReportRequest { this.driverInboxId = rec.message })
    }
}

/** Writes records to acme.metrics.v1.MetricSeriesValidationService. */
class PushInspectionMessageMetricSeriesValidation(private val stub: MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub) : PushInspectionMessageSink {
    override suspend fun forward(rec: PushInspectionMessageRecord) {
        val request = explainMetricSeriesDecisionRequest {
            this.details = ExplainMetricSeriesDecisionRequestKt.details { this.locale = rec.message }
        }
        stub.explainMetricSeriesDecision(request)
    }
}

/** Writes records to acme.yards.v1.YardValidationService. */
class PushInspectionMessageYardValidation(private val stub: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub) : PushInspectionMessageSink {
    override suspend fun forward(rec: PushInspectionMessageRecord) {
        RpcBoundary.run("acme.yards.v1.YardValidationService/ScoreYard") { stub.scoreYard(scoreYardRequest { this.yard = yard { this.code = rec.subject } }) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPushInspectionMessage(kind: String, rec: PushInspectionMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "driver-inbox-reporting" to { PushInspectionMessageDriverInboxReporting(DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(channel)).forward(rec) },
        "metric-series-validation" to { PushInspectionMessageMetricSeriesValidation(MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub(channel)).forward(rec) },
        "yard-validation" to { PushInspectionMessageYardValidation(YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
