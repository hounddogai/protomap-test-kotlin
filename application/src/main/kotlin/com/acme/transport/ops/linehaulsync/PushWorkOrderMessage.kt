package com.acme.transport.ops.linehaulsync

import com.acme.boxoffice.v1.BoxOfficeSaleServiceGrpcKt
import com.acme.boxoffice.v1.SearchBoxOfficeSalesRequestKt
import com.acme.boxoffice.v1.searchBoxOfficeSalesRequest
import com.acme.common.v1.pageRequest
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.ListDispatchAssignmentWorkflowsRequestKt
import com.acme.dispatch.v1.listDispatchAssignmentWorkflowsRequest
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.getWorkOrderRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.CountBuildingsRequestKt
import com.acme.workplaces.v1.countBuildingsRequest
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderService/GetWorkOrder and hands what it keeps to the sink of its kind. */
suspend fun pushWorkOrderMessage(channel: ManagedChannel) {
    val origin = WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.getWorkOrder(getWorkOrderRequest { this.workOrderId = "main-push-work-order-message" }) }
    val row = PushWorkOrderMessageRecord(
        message = found.result.message,
        subject = found.workOrder.subject,
        status = normalize(found.result.status),
        correlationId = found.result.correlationId,
    )
    val registry = mutableMapOf<String, PushWorkOrderMessageWriter>()
    for (registration in listOf(
        PushWorkOrderMessageRegistration("building-query", PushWorkOrderMessageBuildingQuery(BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub(channel))),
        PushWorkOrderMessageRegistration("dispatch-assignment-workflow", PushWorkOrderMessageDispatchAssignmentWorkflow(DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub(channel))),
        PushWorkOrderMessageRegistration("box-office-sale", PushWorkOrderMessageBoxOfficeSale(BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["box-office-sale"] ?: return
    sink.transmit(row)
}

/** Where pushWorkOrderMessage hands each record. */
interface PushWorkOrderMessageWriter {
    suspend fun transmit(rec: PushWorkOrderMessageRecord)
}

/** What pushWorkOrderMessage keeps of each GetWorkOrder response. */
data class PushWorkOrderMessageRecord(val message: String, val subject: String, val status: String, val correlationId: String)

/** Writes records to acme.workplaces.v1.BuildingQueryService. */
class PushWorkOrderMessageBuildingQuery(private val stub: BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub) : PushWorkOrderMessageWriter {
    override suspend fun transmit(rec: PushWorkOrderMessageRecord) {
        val request = countBuildingsRequest {
            this.filter = CountBuildingsRequestKt.filter { this.query = rec.message }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.countBuildings(request)
    }
}

/** Sends records to acme.dispatch.v1.DispatchAssignmentWorkflowService. */
class PushWorkOrderMessageDispatchAssignmentWorkflow(private val stub: DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub) : PushWorkOrderMessageWriter {
    override suspend fun transmit(rec: PushWorkOrderMessageRecord) {
        val request = listDispatchAssignmentWorkflowsRequest {
            this.filter = ListDispatchAssignmentWorkflowsRequestKt.filter { this.region = rec.subject }
            this.page = pageRequest { this.pageToken = rec.correlationId }
        }
        stub.listDispatchAssignmentWorkflows(request)
    }
}

/** Hands records to acme.boxoffice.v1.BoxOfficeSaleService. */
class PushWorkOrderMessageBoxOfficeSale(private val stub: BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub) : PushWorkOrderMessageWriter {
    override suspend fun transmit(rec: PushWorkOrderMessageRecord) {
        val request = searchBoxOfficeSalesRequest {
            this.orderBy = rec.subject
            this.filter = SearchBoxOfficeSalesRequestKt.filter { this.ownerId = rec.correlationId }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.searchBoxOfficeSales(request)
    }
}

/** A sink and the kind of record it takes. */
class PushWorkOrderMessageRegistration(val kind: String, val sink: PushWorkOrderMessageWriter)
