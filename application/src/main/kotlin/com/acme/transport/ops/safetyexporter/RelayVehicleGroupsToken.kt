package com.acme.transport.ops.safetyexporter

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.customers.v1.CancelHouseholdWorkflowRequestKt
import com.acme.customers.v1.HouseholdWorkflowServiceGrpcKt
import com.acme.customers.v1.cancelHouseholdWorkflowRequest
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.findVehicleGroupsRequest
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.escalateWorkOrderRequest
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.explainShipmentCalculationRequest
import com.acme.shipping.v1.shipment
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleGroupQueryService/FindVehicleGroups and hands what it keeps to the sink of its kind. */
suspend fun relayVehicleGroupsToken(channel: ManagedChannel) {
    val source = VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineStub(channel)
    val got = source.findVehicleGroups(findVehicleGroupsRequest { this.orderBy = "current-relay-vehicle-groups-token" })
    val entry = RelayVehicleGroupsTokenRecord(
        nextPageToken = got.pageInfo.nextPageToken,
        status = got.result.status,
        message = got.result.message,
    )
    val sinks = mapOf<String, RelayVehicleGroupsTokenDestination>(
        "audit-event-validation" to RelayVehicleGroupsTokenAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        "household-workflow" to RelayVehicleGroupsTokenHouseholdWorkflow(HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub(channel)),
        "shipment-calculation" to RelayVehicleGroupsTokenShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel)),
        "work-order-approval" to RelayVehicleGroupsTokenWorkOrderApproval(WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "shipment-calculation") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where relayVehicleGroupsToken hands each record. */
interface RelayVehicleGroupsTokenDestination {
    suspend fun deliver(rec: RelayVehicleGroupsTokenRecord)
}

/** What relayVehicleGroupsToken keeps of each FindVehicleGroups response. */
data class RelayVehicleGroupsTokenRecord(val nextPageToken: String, val status: String, val message: String)

/** Passes records to acme.audit.v1.AuditEventValidationService. */
class RelayVehicleGroupsTokenAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : RelayVehicleGroupsTokenDestination {
    override suspend fun deliver(rec: RelayVehicleGroupsTokenRecord) {
        val request = previewAuditEventRequest {
            this.auditEvent = auditEvent { this.source = rec.status; this.kind = rec.message }
            this.auditEventId = rec.nextPageToken
        }
        stub.previewAuditEvent(request)
    }
}

/** Sends records to acme.customers.v1.HouseholdWorkflowService. */
class RelayVehicleGroupsTokenHouseholdWorkflow(private val stub: HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub) : RelayVehicleGroupsTokenDestination {
    override suspend fun deliver(rec: RelayVehicleGroupsTokenRecord) {
        val request = cancelHouseholdWorkflowRequest {
            this.details = CancelHouseholdWorkflowRequestKt.details { this.step = rec.message }
        }
        stub.cancelHouseholdWorkflow(request)
    }
}

/** Hands records to acme.shipping.v1.ShipmentCalculationService. */
class RelayVehicleGroupsTokenShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : RelayVehicleGroupsTokenDestination {
    override suspend fun deliver(rec: RelayVehicleGroupsTokenRecord) {
        val request = explainShipmentCalculationRequest {
            this.shipment = shipment { this.notes = rec.message; this.shipmentId = rec.status }
        }
        stub.explainShipmentCalculation(request)
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderApprovalService. */
class RelayVehicleGroupsTokenWorkOrderApproval(private val stub: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub) : RelayVehicleGroupsTokenDestination {
    override suspend fun deliver(rec: RelayVehicleGroupsTokenRecord) {
        stub.escalateWorkOrder(escalateWorkOrderRequest { this.reason = rec.status })
    }
}
