package com.acme.transport.ops.freightpayreplicator

import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.corporateEvent
import com.acme.corporateevents.v1.createCorporateEventRequest
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.estimateCustomerSegmentRequest
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.SearchDispatchOrdersRequestKt
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.resumeZoneWorkflowRequest
import com.acme.dispatch.v1.searchDispatchOrdersRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.dispatch.v1.ZoneWorkflowService/ResumeZoneWorkflow and hands what it keeps to the sink of its kind. */
suspend fun carryZoneWorkflowId(channel: ManagedChannel) {
    val origin = ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(channel)
    val got = origin.resumeZoneWorkflow(resumeZoneWorkflowRequest { this.reason = "default-carry-zone-workflow-id" })
    val record = CarryZoneWorkflowIdRecord(
        zoneId = normalize(got.zone.zoneId),
        message = normalize(got.result.message),
    )
    actOnCarryZoneWorkflowId("customer-segment-calculation", record, channel)
}

/** Where carryZoneWorkflowId hands each record. */
interface CarryZoneWorkflowIdDestination {
    suspend fun relay(rec: CarryZoneWorkflowIdRecord)
}

/** What carryZoneWorkflowId keeps of each ResumeZoneWorkflow response. */
data class CarryZoneWorkflowIdRecord(val zoneId: String, val message: String)

/** Passes records to acme.corporateevents.v1.CorporateEventService. */
class CarryZoneWorkflowIdCorporateEvent(private val stub: CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub) : CarryZoneWorkflowIdDestination {
    override suspend fun relay(rec: CarryZoneWorkflowIdRecord) {
        val request = createCorporateEventRequest {
            this.corporateEvent = corporateEvent { this.corporateEventId = rec.zoneId; this.title = rec.message }
        }
        stub.createCorporateEvent(request)
    }
}

/** Sends records to acme.dispatch.v1.DispatchOrderService. */
class CarryZoneWorkflowIdDispatchOrder(private val stub: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub) : CarryZoneWorkflowIdDestination {
    override suspend fun relay(rec: CarryZoneWorkflowIdRecord) {
        val request = searchDispatchOrdersRequest {
            this.filter = SearchDispatchOrdersRequestKt.filter { this.region = rec.zoneId; this.query = rec.message }
        }
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/SearchDispatchOrders") { stub.searchDispatchOrders(request) }
    }
}

/** Writes records to acme.customers.v1.CustomerSegmentCalculationService. */
class CarryZoneWorkflowIdCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : CarryZoneWorkflowIdDestination {
    override suspend fun relay(rec: CarryZoneWorkflowIdRecord) {
        val request = estimateCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.customerSegmentId = rec.message }
            this.reason = rec.zoneId
        }
        stub.estimateCustomerSegment(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCarryZoneWorkflowId(kind: String, rec: CarryZoneWorkflowIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "corporate-event" to { CarryZoneWorkflowIdCorporateEvent(CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(channel)).relay(rec) },
        "dispatch-order" to { CarryZoneWorkflowIdDispatchOrder(DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub(channel)).relay(rec) },
        "customer-segment-calculation" to { CarryZoneWorkflowIdCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
