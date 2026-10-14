package com.acme.transport.ops.lastmilesync

import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.cancelDriverInboxWorkflowRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.getTelemetryReadingApprovalStatusRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverInboxWorkflowService/CancelDriverInboxWorkflow and hands what it keeps to acme.telematics.v1.TelemetryReadingApprovalService. */
suspend fun replayDriverInboxWorkflowNumber(channel: ManagedChannel) {
    val reader = DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.cancelDriverInboxWorkflow(cancelDriverInboxWorkflowRequest { this.reason = "current-replay-driver-inbox-workflow-number" }) }
    val row = ReplayDriverInboxWorkflowNumberRecord(
        number = found.driverInbox.number,
        status = normalize(found.outcome.status),
        message = found.outcome.message,
        driverInboxId = found.driverInbox.driverInboxId,
    )
    ReplayDriverInboxWorkflowNumberTelemetryReadingApproval(TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel)).transmit(row)
}

/** What replayDriverInboxWorkflowNumber keeps of each CancelDriverInboxWorkflow response. */
data class ReplayDriverInboxWorkflowNumberRecord(val number: String, val status: String, val message: String, val driverInboxId: String)

/** Sends records to acme.telematics.v1.TelemetryReadingApprovalService. */
class ReplayDriverInboxWorkflowNumberTelemetryReadingApproval(private val stub: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub) {
    suspend fun transmit(rec: ReplayDriverInboxWorkflowNumberRecord) {
        stub.getTelemetryReadingApprovalStatus(getTelemetryReadingApprovalStatusRequest { this.telemetryReadingId = rec.message })
    }
}
