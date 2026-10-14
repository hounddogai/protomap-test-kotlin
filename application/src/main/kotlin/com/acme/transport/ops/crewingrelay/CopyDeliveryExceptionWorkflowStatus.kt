package com.acme.transport.ops.crewingrelay

import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.ResumeProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.inspectDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.resumeProofOfDeliveryWorkflowRequest
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.ResumeDispatchAssignmentWorkflowRequestKt
import com.acme.dispatch.v1.resumeDispatchAssignmentWorkflowRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.pushFuelTransactionRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryExceptionWorkflowService/InspectDeliveryExceptionWorkflow and hands what it keeps to the sink of its kind. */
suspend fun copyDeliveryExceptionWorkflowStatus(channel: ManagedChannel) {
    val reader = DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.inspectDeliveryExceptionWorkflow(inspectDeliveryExceptionWorkflowRequest { this.deliveryExceptionId = "nightly-copy-delivery-exception-workflow-status" }) }
    val row = CopyDeliveryExceptionWorkflowStatusRecord(
        status = got.result.status,
        subject = got.deliveryException.subject,
        deliveryExceptionId = got.deliveryException.deliveryExceptionId,
        message = got.result.message,
    )
    val sinks = mapOf<String, CopyDeliveryExceptionWorkflowStatusWriter>(
        "dispatch-assignment-workflow" to CopyDeliveryExceptionWorkflowStatusDispatchAssignmentWorkflow(DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub(channel)),
        "proof-of-delivery-workflow" to CopyDeliveryExceptionWorkflowStatusProofOfDeliveryWorkflow(ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel)),
        "fuel-transaction-sync" to CopyDeliveryExceptionWorkflowStatusFuelTransactionSync(FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub(channel)),
    )
    routeCopyDeliveryExceptionWorkflowStatus(sinks, "dispatch-assignment-workflow", row)
}

/** Where copyDeliveryExceptionWorkflowStatus hands each record. */
interface CopyDeliveryExceptionWorkflowStatusWriter {
    suspend fun hand(rec: CopyDeliveryExceptionWorkflowStatusRecord)
}

/** What copyDeliveryExceptionWorkflowStatus keeps of each InspectDeliveryExceptionWorkflow response. */
data class CopyDeliveryExceptionWorkflowStatusRecord(val status: String, val subject: String, val deliveryExceptionId: String, val message: String)

/** Writes records to acme.dispatch.v1.DispatchAssignmentWorkflowService. */
class CopyDeliveryExceptionWorkflowStatusDispatchAssignmentWorkflow(private val stub: DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub) : CopyDeliveryExceptionWorkflowStatusWriter {
    override suspend fun hand(rec: CopyDeliveryExceptionWorkflowStatusRecord) {
        val request = resumeDispatchAssignmentWorkflowRequest {
            this.details = ResumeDispatchAssignmentWorkflowRequestKt.details { this.step = rec.deliveryExceptionId }
        }
        stub.resumeDispatchAssignmentWorkflow(request)
    }
}

/** Hands records to acme.deliveries.v1.ProofOfDeliveryWorkflowService. */
class CopyDeliveryExceptionWorkflowStatusProofOfDeliveryWorkflow(private val stub: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub) : CopyDeliveryExceptionWorkflowStatusWriter {
    override suspend fun hand(rec: CopyDeliveryExceptionWorkflowStatusRecord) {
        val request = resumeProofOfDeliveryWorkflowRequest {
            this.details = ResumeProofOfDeliveryWorkflowRequestKt.details { this.initiatedBy = rec.subject }
            this.proofOfDelivery = proofOfDelivery { this.number = rec.status }
            this.reason = rec.deliveryExceptionId
        }
        stub.resumeProofOfDeliveryWorkflow(request)
    }
}

/** Writes records to acme.fuel.v1.FuelTransactionSyncService. */
class CopyDeliveryExceptionWorkflowStatusFuelTransactionSync(private val stub: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub) : CopyDeliveryExceptionWorkflowStatusWriter {
    override suspend fun hand(rec: CopyDeliveryExceptionWorkflowStatusRecord) {
        stub.pushFuelTransaction(pushFuelTransactionRequest { this.fuelTransactionId = rec.message })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCopyDeliveryExceptionWorkflowStatus(sinks: Map<String, CopyDeliveryExceptionWorkflowStatusWriter>, kind: String, rec: CopyDeliveryExceptionWorkflowStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.hand(rec)
}
