package com.acme.transport.ops.fleetopsbackfill

import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.listDeliveryWorkflowsRequest
import com.acme.licensing.v1.ListSoftwareLicensesRequestKt
import com.acme.licensing.v1.SoftwareLicenseServiceGrpcKt
import com.acme.licensing.v1.listSoftwareLicensesRequest
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryWorkflowService/ListDeliveryWorkflows and hands what it keeps to acme.licensing.v1.SoftwareLicenseService. */
suspend fun mirrorDeliveryWorkflowsMessage(channel: ManagedChannel) {
    val upstream = DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel)
    val current = upstream.listDeliveryWorkflows(listDeliveryWorkflowsRequest { this.orderBy = "primary-mirror-delivery-workflows-message" })
    val record = MirrorDeliveryWorkflowsMessageRecord(
        message = current.result.message,
        nextPageToken = current.pageInfo.nextPageToken,
        correlationId = current.result.correlationId,
    )
    MirrorDeliveryWorkflowsMessageSoftwareLicense(SoftwareLicenseServiceGrpcKt.SoftwareLicenseServiceCoroutineStub(channel)).write(record)
}

/** What mirrorDeliveryWorkflowsMessage keeps of each ListDeliveryWorkflows response. */
data class MirrorDeliveryWorkflowsMessageRecord(val message: String, val nextPageToken: String, val correlationId: String)

/** Passes records to acme.licensing.v1.SoftwareLicenseService. */
class MirrorDeliveryWorkflowsMessageSoftwareLicense(private val stub: SoftwareLicenseServiceGrpcKt.SoftwareLicenseServiceCoroutineStub) {
    suspend fun write(rec: MirrorDeliveryWorkflowsMessageRecord) {
        val request = listSoftwareLicensesRequest {
            this.filter = ListSoftwareLicensesRequestKt.filter { this.query = rec.correlationId; this.region = rec.message }
        }
        stub.listSoftwareLicenses(request)
    }
}
