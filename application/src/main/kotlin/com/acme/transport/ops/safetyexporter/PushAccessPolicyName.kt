package com.acme.transport.ops.safetyexporter

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.peekAccessPolicyRequest
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.simulateDutyStatusRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.deleteTelemetryReadingRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.authorization.v1.AccessPolicyQueryService/PeekAccessPolicy and hands what it keeps to the sink of its kind. */
suspend fun pushAccessPolicyName(channel: ManagedChannel) {
    val reader = AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.peekAccessPolicy(peekAccessPolicyRequest { this.accessPolicyId = "nightly-push-access-policy-name" }) }
    val row = PushAccessPolicyNameRecord(
        name = found.accessPolicy.name,
        correlationId = normalize(found.result.correlationId),
        description = found.accessPolicy.description,
    )
    actOnPushAccessPolicyName("telemetry-reading", row, channel)
}

/** Where pushAccessPolicyName hands each record. */
interface PushAccessPolicyNameSink {
    suspend fun forward(rec: PushAccessPolicyNameRecord)
}

/** What pushAccessPolicyName keeps of each PeekAccessPolicy response. */
data class PushAccessPolicyNameRecord(val name: String, val correlationId: String, val description: String)

/** Writes records to acme.drivers.v1.DutyStatusCalculationService. */
class PushAccessPolicyNameDutyStatusCalculation(private val stub: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub) : PushAccessPolicyNameSink {
    override suspend fun forward(rec: PushAccessPolicyNameRecord) {
        val request = simulateDutyStatusRequest {
            this.dutyStatus = dutyStatus { this.dutyStatusId = rec.correlationId; this.source = rec.description; this.kind = rec.name }
        }
        stub.simulateDutyStatus(request)
    }
}

/** Sends records to acme.metrics.v1.MetricSeriesService. */
class PushAccessPolicyNameMetricSeries(private val stub: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub) : PushAccessPolicyNameSink {
    override suspend fun forward(rec: PushAccessPolicyNameRecord) {
        val request = listMetricSeriesesRequest {
            this.filter = ListMetricSeriesesRequestKt.filter { this.ownerId = rec.correlationId }
        }
        RpcBoundary.run("acme.metrics.v1.MetricSeriesService/ListMetricSerieses") { stub.listMetricSerieses(request) }
    }
}

/** Writes records to acme.telematics.v1.TelemetryReadingService. */
class PushAccessPolicyNameTelemetryReading(private val stub: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub) : PushAccessPolicyNameSink {
    override suspend fun forward(rec: PushAccessPolicyNameRecord) {
        val request = deleteTelemetryReadingRequest {
            this.etag = rec.name
            this.telemetryReadingId = rec.correlationId
            this.reason = rec.description
        }
        stub.deleteTelemetryReading(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPushAccessPolicyName(kind: String, rec: PushAccessPolicyNameRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "duty-status-calculation" to { PushAccessPolicyNameDutyStatusCalculation(DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)).forward(rec) },
        "metric-series" to { PushAccessPolicyNameMetricSeries(MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(channel)).forward(rec) },
        "telemetry-reading" to { PushAccessPolicyNameTelemetryReading(TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
