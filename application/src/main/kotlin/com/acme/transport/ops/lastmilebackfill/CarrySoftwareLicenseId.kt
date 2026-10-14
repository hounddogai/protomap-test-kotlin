package com.acme.transport.ops.lastmilebackfill

import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.escalateDeliveryExceptionRequest
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.scoreInspectionRequest
import com.acme.licensing.v1.SoftwareLicenseServiceGrpcKt
import com.acme.licensing.v1.updateSoftwareLicenseRequest
import com.acme.routing.v1.AdvanceRouteWorkflowRequestKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.advanceRouteWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.rejectDockRequest
import io.grpc.ManagedChannel

/** Reads acme.licensing.v1.SoftwareLicenseService/UpdateSoftwareLicense and hands what it keeps to the sink of its kind. */
suspend fun carrySoftwareLicenseId(channel: ManagedChannel) {
    val reader = SoftwareLicenseServiceGrpcKt.SoftwareLicenseServiceCoroutineStub(channel)
    val reply = reader.updateSoftwareLicense(updateSoftwareLicenseRequest { })
    val record = CarrySoftwareLicenseIdRecord(
        softwareLicenseId = reply.softwareLicense.softwareLicenseId,
        message = reply.result.message,
    )
    val sinks = mapOf<String, CarrySoftwareLicenseIdDestination>(
        "delivery-exception-approval" to CarrySoftwareLicenseIdDeliveryExceptionApproval(DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub(channel)),
        "route-workflow" to CarrySoftwareLicenseIdRouteWorkflow(RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel)),
        "inspection-validation" to CarrySoftwareLicenseIdInspectionValidation(InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub(channel)),
        "dock-approval" to CarrySoftwareLicenseIdDockApproval(DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub(channel)),
    )
    routeCarrySoftwareLicenseId(sinks, "route-workflow", record)
}

/** Where carrySoftwareLicenseId hands each record. */
interface CarrySoftwareLicenseIdDestination {
    suspend fun forward(rec: CarrySoftwareLicenseIdRecord)
}

/** What carrySoftwareLicenseId keeps of each UpdateSoftwareLicense response. */
data class CarrySoftwareLicenseIdRecord(val softwareLicenseId: String, val message: String)

/** Hands records to acme.deliveries.v1.DeliveryExceptionApprovalService. */
class CarrySoftwareLicenseIdDeliveryExceptionApproval(private val stub: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub) : CarrySoftwareLicenseIdDestination {
    override suspend fun forward(rec: CarrySoftwareLicenseIdRecord) {
        val request = escalateDeliveryExceptionRequest {
            this.reason = rec.message
            this.deliveryException = deliveryException { this.subject = rec.softwareLicenseId }
        }
        stub.escalateDeliveryException(request)
    }
}

/** Sends records to acme.routing.v1.RouteWorkflowService. */
class CarrySoftwareLicenseIdRouteWorkflow(private val stub: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub) : CarrySoftwareLicenseIdDestination {
    override suspend fun forward(rec: CarrySoftwareLicenseIdRecord) {
        val request = advanceRouteWorkflowRequest {
            this.details = AdvanceRouteWorkflowRequestKt.details { this.step = rec.message }
            this.reason = rec.softwareLicenseId
        }
        stub.advanceRouteWorkflow(request)
    }
}

/** Hands records to acme.fleet.v1.InspectionValidationService. */
class CarrySoftwareLicenseIdInspectionValidation(private val stub: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub) : CarrySoftwareLicenseIdDestination {
    override suspend fun forward(rec: CarrySoftwareLicenseIdRecord) {
        val request = scoreInspectionRequest {
            this.inspectionId = rec.message
            this.inspection = inspection { this.subject = rec.softwareLicenseId }
        }
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/ScoreInspection") { stub.scoreInspection(request) }
    }
}

/** Writes records to acme.yards.v1.DockApprovalService. */
class CarrySoftwareLicenseIdDockApproval(private val stub: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub) : CarrySoftwareLicenseIdDestination {
    override suspend fun forward(rec: CarrySoftwareLicenseIdRecord) {
        val request = rejectDockRequest {
            this.dock = dock { this.code = rec.message; this.dockId = rec.softwareLicenseId }
        }
        stub.rejectDock(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCarrySoftwareLicenseId(sinks: Map<String, CarrySoftwareLicenseIdDestination>, kind: String, rec: CarrySoftwareLicenseIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
