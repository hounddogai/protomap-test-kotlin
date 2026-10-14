package com.acme.transport.ops.linehaulimporter

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.deleteAuditEventRequest
import com.acme.customers.v1.ConsentRecordWorkflowServiceGrpcKt
import com.acme.customers.v1.advanceConsentRecordWorkflowRequest
import com.acme.telematics.v1.ListTelemetryReadingReportsRequestKt
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.listTelemetryReadingReportsRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.cancelYardWorkflowRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.ConsentRecordWorkflowService/AdvanceConsentRecordWorkflow and hands what it keeps to the sink of its kind. */
suspend fun propagateConsentRecordWorkflowId(channel: ManagedChannel) {
    val reader = ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub(channel)
    val current = reader.advanceConsentRecordWorkflow(advanceConsentRecordWorkflowRequest { this.reason = "default-propagate-consent-record-workflow-id" })
    val rec = PropagateConsentRecordWorkflowIdRecord(
        consentRecordId = current.consentRecord.consentRecordId,
        status = current.outcome.status,
        message = normalize(current.result.message),
    )
    val sinks = mapOf<String, PropagateConsentRecordWorkflowIdSink>(
        "yard-workflow" to PropagateConsentRecordWorkflowIdYardWorkflow(YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub(channel)),
        "telemetry-reading-reporting" to PropagateConsentRecordWorkflowIdTelemetryReadingReporting(TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub(channel)),
        "audit-event" to PropagateConsentRecordWorkflowIdAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)),
    )
    routePropagateConsentRecordWorkflowId(sinks, "telemetry-reading-reporting", rec)
}

/** Where propagateConsentRecordWorkflowId hands each record. */
interface PropagateConsentRecordWorkflowIdSink {
    suspend fun write(rec: PropagateConsentRecordWorkflowIdRecord)
}

/** What propagateConsentRecordWorkflowId keeps of each AdvanceConsentRecordWorkflow response. */
data class PropagateConsentRecordWorkflowIdRecord(val consentRecordId: String, val status: String, val message: String)

/** Writes records to acme.yards.v1.YardWorkflowService. */
class PropagateConsentRecordWorkflowIdYardWorkflow(private val stub: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub) : PropagateConsentRecordWorkflowIdSink {
    override suspend fun write(rec: PropagateConsentRecordWorkflowIdRecord) {
        stub.cancelYardWorkflow(cancelYardWorkflowRequest { this.yard = yard { this.code = rec.message } })
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingReportingService. */
class PropagateConsentRecordWorkflowIdTelemetryReadingReporting(private val stub: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub) : PropagateConsentRecordWorkflowIdSink {
    override suspend fun write(rec: PropagateConsentRecordWorkflowIdRecord) {
        val request = listTelemetryReadingReportsRequest {
            this.orderBy = rec.status
            this.filter = ListTelemetryReadingReportsRequestKt.filter { this.ownerId = rec.consentRecordId }
        }
        stub.listTelemetryReadingReports(request)
    }
}

/** Hands records to acme.audit.v1.AuditEventService. */
class PropagateConsentRecordWorkflowIdAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : PropagateConsentRecordWorkflowIdSink {
    override suspend fun write(rec: PropagateConsentRecordWorkflowIdRecord) {
        stub.deleteAuditEvent(deleteAuditEventRequest { this.etag = rec.message })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routePropagateConsentRecordWorkflowId(sinks: Map<String, PropagateConsentRecordWorkflowIdSink>, kind: String, rec: PropagateConsentRecordWorkflowIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.write(rec)
}
