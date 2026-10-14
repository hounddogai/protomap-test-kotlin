package com.acme.transport.ops.networkmirror

import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.normalizeDeliveryRequest
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.ListPendingDriversRequestKt
import com.acme.drivers.v1.listPendingDriversRequest
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.pauseRouteWorkflowRequest
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.reinstateGeoFenceRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RouteWorkflowService/PauseRouteWorkflow and hands what it keeps to the sink of its kind. */
suspend fun backfillRouteWorkflowNotes(channel: ManagedChannel) {
    val origin = RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel)
    val got = origin.pauseRouteWorkflow(pauseRouteWorkflowRequest { this.routeId = "current-backfill-route-workflow-notes" })
    val row = BackfillRouteWorkflowNotesRecord(
        notes = got.route.notes,
        message = got.result.message,
        referenceId = got.outcome.referenceId,
        number = got.route.number,
    )
    val registry = mutableMapOf<String, BackfillRouteWorkflowNotesWriter>()
    for (registration in listOf(
        BackfillRouteWorkflowNotesRegistration("delivery-validation", BackfillRouteWorkflowNotesDeliveryValidation(DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub(channel))),
        BackfillRouteWorkflowNotesRegistration("driver-approval", BackfillRouteWorkflowNotesDriverApproval(DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel))),
        BackfillRouteWorkflowNotesRegistration("geo-fence-lifecycle", BackfillRouteWorkflowNotesGeoFenceLifecycle(GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["geo-fence-lifecycle"] ?: return
    sink.relay(row)
}

/** Where backfillRouteWorkflowNotes hands each record. */
interface BackfillRouteWorkflowNotesWriter {
    suspend fun relay(rec: BackfillRouteWorkflowNotesRecord)
}

/** What backfillRouteWorkflowNotes keeps of each PauseRouteWorkflow response. */
data class BackfillRouteWorkflowNotesRecord(val notes: String, val message: String, val referenceId: String, val number: String)

/** Sends records to acme.deliveries.v1.DeliveryValidationService. */
class BackfillRouteWorkflowNotesDeliveryValidation(private val stub: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub) : BackfillRouteWorkflowNotesWriter {
    override suspend fun relay(rec: BackfillRouteWorkflowNotesRecord) {
        stub.normalizeDelivery(normalizeDeliveryRequest { this.reason = rec.number })
    }
}

/** Passes records to acme.drivers.v1.DriverApprovalService. */
class BackfillRouteWorkflowNotesDriverApproval(private val stub: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub) : BackfillRouteWorkflowNotesWriter {
    override suspend fun relay(rec: BackfillRouteWorkflowNotesRecord) {
        val request = listPendingDriversRequest {
            this.filter = ListPendingDriversRequestKt.filter { this.ownerId = rec.notes; this.query = rec.referenceId }
            this.page = pageRequest { this.pageToken = rec.number }
        }
        stub.listPendingDrivers(request)
    }
}

/** Writes records to acme.telematics.v1.GeoFenceLifecycleService. */
class BackfillRouteWorkflowNotesGeoFenceLifecycle(private val stub: GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub) : BackfillRouteWorkflowNotesWriter {
    override suspend fun relay(rec: BackfillRouteWorkflowNotesRecord) {
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ReinstateGeoFence") { stub.reinstateGeoFence(reinstateGeoFenceRequest { this.geoFenceId = rec.message }) }
    }
}

/** A sink and the kind of record it takes. */
class BackfillRouteWorkflowNotesRegistration(val kind: String, val sink: BackfillRouteWorkflowNotesWriter)
