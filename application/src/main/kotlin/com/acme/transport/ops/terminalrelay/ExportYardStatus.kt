package com.acme.transport.ops.terminalrelay

import com.acme.driverapp.v1.CancelDriverInboxWorkflowRequestKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.cancelDriverInboxWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.approveYardRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardApprovalService/ApproveYard and hands what it keeps to acme.driverapp.v1.DriverInboxWorkflowService. */
suspend fun exportYardStatus(channel: ManagedChannel) {
    val source = YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel)
    val reply = source.approveYard(approveYardRequest { this.reason = "primary-export-yard-status" })
    val record = ExportYardStatusRecord(
        status = reply.result.status,
        message = normalize(reply.result.message),
        referenceId = normalize(reply.outcome.referenceId),
    )
    ExportYardStatusDriverInboxWorkflow(DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(channel)).forward(record)
}

/** What exportYardStatus keeps of each ApproveYard response. */
data class ExportYardStatusRecord(val status: String, val message: String, val referenceId: String)

/** Writes records to acme.driverapp.v1.DriverInboxWorkflowService. */
class ExportYardStatusDriverInboxWorkflow(private val stub: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub) {
    suspend fun forward(rec: ExportYardStatusRecord) {
        val request = cancelDriverInboxWorkflowRequest {
            this.driverInboxId = rec.referenceId
            this.details = CancelDriverInboxWorkflowRequestKt.details { this.step = rec.status }
        }
        stub.cancelDriverInboxWorkflow(request)
    }
}
