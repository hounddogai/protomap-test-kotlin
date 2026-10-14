package com.acme.transport.ops.emissionspublisher

import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveDispatchAssignmentRequest
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.deleteTelemetryReadingReportRequest
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.getYardRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.YardService/GetYard and hands what it keeps to the sink of its kind. */
suspend fun pushYardName(channel: ManagedChannel) {
    val origin = YardServiceGrpcKt.YardServiceCoroutineStub(channel)
    val reply = origin.getYard(getYardRequest { this.yardId = "main-push-yard-name" })
    val rec = PushYardNameRecord(
        name = reply.yard.name,
        yardId = reply.yard.yardId,
    )
    dispatchPushYardName("dispatch-assignment-approval", rec, channel)
}

/** Where pushYardName hands each record. */
interface PushYardNameTarget {
    suspend fun forward(rec: PushYardNameRecord)
}

/** What pushYardName keeps of each GetYard response. */
data class PushYardNameRecord(val name: String, val yardId: String)

/** Writes records to acme.telematics.v1.TelemetryReadingReportingService. */
class PushYardNameTelemetryReadingReporting(private val stub: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub) : PushYardNameTarget {
    override suspend fun forward(rec: PushYardNameRecord) {
        stub.deleteTelemetryReadingReport(deleteTelemetryReadingReportRequest { this.etag = rec.yardId; this.reason = rec.name })
    }
}

/** Sends records to acme.dispatch.v1.DispatchAssignmentApprovalService. */
class PushYardNameDispatchAssignmentApproval(private val stub: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub) : PushYardNameTarget {
    override suspend fun forward(rec: PushYardNameRecord) {
        val request = approveDispatchAssignmentRequest {
            this.dispatchAssignmentId = rec.name
            this.reason = rec.yardId
        }
        stub.approveDispatchAssignment(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PushYardNameLogOnly : PushYardNameTarget {
    override suspend fun forward(rec: PushYardNameRecord) {
        LoggerFactory.getLogger("PushYardNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPushYardName(kind: String, rec: PushYardNameRecord, channel: ManagedChannel) {
    var write: suspend (PushYardNameRecord) -> Unit = PushYardNameLogOnly()::forward
    when (kind) {
        "telemetry-reading-reporting" -> write = PushYardNameTelemetryReadingReporting(TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub(channel))::forward
        "dispatch-assignment-approval" -> write = PushYardNameDispatchAssignmentApproval(DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
