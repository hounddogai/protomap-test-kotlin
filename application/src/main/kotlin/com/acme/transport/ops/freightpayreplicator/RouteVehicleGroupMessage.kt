package com.acme.transport.ops.freightpayreplicator

import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.calculateCorporateEventRequest
import com.acme.corporateevents.v1.corporateEvent
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.retryCustomerWorkflowRequest
import com.acme.dispatch.v1.AdvanceZoneWorkflowRequestKt
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.advanceZoneWorkflowRequest
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.previewVehicleGroupRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.VehicleGroupValidationService/PreviewVehicleGroup and hands what it keeps to the sink of its kind. */
suspend fun routeVehicleGroupMessage(channel: ManagedChannel) {
    val origin = VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.previewVehicleGroup(previewVehicleGroupRequest { this.vehicleGroupId = "live-route-vehicle-group-message" }) }
    val entry = RouteVehicleGroupMessageRecord(
        message = normalize(got.result.message),
        referenceId = normalize(got.outcome.referenceId),
        outcomeMessage = got.outcome.message,
        vehicleGroupId = got.vehicleGroup.vehicleGroupId,
    )
    dispatchRouteVehicleGroupMessage("zone-workflow", entry, channel)
}

/** Where routeVehicleGroupMessage hands each record. */
interface RouteVehicleGroupMessageWriter {
    suspend fun write(rec: RouteVehicleGroupMessageRecord)
}

/** What routeVehicleGroupMessage keeps of each PreviewVehicleGroup response. */
data class RouteVehicleGroupMessageRecord(val message: String, val referenceId: String, val outcomeMessage: String, val vehicleGroupId: String)

/** Writes records to acme.dispatch.v1.ZoneWorkflowService. */
class RouteVehicleGroupMessageZoneWorkflow(private val stub: ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub) : RouteVehicleGroupMessageWriter {
    override suspend fun write(rec: RouteVehicleGroupMessageRecord) {
        val request = advanceZoneWorkflowRequest {
            this.details = AdvanceZoneWorkflowRequestKt.details { this.step = rec.outcomeMessage }
            this.zoneId = rec.vehicleGroupId
        }
        stub.advanceZoneWorkflow(request)
    }
}

/** Writes records to acme.corporateevents.v1.CorporateEventCalculationService. */
class RouteVehicleGroupMessageCorporateEventCalculation(private val stub: CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub) : RouteVehicleGroupMessageWriter {
    override suspend fun write(rec: RouteVehicleGroupMessageRecord) {
        val request = calculateCorporateEventRequest {
            this.reason = rec.vehicleGroupId
            this.corporateEvent = corporateEvent { this.corporateEventId = rec.message }
            this.corporateEventId = rec.referenceId
        }
        RpcBoundary.run("acme.corporateevents.v1.CorporateEventCalculationService/CalculateCorporateEvent") { stub.calculateCorporateEvent(request) }
    }
}

/** Writes records to acme.customers.v1.CustomerWorkflowService. */
class RouteVehicleGroupMessageCustomerWorkflow(private val stub: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub) : RouteVehicleGroupMessageWriter {
    override suspend fun write(rec: RouteVehicleGroupMessageRecord) {
        val request = retryCustomerWorkflowRequest {
            this.reason = rec.vehicleGroupId
            this.customerId = rec.outcomeMessage
        }
        RpcBoundary.run("acme.customers.v1.CustomerWorkflowService/RetryCustomerWorkflow") { stub.retryCustomerWorkflow(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteVehicleGroupMessageLogOnly : RouteVehicleGroupMessageWriter {
    override suspend fun write(rec: RouteVehicleGroupMessageRecord) {
        LoggerFactory.getLogger("RouteVehicleGroupMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRouteVehicleGroupMessage(kind: String, rec: RouteVehicleGroupMessageRecord, channel: ManagedChannel) {
    var write: suspend (RouteVehicleGroupMessageRecord) -> Unit = RouteVehicleGroupMessageLogOnly()::write
    when (kind) {
        "zone-workflow" -> write = RouteVehicleGroupMessageZoneWorkflow(ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(channel))::write
        "corporate-event-calculation" -> write = RouteVehicleGroupMessageCorporateEventCalculation(CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(channel))::write
        "customer-workflow" -> write = RouteVehicleGroupMessageCustomerWorkflow(CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub(channel))::write
    }
    write(rec)
}
