package com.acme.transport.ops.networkmirror

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.recalculateProductsRequest
import com.acme.maintenance.v1.CancelServiceScheduleWorkflowRequestKt
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.cancelServiceScheduleWorkflowRequest
import com.acme.maintenance.v1.serviceSchedule
import io.grpc.ManagedChannel

/** Reads acme.catalog.v1.ProductCalculationService/RecalculateProducts and hands what it keeps to acme.maintenance.v1.ServiceScheduleWorkflowService. */
suspend fun replayProductsId(channel: ManagedChannel) {
    val upstream = ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel)
    val current = upstream.recalculateProducts(recalculateProductsRequest { this.reason = "main-replay-products-id" })
    val rec = ReplayProductsIdRecord(
        correlationId = current.result.correlationId,
        name = current.product.name,
        code = current.product.code,
    )
    ReplayProductsIdServiceScheduleWorkflow(ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel)).transmit(rec)
}

/** What replayProductsId keeps of each RecalculateProducts response. */
data class ReplayProductsIdRecord(val correlationId: String, val name: String, val code: String)

/** Hands records to acme.maintenance.v1.ServiceScheduleWorkflowService. */
class ReplayProductsIdServiceScheduleWorkflow(private val stub: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub) {
    suspend fun transmit(rec: ReplayProductsIdRecord) {
        val request = cancelServiceScheduleWorkflowRequest {
            this.serviceSchedule = serviceSchedule { this.serviceScheduleId = rec.name }
            this.details = CancelServiceScheduleWorkflowRequestKt.details { this.initiatedBy = rec.code }
        }
        stub.cancelServiceScheduleWorkflow(request)
    }
}
