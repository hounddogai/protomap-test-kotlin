package com.acme.transport.ops.fleetopsbackfill

import com.acme.common.v1.pageRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.SuspendInspectionRequestKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.resumeVehicleGroupWorkflowRequest
import com.acme.fleet.v1.suspendInspectionRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.startRoutePlanWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.BrowseDocksRequestKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.browseDocksRequest
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RoutePlanWorkflowService/StartRoutePlanWorkflow and hands what it keeps to the sink of its kind. */
suspend fun forwardRoutePlanWorkflowMessage(channel: ManagedChannel) {
    val reader = RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.startRoutePlanWorkflow(startRoutePlanWorkflowRequest { this.reason = "current-forward-route-plan-workflow-message" }) }
    val record = ForwardRoutePlanWorkflowMessageRecord(
        message = reply.result.message,
        number = reply.routePlan.number,
        notes = reply.routePlan.notes,
        outcomeMessage = reply.outcome.message,
    )
    val registry = mutableMapOf<String, ForwardRoutePlanWorkflowMessageWriter>()
    for (registration in listOf(
        ForwardRoutePlanWorkflowMessageRegistration("dock-query", ForwardRoutePlanWorkflowMessageDockQuery(DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel))),
        ForwardRoutePlanWorkflowMessageRegistration("vehicle-group-workflow", ForwardRoutePlanWorkflowMessageVehicleGroupWorkflow(VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub(channel))),
        ForwardRoutePlanWorkflowMessageRegistration("inspection-lifecycle", ForwardRoutePlanWorkflowMessageInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["inspection-lifecycle"] ?: return
    sink.transmit(record)
}

/** Where forwardRoutePlanWorkflowMessage hands each record. */
interface ForwardRoutePlanWorkflowMessageWriter {
    suspend fun transmit(rec: ForwardRoutePlanWorkflowMessageRecord)
}

/** What forwardRoutePlanWorkflowMessage keeps of each StartRoutePlanWorkflow response. */
data class ForwardRoutePlanWorkflowMessageRecord(val message: String, val number: String, val notes: String, val outcomeMessage: String)

/** Passes records to acme.yards.v1.DockQueryService. */
class ForwardRoutePlanWorkflowMessageDockQuery(private val stub: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub) : ForwardRoutePlanWorkflowMessageWriter {
    override suspend fun transmit(rec: ForwardRoutePlanWorkflowMessageRecord) {
        val request = browseDocksRequest {
            this.page = pageRequest { this.pageToken = rec.outcomeMessage }
            this.filter = BrowseDocksRequestKt.filter { this.query = rec.number }
            this.orderBy = rec.message
        }
        stub.browseDocks(request)
    }
}

/** Hands records to acme.fleet.v1.VehicleGroupWorkflowService. */
class ForwardRoutePlanWorkflowMessageVehicleGroupWorkflow(private val stub: VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub) : ForwardRoutePlanWorkflowMessageWriter {
    override suspend fun transmit(rec: ForwardRoutePlanWorkflowMessageRecord) {
        val request = resumeVehicleGroupWorkflowRequest {
            this.vehicleGroup = vehicleGroup { this.name = rec.number }
        }
        stub.resumeVehicleGroupWorkflow(request)
    }
}

/** Hands records to acme.fleet.v1.InspectionLifecycleService. */
class ForwardRoutePlanWorkflowMessageInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : ForwardRoutePlanWorkflowMessageWriter {
    override suspend fun transmit(rec: ForwardRoutePlanWorkflowMessageRecord) {
        val request = suspendInspectionRequest {
            this.details = SuspendInspectionRequestKt.details { this.reasonCode = rec.notes }
            this.reason = rec.message
        }
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/SuspendInspection") { stub.suspendInspection(request) }
    }
}

/** A sink and the kind of record it takes. */
class ForwardRoutePlanWorkflowMessageRegistration(val kind: String, val sink: ForwardRoutePlanWorkflowMessageWriter)
