package com.acme.transport.ops.terminalrelay

import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.startDeliveryWorkflowRequest
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.retryVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.listPendingRoutePlansRequest
import com.acme.telematics.v1.TerminateTripLogRequestKt
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.terminateTripLogRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RoutePlanApprovalService/ListPendingRoutePlans and hands what it keeps to the sink of its kind. */
suspend fun backfillPendingRoutePlansMessage(channel: ManagedChannel) {
    val reader = RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.listPendingRoutePlans(listPendingRoutePlansRequest { this.orderBy = "primary-backfill-pending-route-plans-message" }) }
    val record = BackfillPendingRoutePlansMessageRecord(
        message = current.result.message,
        correlationId = current.result.correlationId,
        nextPageToken = normalize(current.pageInfo.nextPageToken),
        status = current.result.status,
    )
    actOnBackfillPendingRoutePlansMessage("vehicle-workflow", record, channel)
}

/** Where backfillPendingRoutePlansMessage hands each record. */
interface BackfillPendingRoutePlansMessageTarget {
    suspend fun forward(rec: BackfillPendingRoutePlansMessageRecord)
}

/** What backfillPendingRoutePlansMessage keeps of each ListPendingRoutePlans response. */
data class BackfillPendingRoutePlansMessageRecord(val message: String, val correlationId: String, val nextPageToken: String, val status: String)

/** Sends records to acme.telematics.v1.TripLogLifecycleService. */
class BackfillPendingRoutePlansMessageTripLogLifecycle(private val stub: TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub) : BackfillPendingRoutePlansMessageTarget {
    override suspend fun forward(rec: BackfillPendingRoutePlansMessageRecord) {
        val request = terminateTripLogRequest {
            this.details = TerminateTripLogRequestKt.details { this.reasonCode = rec.status }
        }
        stub.terminateTripLog(request)
    }
}

/** Passes records to acme.deliveries.v1.DeliveryWorkflowService. */
class BackfillPendingRoutePlansMessageDeliveryWorkflow(private val stub: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub) : BackfillPendingRoutePlansMessageTarget {
    override suspend fun forward(rec: BackfillPendingRoutePlansMessageRecord) {
        val request = startDeliveryWorkflowRequest {
            this.delivery = delivery { this.notes = rec.correlationId; this.deliveryId = rec.nextPageToken }
            this.deliveryId = rec.message
        }
        stub.startDeliveryWorkflow(request)
    }
}

/** Sends records to acme.fleet.v1.VehicleWorkflowService. */
class BackfillPendingRoutePlansMessageVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : BackfillPendingRoutePlansMessageTarget {
    override suspend fun forward(rec: BackfillPendingRoutePlansMessageRecord) {
        stub.retryVehicleWorkflow(retryVehicleWorkflowRequest { this.vehicle = vehicle { this.code = rec.message } })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnBackfillPendingRoutePlansMessage(kind: String, rec: BackfillPendingRoutePlansMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "trip-log-lifecycle" to { BackfillPendingRoutePlansMessageTripLogLifecycle(TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(channel)).forward(rec) },
        "delivery-workflow" to { BackfillPendingRoutePlansMessageDeliveryWorkflow(DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel)).forward(rec) },
        "vehicle-workflow" to { BackfillPendingRoutePlansMessageVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
