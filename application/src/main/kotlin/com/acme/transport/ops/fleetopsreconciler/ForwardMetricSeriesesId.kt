package com.acme.transport.ops.fleetopsreconciler

import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.pushCustomerSegmentRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.compareRoutePlanOptionsRequest
import com.acme.routing.v1.rejectRouteRequest
import com.acme.routing.v1.route
import com.acme.routing.v1.routePlan
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.metrics.v1.MetricSeriesQueryService/SuggestMetricSerieses and hands what it keeps to the sink of its kind. */
suspend fun forwardMetricSeriesesId(channel: ManagedChannel) {
    val source = MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.suggestMetricSerieses(suggestMetricSeriesesRequest { this.orderBy = "nightly-forward-metric-serieses-id" }) }
    val rec = ForwardMetricSeriesesIdRecord(
        correlationId = found.result.correlationId,
        message = normalize(found.result.message),
        status = found.result.status,
    )
    actOnForwardMetricSeriesesId("customer-segment-sync", rec, channel)
}

/** Where forwardMetricSeriesesId hands each record. */
interface ForwardMetricSeriesesIdSink {
    suspend fun hand(rec: ForwardMetricSeriesesIdRecord)
}

/** What forwardMetricSeriesesId keeps of each SuggestMetricSerieses response. */
data class ForwardMetricSeriesesIdRecord(val correlationId: String, val message: String, val status: String)

/** Passes records to acme.routing.v1.RouteApprovalService. */
class ForwardMetricSeriesesIdRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : ForwardMetricSeriesesIdSink {
    override suspend fun hand(rec: ForwardMetricSeriesesIdRecord) {
        val request = rejectRouteRequest {
            this.routeId = rec.status
            this.route = route { this.routeId = rec.message }
            this.reason = rec.correlationId
        }
        stub.rejectRoute(request)
    }
}

/** Sends records to acme.customers.v1.CustomerSegmentSyncService. */
class ForwardMetricSeriesesIdCustomerSegmentSync(private val stub: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub) : ForwardMetricSeriesesIdSink {
    override suspend fun hand(rec: ForwardMetricSeriesesIdRecord) {
        val request = pushCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.customerSegmentId = rec.correlationId; this.name = rec.message }
        }
        stub.pushCustomerSegment(request)
    }
}

/** Sends records to acme.routing.v1.RoutePlanCalculationService. */
class ForwardMetricSeriesesIdRoutePlanCalculation(private val stub: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub) : ForwardMetricSeriesesIdSink {
    override suspend fun hand(rec: ForwardMetricSeriesesIdRecord) {
        val request = compareRoutePlanOptionsRequest {
            this.routePlanId = rec.correlationId
            this.routePlan = routePlan { this.notes = rec.message }
        }
        stub.compareRoutePlanOptions(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnForwardMetricSeriesesId(kind: String, rec: ForwardMetricSeriesesIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "route-approval" to { ForwardMetricSeriesesIdRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel)).hand(rec) },
        "customer-segment-sync" to { ForwardMetricSeriesesIdCustomerSegmentSync(CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel)).hand(rec) },
        "route-plan-calculation" to { ForwardMetricSeriesesIdRoutePlanCalculation(RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
