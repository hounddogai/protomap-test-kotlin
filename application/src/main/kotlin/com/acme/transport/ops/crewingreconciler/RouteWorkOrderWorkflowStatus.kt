package com.acme.transport.ops.crewingreconciler

import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.cart.v1.ExplainCartCalculationRequestKt
import com.acme.cart.v1.explainCartCalculationRequest
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.commentInspectionApprovalRequest
import com.acme.fleet.v1.inspection
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderWorkflowService/ResumeWorkOrderWorkflow and hands what it keeps to the sink of its kind. */
suspend fun routeWorkOrderWorkflowStatus(channel: ManagedChannel) {
    val upstream = WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.resumeWorkOrderWorkflow(resumeWorkOrderWorkflowRequest { this.reason = "current-route-work-order-workflow-status" }) }
    val record = RouteWorkOrderWorkflowStatusRecord(
        status = reply.result.status,
        correlationId = normalize(reply.result.correlationId),
        message = reply.outcome.message,
        resultMessage = reply.result.message,
    )
    for (sink in listOf<RouteWorkOrderWorkflowStatusDestination>(
        RouteWorkOrderWorkflowStatusInspectionApproval(InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(channel)),
        RouteWorkOrderWorkflowStatusCartCalculation(CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub(channel)),
    )) {
        sink.relay(record)
    }
}

/** Where routeWorkOrderWorkflowStatus hands each record. */
interface RouteWorkOrderWorkflowStatusDestination {
    suspend fun relay(rec: RouteWorkOrderWorkflowStatusRecord)
}

/** What routeWorkOrderWorkflowStatus keeps of each ResumeWorkOrderWorkflow response. */
data class RouteWorkOrderWorkflowStatusRecord(val status: String, val correlationId: String, val message: String, val resultMessage: String)

/** Sends records to acme.fleet.v1.InspectionApprovalService. */
class RouteWorkOrderWorkflowStatusInspectionApproval(private val stub: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub) : RouteWorkOrderWorkflowStatusDestination {
    override suspend fun relay(rec: RouteWorkOrderWorkflowStatusRecord) {
        val request = commentInspectionApprovalRequest {
            this.inspection = inspection { this.inspectionId = rec.resultMessage }
            this.inspectionId = rec.message
        }
        RpcBoundary.run("acme.fleet.v1.InspectionApprovalService/CommentInspectionApproval") { stub.commentInspectionApproval(request) }
    }
}

/** Sends records to acme.cart.v1.CartCalculationService. */
class RouteWorkOrderWorkflowStatusCartCalculation(private val stub: CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub) : RouteWorkOrderWorkflowStatusDestination {
    override suspend fun relay(rec: RouteWorkOrderWorkflowStatusRecord) {
        val request = explainCartCalculationRequest {
            this.cartId = rec.status
            this.details = ExplainCartCalculationRequestKt.details { this.locale = rec.message }
        }
        RpcBoundary.run("acme.cart.v1.CartCalculationService/ExplainCartCalculation") { stub.explainCartCalculation(request) }
    }
}
