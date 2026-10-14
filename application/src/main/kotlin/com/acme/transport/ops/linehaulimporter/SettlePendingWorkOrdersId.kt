package com.acme.transport.ops.linehaulimporter

import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.listPendingWorkOrdersRequest
import com.acme.telematics.v1.StartTripLogWorkflowRequestKt
import com.acme.telematics.v1.TripLogWorkflowServiceGrpcKt
import com.acme.telematics.v1.startTripLogWorkflowRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderApprovalService/ListPendingWorkOrders and hands what it keeps to acme.telematics.v1.TripLogWorkflowService. */
suspend fun settlePendingWorkOrdersId(channel: ManagedChannel) {
    val origin = WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.listPendingWorkOrders(listPendingWorkOrdersRequest { this.orderBy = "main-settle-pending-work-orders-id" }) }
    val rec = SettlePendingWorkOrdersIdRecord(
        correlationId = got.result.correlationId,
        status = got.result.status,
        message = got.result.message,
        nextPageToken = normalize(got.pageInfo.nextPageToken),
    )
    SettlePendingWorkOrdersIdTripLogWorkflow(TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub(channel)).write(rec)
}

/** What settlePendingWorkOrdersId keeps of each ListPendingWorkOrders response. */
data class SettlePendingWorkOrdersIdRecord(val correlationId: String, val status: String, val message: String, val nextPageToken: String)

/** Sends records to acme.telematics.v1.TripLogWorkflowService. */
class SettlePendingWorkOrdersIdTripLogWorkflow(private val stub: TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub) {
    suspend fun write(rec: SettlePendingWorkOrdersIdRecord) {
        val request = startTripLogWorkflowRequest {
            this.tripLogId = rec.nextPageToken
            this.details = StartTripLogWorkflowRequestKt.details { this.step = rec.correlationId }
            this.tripLog = tripLog { this.notes = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TripLogWorkflowService/StartTripLogWorkflow") { stub.startTripLogWorkflow(request) }
    }
}
