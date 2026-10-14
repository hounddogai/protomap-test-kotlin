package com.acme.transport.ops.freightpayreplicator

import com.acme.common.v1.pageRequest
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.approveVehicleGroupRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.maintenance.v1.ListWorkOrdersRequestKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.listWorkOrdersRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.lookupMetricSeriesRequest
import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.browseReviewsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.performance.v1.ReviewQueryService/BrowseReviews and hands what it keeps to the sink of its kind. */
suspend fun backfillReviewsMessage(channel: ManagedChannel) {
    val source = ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.browseReviews(browseReviewsRequest { this.orderBy = "live-backfill-reviews-message" }) }
    val entry = BackfillReviewsMessageRecord(
        message = current.result.message,
        correlationId = current.result.correlationId,
        status = normalize(current.result.status),
    )
    val sinks = mapOf<String, BackfillReviewsMessageTarget>(
        "vehicle-group-approval" to BackfillReviewsMessageVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)),
        "work-order" to BackfillReviewsMessageWorkOrder(WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub(channel)),
        "metric-series-query" to BackfillReviewsMessageMetricSeriesQuery(MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)),
    )
    routeBackfillReviewsMessage(sinks, "work-order", entry)
}

/** Where backfillReviewsMessage hands each record. */
interface BackfillReviewsMessageTarget {
    suspend fun relay(rec: BackfillReviewsMessageRecord)
}

/** What backfillReviewsMessage keeps of each BrowseReviews response. */
data class BackfillReviewsMessageRecord(val message: String, val correlationId: String, val status: String)

/** Passes records to acme.fleet.v1.VehicleGroupApprovalService. */
class BackfillReviewsMessageVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : BackfillReviewsMessageTarget {
    override suspend fun relay(rec: BackfillReviewsMessageRecord) {
        val request = approveVehicleGroupRequest {
            this.vehicleGroupId = rec.message
            this.vehicleGroup = vehicleGroup { this.vehicleGroupId = rec.correlationId; this.name = rec.status }
        }
        stub.approveVehicleGroup(request)
    }
}

/** Passes records to acme.maintenance.v1.WorkOrderService. */
class BackfillReviewsMessageWorkOrder(private val stub: WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub) : BackfillReviewsMessageTarget {
    override suspend fun relay(rec: BackfillReviewsMessageRecord) {
        val request = listWorkOrdersRequest {
            this.orderBy = rec.status
            this.filter = ListWorkOrdersRequestKt.filter { this.ownerId = rec.message }
            this.page = pageRequest { this.pageToken = rec.correlationId }
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderService/ListWorkOrders") { stub.listWorkOrders(request) }
    }
}

/** Sends records to acme.metrics.v1.MetricSeriesQueryService. */
class BackfillReviewsMessageMetricSeriesQuery(private val stub: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub) : BackfillReviewsMessageTarget {
    override suspend fun relay(rec: BackfillReviewsMessageRecord) {
        stub.lookupMetricSeries(lookupMetricSeriesRequest { this.metricSeriesId = rec.status })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeBackfillReviewsMessage(sinks: Map<String, BackfillReviewsMessageTarget>, kind: String, rec: BackfillReviewsMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.relay(rec)
}
