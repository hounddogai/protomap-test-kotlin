package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.fleet.v1.CancelVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.cancelVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.approveFuelTransactionRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.maintenance.v1.ListWorkOrdersRequestKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.listWorkOrdersRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.listToursRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.tours.v1.TourService/ListTours and hands what it keeps to the sink of its kind. */
suspend fun handOffToursId(channel: ManagedChannel) {
    val origin = TourServiceGrpcKt.TourServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.listTours(listToursRequest { this.orderBy = "current-hand-off-tours-id" }) }
    val rec = HandOffToursIdRecord(
        correlationId = normalize(found.result.correlationId),
        nextPageToken = found.pageInfo.nextPageToken,
        message = normalize(found.result.message),
        status = normalize(found.result.status),
    )
    actOnHandOffToursId("work-order", rec, channel)
}

/** Where handOffToursId hands each record. */
interface HandOffToursIdSink {
    suspend fun transmit(rec: HandOffToursIdRecord)
}

/** What handOffToursId keeps of each ListTours response. */
data class HandOffToursIdRecord(val correlationId: String, val nextPageToken: String, val message: String, val status: String)

/** Hands records to acme.maintenance.v1.WorkOrderService. */
class HandOffToursIdWorkOrder(private val stub: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub) : HandOffToursIdSink {
    override suspend fun transmit(rec: HandOffToursIdRecord) {
        val request = listWorkOrdersRequest {
            this.filter = ListWorkOrdersRequestKt.filter { this.ownerId = rec.nextPageToken }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.listWorkOrders(request)
    }
}

/** Hands records to acme.fleet.v1.VehicleWorkflowService. */
class HandOffToursIdVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : HandOffToursIdSink {
    override suspend fun transmit(rec: HandOffToursIdRecord) {
        val request = cancelVehicleWorkflowRequest {
            this.vehicle = vehicle { this.vehicleId = rec.status }
            this.details = CancelVehicleWorkflowRequestKt.details { this.step = rec.message }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/CancelVehicleWorkflow") { stub.cancelVehicleWorkflow(request) }
    }
}

/** Writes records to acme.fuel.v1.FuelTransactionApprovalService. */
class HandOffToursIdFuelTransactionApproval(private val stub: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub) : HandOffToursIdSink {
    override suspend fun transmit(rec: HandOffToursIdRecord) {
        val request = approveFuelTransactionRequest {
            this.fuelTransaction = fuelTransaction { this.source = rec.correlationId; this.fuelTransactionId = rec.message }
        }
        stub.approveFuelTransaction(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnHandOffToursId(kind: String, rec: HandOffToursIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "work-order" to { HandOffToursIdWorkOrder(WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub(channel)).transmit(rec) },
        "vehicle-workflow" to { HandOffToursIdVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)).transmit(rec) },
        "fuel-transaction-approval" to { HandOffToursIdFuelTransactionApproval(FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
