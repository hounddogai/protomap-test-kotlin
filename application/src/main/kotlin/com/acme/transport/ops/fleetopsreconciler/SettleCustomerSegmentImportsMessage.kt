package com.acme.transport.ops.fleetopsreconciler

import com.acme.customers.v1.CustomerSegmentImportServiceGrpcKt
import com.acme.customers.v1.listCustomerSegmentImportsRequest
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.TerminateGeoFenceRequestKt
import com.acme.telematics.v1.terminateGeoFenceRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentImportService/ListCustomerSegmentImports and hands what it keeps to acme.telematics.v1.GeoFenceLifecycleService. */
suspend fun settleCustomerSegmentImportsMessage(channel: ManagedChannel) {
    val source = CustomerSegmentImportServiceGrpcKt.CustomerSegmentImportServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.listCustomerSegmentImports(listCustomerSegmentImportsRequest { this.orderBy = "primary-settle-customer-segment-imports-message" }) }
    val entry = SettleCustomerSegmentImportsMessageRecord(
        message = current.result.message,
        correlationId = current.result.correlationId,
    )
    SettleCustomerSegmentImportsMessageGeoFenceLifecycle(GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel)).deliver(entry)
}

/** What settleCustomerSegmentImportsMessage keeps of each ListCustomerSegmentImports response. */
data class SettleCustomerSegmentImportsMessageRecord(val message: String, val correlationId: String)

/** Passes records to acme.telematics.v1.GeoFenceLifecycleService. */
class SettleCustomerSegmentImportsMessageGeoFenceLifecycle(private val stub: GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub) {
    suspend fun deliver(rec: SettleCustomerSegmentImportsMessageRecord) {
        val request = terminateGeoFenceRequest {
            this.reason = rec.message
            this.details = TerminateGeoFenceRequestKt.details { this.reasonCode = rec.correlationId }
        }
        stub.terminateGeoFence(request)
    }
}
