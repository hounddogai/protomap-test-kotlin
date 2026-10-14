package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.countPreferenceTotalsRequest
import com.acme.telematics.v1.EscalateTelemetryReadingRequestKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.escalateTelemetryReadingRequest
import com.acme.telematics.v1.reinstateTripLogRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.PreferenceReportingService/CountPreferenceTotals and hands what it keeps to the sink of its kind. */
suspend fun settlePreferenceTotalsMessage(channel: ManagedChannel) {
    val origin = PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub(channel)
    val reply = origin.countPreferenceTotals(countPreferenceTotalsRequest { this.orderBy = "main-settle-preference-totals-message" })
    val row = SettlePreferenceTotalsMessageRecord(
        message = reply.result.message,
        correlationId = normalize(reply.result.correlationId),
    )
    val registry = mutableMapOf<String, SettlePreferenceTotalsMessageWriter>()
    for (registration in listOf(
        SettlePreferenceTotalsMessageRegistration("trip-log-lifecycle", SettlePreferenceTotalsMessageTripLogLifecycle(TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(channel))),
        SettlePreferenceTotalsMessageRegistration("telemetry-reading-approval", SettlePreferenceTotalsMessageTelemetryReadingApproval(TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["trip-log-lifecycle"] ?: return
    sink.deliver(row)
}

/** Where settlePreferenceTotalsMessage hands each record. */
interface SettlePreferenceTotalsMessageWriter {
    suspend fun deliver(rec: SettlePreferenceTotalsMessageRecord)
}

/** What settlePreferenceTotalsMessage keeps of each CountPreferenceTotals response. */
data class SettlePreferenceTotalsMessageRecord(val message: String, val correlationId: String)

/** Hands records to acme.telematics.v1.TripLogLifecycleService. */
class SettlePreferenceTotalsMessageTripLogLifecycle(private val stub: TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub) : SettlePreferenceTotalsMessageWriter {
    override suspend fun deliver(rec: SettlePreferenceTotalsMessageRecord) {
        stub.reinstateTripLog(reinstateTripLogRequest { this.tripLogId = rec.message; this.reason = rec.correlationId })
    }
}

/** Passes records to acme.telematics.v1.TelemetryReadingApprovalService. */
class SettlePreferenceTotalsMessageTelemetryReadingApproval(private val stub: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub) : SettlePreferenceTotalsMessageWriter {
    override suspend fun deliver(rec: SettlePreferenceTotalsMessageRecord) {
        val request = escalateTelemetryReadingRequest {
            this.details = EscalateTelemetryReadingRequestKt.details { this.approverEmail = rec.message }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/EscalateTelemetryReading") { stub.escalateTelemetryReading(request) }
    }
}

/** A sink and the kind of record it takes. */
class SettlePreferenceTotalsMessageRegistration(val kind: String, val sink: SettlePreferenceTotalsMessageWriter)
