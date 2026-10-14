package com.acme.transport.ops.networkmirror

import com.acme.fleet.v1.RetryVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.retryVehicleWorkflowRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.approveFuelCardRequest
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.updateRouteRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RouteService/UpdateRoute and hands what it keeps to the sink of its kind. */
suspend fun syncRouteStatus(channel: ManagedChannel) {
    val origin = RouteServiceGrpcKt.RouteServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.updateRoute(updateRouteRequest { }) }
    val row = SyncRouteStatusRecord(
        status = found.result.status,
        message = normalize(found.result.message),
        routeId = found.route.routeId,
    )
    val sinks = mapOf<String, SyncRouteStatusSink>(
        "vehicle-workflow" to SyncRouteStatusVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)),
        "fuel-card-approval" to SyncRouteStatusFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("SYNC_ROUTE_STATUS_TARGET")] ?: return
    sink.forward(row)
}

/** Where syncRouteStatus hands each record. */
interface SyncRouteStatusSink {
    suspend fun forward(rec: SyncRouteStatusRecord)
}

/** What syncRouteStatus keeps of each UpdateRoute response. */
data class SyncRouteStatusRecord(val status: String, val message: String, val routeId: String)

/** Sends records to acme.fleet.v1.VehicleWorkflowService. */
class SyncRouteStatusVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : SyncRouteStatusSink {
    override suspend fun forward(rec: SyncRouteStatusRecord) {
        val request = retryVehicleWorkflowRequest {
            this.vehicleId = rec.status
            this.details = RetryVehicleWorkflowRequestKt.details { this.initiatedBy = rec.message; this.step = rec.routeId }
        }
        stub.retryVehicleWorkflow(request)
    }
}

/** Hands records to acme.fuel.v1.FuelCardApprovalService. */
class SyncRouteStatusFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : SyncRouteStatusSink {
    override suspend fun forward(rec: SyncRouteStatusRecord) {
        stub.approveFuelCard(approveFuelCardRequest { this.reason = rec.message })
    }
}
