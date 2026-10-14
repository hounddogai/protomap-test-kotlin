package com.acme.transport.ops.fleetopsreconciler

import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.estimateCustomerSegmentRequest
import com.acme.customers.v1.syncCustomerSegmentsRequest
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.routing.v1.ListStopWorkflowsRequestKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.listStopWorkflowsRequest
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.explainShipmentCalculationRequest
import com.acme.shipping.v1.shipment
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentSyncService/SyncCustomerSegments and hands what it keeps to the sink of its kind. */
suspend fun pushCustomerSegmentsStatus(channel: ManagedChannel) {
    val upstream = CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel)
    val got = upstream.syncCustomerSegments(syncCustomerSegmentsRequest { this.customerSegmentId = "current-push-customer-segments-status" })
    val row = PushCustomerSegmentsStatusRecord(
        status = got.outcome.status,
        correlationId = got.result.correlationId,
    )
    val registry = mutableMapOf<String, PushCustomerSegmentsStatusTarget>()
    for (registration in listOf(
        PushCustomerSegmentsStatusRegistration("shipment-calculation", PushCustomerSegmentsStatusShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel))),
        PushCustomerSegmentsStatusRegistration("customer-segment-calculation", PushCustomerSegmentsStatusCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel))),
        PushCustomerSegmentsStatusRegistration("stop-workflow", PushCustomerSegmentsStatusStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel))),
        PushCustomerSegmentsStatusRegistration("work-order-workflow", PushCustomerSegmentsStatusWorkOrderWorkflow(WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["work-order-workflow"] ?: return
    sink.write(row)
}

/** Where pushCustomerSegmentsStatus hands each record. */
interface PushCustomerSegmentsStatusTarget {
    suspend fun write(rec: PushCustomerSegmentsStatusRecord)
}

/** What pushCustomerSegmentsStatus keeps of each SyncCustomerSegments response. */
data class PushCustomerSegmentsStatusRecord(val status: String, val correlationId: String)

/** Writes records to acme.shipping.v1.ShipmentCalculationService. */
class PushCustomerSegmentsStatusShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : PushCustomerSegmentsStatusTarget {
    override suspend fun write(rec: PushCustomerSegmentsStatusRecord) {
        val request = explainShipmentCalculationRequest {
            this.shipment = shipment { this.shipmentId = rec.status }
            this.reason = rec.correlationId
        }
        stub.explainShipmentCalculation(request)
    }
}

/** Sends records to acme.customers.v1.CustomerSegmentCalculationService. */
class PushCustomerSegmentsStatusCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : PushCustomerSegmentsStatusTarget {
    override suspend fun write(rec: PushCustomerSegmentsStatusRecord) {
        RpcBoundary.run("acme.customers.v1.CustomerSegmentCalculationService/EstimateCustomerSegment") { stub.estimateCustomerSegment(estimateCustomerSegmentRequest { this.reason = rec.correlationId }) }
    }
}

/** Passes records to acme.routing.v1.StopWorkflowService. */
class PushCustomerSegmentsStatusStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : PushCustomerSegmentsStatusTarget {
    override suspend fun write(rec: PushCustomerSegmentsStatusRecord) {
        val request = listStopWorkflowsRequest {
            this.filter = ListStopWorkflowsRequestKt.filter { this.query = rec.correlationId }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/ListStopWorkflows") { stub.listStopWorkflows(request) }
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderWorkflowService. */
class PushCustomerSegmentsStatusWorkOrderWorkflow(private val stub: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub) : PushCustomerSegmentsStatusTarget {
    override suspend fun write(rec: PushCustomerSegmentsStatusRecord) {
        val request = resumeWorkOrderWorkflowRequest {
            this.workOrder = workOrder { this.workOrderId = rec.correlationId }
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderWorkflowService/ResumeWorkOrderWorkflow") { stub.resumeWorkOrderWorkflow(request) }
    }
}

/** A sink and the kind of record it takes. */
class PushCustomerSegmentsStatusRegistration(val kind: String, val sink: PushCustomerSegmentsStatusTarget)
