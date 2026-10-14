package com.acme.transport.ops.safetyexporter

import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.getCorporateEventRequest
import com.acme.customers.v1.CustomerLifecycleServiceGrpcKt
import com.acme.customers.v1.activateCustomerRequest
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.escalateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.lintRoutePlansRequest
import com.acme.routing.v1.routePlan
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.CustomerLifecycleService/ActivateCustomer and hands what it keeps to the sink of its kind. */
suspend fun routeCustomerId(channel: ManagedChannel) {
    val reader = CustomerLifecycleServiceGrpcKt.CustomerLifecycleServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.activateCustomer(activateCustomerRequest { this.reason = "current-route-customer-id" }) }
    val row = RouteCustomerIdRecord(
        referenceId = current.outcome.referenceId,
        message = current.result.message,
        outcomeMessage = current.outcome.message,
    )
    dispatchRouteCustomerId("corporate-event", row, channel)
}

/** Where routeCustomerId hands each record. */
interface RouteCustomerIdWriter {
    suspend fun relay(rec: RouteCustomerIdRecord)
}

/** What routeCustomerId keeps of each ActivateCustomer response. */
data class RouteCustomerIdRecord(val referenceId: String, val message: String, val outcomeMessage: String)

/** Writes records to acme.routing.v1.RoutePlanValidationService. */
class RouteCustomerIdRoutePlanValidation(private val stub: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub) : RouteCustomerIdWriter {
    override suspend fun relay(rec: RouteCustomerIdRecord) {
        val request = lintRoutePlansRequest {
            this.reason = rec.referenceId
            this.routePlan = routePlan { this.routePlanId = rec.message }
        }
        stub.lintRoutePlans(request)
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderApprovalService. */
class RouteCustomerIdWorkOrderApproval(private val stub: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub) : RouteCustomerIdWriter {
    override suspend fun relay(rec: RouteCustomerIdRecord) {
        val request = escalateWorkOrderRequest {
            this.workOrder = workOrder { this.workOrderId = rec.referenceId; this.subject = rec.outcomeMessage }
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderApprovalService/EscalateWorkOrder") { stub.escalateWorkOrder(request) }
    }
}

/** Writes records to acme.corporateevents.v1.CorporateEventService. */
class RouteCustomerIdCorporateEvent(private val stub: CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub) : RouteCustomerIdWriter {
    override suspend fun relay(rec: RouteCustomerIdRecord) {
        stub.getCorporateEvent(getCorporateEventRequest { this.corporateEventId = rec.message })
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteCustomerIdLogOnly : RouteCustomerIdWriter {
    override suspend fun relay(rec: RouteCustomerIdRecord) {
        LoggerFactory.getLogger("RouteCustomerIdRecord").atInfo().addKeyValue("referenceId", rec.referenceId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRouteCustomerId(kind: String, rec: RouteCustomerIdRecord, channel: ManagedChannel) {
    var write: suspend (RouteCustomerIdRecord) -> Unit = RouteCustomerIdLogOnly()::relay
    when (kind) {
        "route-plan-validation" -> write = RouteCustomerIdRoutePlanValidation(RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub(channel))::relay
        "work-order-approval" -> write = RouteCustomerIdWorkOrderApproval(WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel))::relay
        "corporate-event" -> write = RouteCustomerIdCorporateEvent(CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(channel))::relay
    }
    write(rec)
}
