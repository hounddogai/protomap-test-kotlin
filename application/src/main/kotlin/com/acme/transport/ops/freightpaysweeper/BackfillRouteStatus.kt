package com.acme.transport.ops.freightpaysweeper

import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.delegateRoutePlanApprovalRequest
import com.acme.routing.v1.escalateRouteRequest
import com.acme.routing.v1.routePlan
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.estimateShowRequest
import com.acme.shows.v1.show
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.RouteApprovalService/EscalateRoute and hands what it keeps to the sink of its kind. */
suspend fun backfillRouteStatus(channel: ManagedChannel) {
    val reader = RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel)
    val current = reader.escalateRoute(escalateRouteRequest { this.reason = "live-backfill-route-status" })
    val entry = BackfillRouteStatusRecord(
        status = current.outcome.status,
        message = current.outcome.message,
    )
    deliverBackfillRouteStatus("route-plan-approval", entry, channel)
}

/** Where backfillRouteStatus hands each record. */
interface BackfillRouteStatusTarget {
    suspend fun write(rec: BackfillRouteStatusRecord)
}

/** What backfillRouteStatus keeps of each EscalateRoute response. */
data class BackfillRouteStatusRecord(val status: String, val message: String)

/** Passes records to acme.shows.v1.ShowCalculationService. */
class BackfillRouteStatusShowCalculation(private val stub: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub) : BackfillRouteStatusTarget {
    override suspend fun write(rec: BackfillRouteStatusRecord) {
        stub.estimateShow(estimateShowRequest { this.show = show { this.title = rec.status } })
    }
}

/** Writes records to acme.routing.v1.RoutePlanApprovalService. */
class BackfillRouteStatusRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : BackfillRouteStatusTarget {
    override suspend fun write(rec: BackfillRouteStatusRecord) {
        val request = delegateRoutePlanApprovalRequest {
            this.routePlanId = rec.status
            this.routePlan = routePlan { this.notes = rec.message }
        }
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/DelegateRoutePlanApproval") { stub.delegateRoutePlanApproval(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillRouteStatusLogOnly : BackfillRouteStatusTarget {
    override suspend fun write(rec: BackfillRouteStatusRecord) {
        LoggerFactory.getLogger("BackfillRouteStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillRouteStatus(kind: String, rec: BackfillRouteStatusRecord, channel: ManagedChannel) {
    var sink: BackfillRouteStatusTarget = BackfillRouteStatusLogOnly()
    when (kind) {
        "show-calculation" -> sink = BackfillRouteStatusShowCalculation(ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel))
        "route-plan-approval" -> sink = BackfillRouteStatusRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
