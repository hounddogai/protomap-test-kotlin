package com.acme.transport.ops.fleetopsbackfill

import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.RetryDriverWorkflowRequestKt
import com.acme.drivers.v1.retryDriverWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.explainCashPositionCalculationRequest
import io.grpc.ManagedChannel

/** Reads acme.treasury.v1.CashPositionCalculationService/ExplainCashPositionCalculation and hands what it keeps to acme.drivers.v1.DriverWorkflowService. */
suspend fun handOffCashPositionCalculationId(channel: ManagedChannel) {
    val source = CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.explainCashPositionCalculation(explainCashPositionCalculationRequest { this.cashPositionId = "main-hand-off-cash-position-calculation-id" }) }
    val rec = HandOffCashPositionCalculationIdRecord(
        referenceId = reply.outcome.referenceId,
        correlationId = normalize(reply.result.correlationId),
    )
    HandOffCashPositionCalculationIdDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel)).hand(rec)
}

/** What handOffCashPositionCalculationId keeps of each ExplainCashPositionCalculation response. */
data class HandOffCashPositionCalculationIdRecord(val referenceId: String, val correlationId: String)

/** Passes records to acme.drivers.v1.DriverWorkflowService. */
class HandOffCashPositionCalculationIdDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) {
    suspend fun hand(rec: HandOffCashPositionCalculationIdRecord) {
        val request = retryDriverWorkflowRequest {
            this.reason = rec.correlationId
            this.details = RetryDriverWorkflowRequestKt.details { this.step = rec.referenceId }
        }
        stub.retryDriverWorkflow(request)
    }
}
