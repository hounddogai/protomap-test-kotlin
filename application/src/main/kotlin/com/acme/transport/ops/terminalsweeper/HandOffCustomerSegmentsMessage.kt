package com.acme.transport.ops.terminalsweeper

import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.pullCustomerSegmentsRequest
import com.acme.fleet.v1.SummarizeVehiclesRequestKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.summarizeVehiclesRequest
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.subscriptions.v1.deleteSubscriptionRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.resumeDockWorkflowRequest
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentSyncService/PullCustomerSegments and hands what it keeps to the sink of its kind. */
suspend fun handOffCustomerSegmentsMessage(channel: ManagedChannel) {
    val source = CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.pullCustomerSegments(pullCustomerSegmentsRequest { this.customerSegmentId = "nightly-hand-off-customer-segments-message" }) }
    val record = HandOffCustomerSegmentsMessageRecord(
        message = current.result.message,
        status = current.result.status,
        referenceId = current.outcome.referenceId,
        outcomeMessage = normalize(current.outcome.message),
    )
    val sinks = mapOf<String, HandOffCustomerSegmentsMessageTarget>(
        "subscription" to HandOffCustomerSegmentsMessageSubscription(SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub(channel)),
        "vehicle-query" to HandOffCustomerSegmentsMessageVehicleQuery(VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub(channel)),
        "dock-workflow" to HandOffCustomerSegmentsMessageDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "subscription") continue
        sinks[kind]?.deliver(record)
    }
}

/** Where handOffCustomerSegmentsMessage hands each record. */
interface HandOffCustomerSegmentsMessageTarget {
    suspend fun deliver(rec: HandOffCustomerSegmentsMessageRecord)
}

/** What handOffCustomerSegmentsMessage keeps of each PullCustomerSegments response. */
data class HandOffCustomerSegmentsMessageRecord(val message: String, val status: String, val referenceId: String, val outcomeMessage: String)

/** Hands records to acme.subscriptions.v1.SubscriptionService. */
class HandOffCustomerSegmentsMessageSubscription(private val stub: SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub) : HandOffCustomerSegmentsMessageTarget {
    override suspend fun deliver(rec: HandOffCustomerSegmentsMessageRecord) {
        stub.deleteSubscription(deleteSubscriptionRequest { this.etag = rec.status; this.reason = rec.message })
    }
}

/** Passes records to acme.fleet.v1.VehicleQueryService. */
class HandOffCustomerSegmentsMessageVehicleQuery(private val stub: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub) : HandOffCustomerSegmentsMessageTarget {
    override suspend fun deliver(rec: HandOffCustomerSegmentsMessageRecord) {
        val request = summarizeVehiclesRequest {
            this.details = SummarizeVehiclesRequestKt.details { this.format = rec.status }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleQueryService/SummarizeVehicles") { stub.summarizeVehicles(request) }
    }
}

/** Passes records to acme.yards.v1.DockWorkflowService. */
class HandOffCustomerSegmentsMessageDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : HandOffCustomerSegmentsMessageTarget {
    override suspend fun deliver(rec: HandOffCustomerSegmentsMessageRecord) {
        stub.resumeDockWorkflow(resumeDockWorkflowRequest { this.reason = rec.referenceId })
    }
}
