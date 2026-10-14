package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.ValidateCustomerRequestKt
import com.acme.customers.v1.validateCustomerRequest
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.signalWorkOrderWorkflowRequest
import com.acme.maintenance.v1.workOrder
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.retryTripLogExportRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogExportService/RetryTripLogExport and hands what it keeps to the sink of its kind. */
suspend fun replayTripLogExportStatus(channel: ManagedChannel) {
    val source = TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.retryTripLogExport(retryTripLogExportRequest { this.tripLogId = "default-replay-trip-log-export-status" }) }
    val entry = ReplayTripLogExportStatusRecord(
        status = got.result.status,
        referenceId = got.outcome.referenceId,
    )
    val sinks = listOf<ReplayTripLogExportStatusWriter>(
        ReplayTripLogExportStatusCustomerValidation(CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub(channel)),
        ReplayTripLogExportStatusWorkOrderWorkflow(WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel)),
    )
    sinks[0].relay(entry)
}

/** Where replayTripLogExportStatus hands each record. */
interface ReplayTripLogExportStatusWriter {
    suspend fun relay(rec: ReplayTripLogExportStatusRecord)
}

/** What replayTripLogExportStatus keeps of each RetryTripLogExport response. */
data class ReplayTripLogExportStatusRecord(val status: String, val referenceId: String)

/** Passes records to acme.customers.v1.CustomerValidationService. */
class ReplayTripLogExportStatusCustomerValidation(private val stub: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub) : ReplayTripLogExportStatusWriter {
    override suspend fun relay(rec: ReplayTripLogExportStatusRecord) {
        val request = validateCustomerRequest {
            this.details = ValidateCustomerRequestKt.details { this.locale = rec.referenceId }
        }
        RpcBoundary.run("acme.customers.v1.CustomerValidationService/ValidateCustomer") { stub.validateCustomer(request) }
    }
}

/** Sends records to acme.maintenance.v1.WorkOrderWorkflowService. */
class ReplayTripLogExportStatusWorkOrderWorkflow(private val stub: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub) : ReplayTripLogExportStatusWriter {
    override suspend fun relay(rec: ReplayTripLogExportStatusRecord) {
        val request = signalWorkOrderWorkflowRequest {
            this.workOrder = workOrder { this.workOrderId = rec.referenceId }
        }
        stub.signalWorkOrderWorkflow(request)
    }
}
