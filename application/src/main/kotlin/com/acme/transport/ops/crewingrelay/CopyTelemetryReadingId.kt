package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.ListConsentRecordReportsRequestKt
import com.acme.customers.v1.listConsentRecordReportsRequest
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.startPartWorkflowRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.rejectTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.TelemetryReadingApprovalService/RejectTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun copyTelemetryReadingId(channel: ManagedChannel) {
    val origin = TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.rejectTelemetryReading(rejectTelemetryReadingRequest { this.telemetryReadingId = "default-copy-telemetry-reading-id" }) }
    val entry = CopyTelemetryReadingIdRecord(
        referenceId = got.outcome.referenceId,
        source = got.telemetryReading.source,
    )
    dispatchCopyTelemetryReadingId("consent-record-reporting", entry, channel)
}

/** Where copyTelemetryReadingId hands each record. */
interface CopyTelemetryReadingIdSink {
    suspend fun transmit(rec: CopyTelemetryReadingIdRecord)
}

/** What copyTelemetryReadingId keeps of each RejectTelemetryReading response. */
data class CopyTelemetryReadingIdRecord(val referenceId: String, val source: String)

/** Passes records to acme.customers.v1.ConsentRecordReportingService. */
class CopyTelemetryReadingIdConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : CopyTelemetryReadingIdSink {
    override suspend fun transmit(rec: CopyTelemetryReadingIdRecord) {
        val request = listConsentRecordReportsRequest {
            this.filter = ListConsentRecordReportsRequestKt.filter { this.query = rec.source }
        }
        stub.listConsentRecordReports(request)
    }
}

/** Sends records to acme.maintenance.v1.PartWorkflowService. */
class CopyTelemetryReadingIdPartWorkflow(private val stub: PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub) : CopyTelemetryReadingIdSink {
    override suspend fun transmit(rec: CopyTelemetryReadingIdRecord) {
        stub.startPartWorkflow(startPartWorkflowRequest { this.reason = rec.referenceId; this.partId = rec.source })
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyTelemetryReadingIdLogOnly : CopyTelemetryReadingIdSink {
    override suspend fun transmit(rec: CopyTelemetryReadingIdRecord) {
        LoggerFactory.getLogger("CopyTelemetryReadingIdRecord").atInfo().addKeyValue("referenceId", rec.referenceId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchCopyTelemetryReadingId(kind: String, rec: CopyTelemetryReadingIdRecord, channel: ManagedChannel) {
    var write: suspend (CopyTelemetryReadingIdRecord) -> Unit = CopyTelemetryReadingIdLogOnly()::transmit
    when (kind) {
        "consent-record-reporting" -> write = CopyTelemetryReadingIdConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel))::transmit
        "part-workflow" -> write = CopyTelemetryReadingIdPartWorkflow(PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub(channel))::transmit
    }
    write(rec)
}
