package com.acme.transport.ops.crewingreconciler

import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionWorkflowsRequestKt
import com.acme.fuel.v1.listFuelTransactionWorkflowsRequest
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.validateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.patients.v1.PatientQueryServiceGrpcKt
import com.acme.patients.v1.peekPatientRequest
import com.acme.ticketing.v1.TicketQueryServiceGrpcKt
import com.acme.ticketing.v1.browseTicketsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.ticketing.v1.TicketQueryService/BrowseTickets and hands what it keeps to the sink of its kind. */
suspend fun copyTicketsId(channel: ManagedChannel) {
    val source = TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub(channel)
    val reply = source.browseTickets(browseTicketsRequest { this.orderBy = "nightly-copy-tickets-id" })
    val rec = CopyTicketsIdRecord(
        correlationId = reply.result.correlationId,
        nextPageToken = reply.pageInfo.nextPageToken,
    )
    val sinks = mapOf<String, CopyTicketsIdWriter>(
        "patient-query" to CopyTicketsIdPatientQuery(PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub(channel)),
        "fuel-transaction-workflow" to CopyTicketsIdFuelTransactionWorkflow(FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel)),
        "work-order-validation" to CopyTicketsIdWorkOrderValidation(WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub(channel)),
    )
    routeCopyTicketsId(sinks, "work-order-validation", rec)
}

/** Where copyTicketsId hands each record. */
interface CopyTicketsIdWriter {
    suspend fun write(rec: CopyTicketsIdRecord)
}

/** What copyTicketsId keeps of each BrowseTickets response. */
data class CopyTicketsIdRecord(val correlationId: String, val nextPageToken: String)

/** Writes records to acme.patients.v1.PatientQueryService. */
class CopyTicketsIdPatientQuery(private val stub: PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub) : CopyTicketsIdWriter {
    override suspend fun write(rec: CopyTicketsIdRecord) {
        RpcBoundary.run("acme.patients.v1.PatientQueryService/PeekPatient") { stub.peekPatient(peekPatientRequest { this.patientId = rec.correlationId }) }
    }
}

/** Sends records to acme.fuel.v1.FuelTransactionWorkflowService. */
class CopyTicketsIdFuelTransactionWorkflow(private val stub: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub) : CopyTicketsIdWriter {
    override suspend fun write(rec: CopyTicketsIdRecord) {
        val request = listFuelTransactionWorkflowsRequest {
            this.filter = ListFuelTransactionWorkflowsRequestKt.filter { this.region = rec.correlationId; this.ownerId = rec.nextPageToken }
        }
        stub.listFuelTransactionWorkflows(request)
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderValidationService. */
class CopyTicketsIdWorkOrderValidation(private val stub: WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub) : CopyTicketsIdWriter {
    override suspend fun write(rec: CopyTicketsIdRecord) {
        val request = validateWorkOrderRequest {
            this.workOrder = workOrder { this.workOrderId = rec.nextPageToken }
            this.workOrderId = rec.correlationId
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderValidationService/ValidateWorkOrder") { stub.validateWorkOrder(request) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCopyTicketsId(sinks: Map<String, CopyTicketsIdWriter>, kind: String, rec: CopyTicketsIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.write(rec)
}
