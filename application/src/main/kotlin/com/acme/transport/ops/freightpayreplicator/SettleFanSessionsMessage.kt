package com.acme.transport.ops.freightpayreplicator

import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.pauseDispatchOrderWorkflowRequest
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.findFanSessionsRequest
import com.acme.routing.v1.RecallRouteRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.recallRouteRequest
import com.acme.routing.v1.route
import com.acme.telematics.v1.SyncTripLogsRequestKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.syncTripLogsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fanportal.v1.FanSessionQueryService/FindFanSessions and hands what it keeps to the sink of its kind. */
suspend fun settleFanSessionsMessage(channel: ManagedChannel) {
    val origin = FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.findFanSessions(findFanSessionsRequest { this.orderBy = "live-settle-fan-sessions-message" }) }
    val record = SettleFanSessionsMessageRecord(
        message = current.result.message,
        correlationId = normalize(current.result.correlationId),
        status = current.result.status,
    )
    deliverSettleFanSessionsMessage("trip-log-sync", record, channel)
}

/** Where settleFanSessionsMessage hands each record. */
interface SettleFanSessionsMessageSink {
    suspend fun hand(rec: SettleFanSessionsMessageRecord)
}

/** What settleFanSessionsMessage keeps of each FindFanSessions response. */
data class SettleFanSessionsMessageRecord(val message: String, val correlationId: String, val status: String)

/** Passes records to acme.routing.v1.RouteApprovalService. */
class SettleFanSessionsMessageRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : SettleFanSessionsMessageSink {
    override suspend fun hand(rec: SettleFanSessionsMessageRecord) {
        val request = recallRouteRequest {
            this.reason = rec.status
            this.details = RecallRouteRequestKt.details { this.approverEmail = rec.message }
            this.route = route { this.notes = rec.correlationId }
        }
        stub.recallRoute(request)
    }
}

/** Writes records to acme.telematics.v1.TripLogSyncService. */
class SettleFanSessionsMessageTripLogSync(private val stub: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub) : SettleFanSessionsMessageSink {
    override suspend fun hand(rec: SettleFanSessionsMessageRecord) {
        val request = syncTripLogsRequest {
            this.details = SyncTripLogsRequestKt.details { this.checksum = rec.message; this.resolution = rec.status }
        }
        stub.syncTripLogs(request)
    }
}

/** Writes records to acme.dispatch.v1.DispatchOrderWorkflowService. */
class SettleFanSessionsMessageDispatchOrderWorkflow(private val stub: DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub) : SettleFanSessionsMessageSink {
    override suspend fun hand(rec: SettleFanSessionsMessageRecord) {
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderWorkflowService/PauseDispatchOrderWorkflow") { stub.pauseDispatchOrderWorkflow(pauseDispatchOrderWorkflowRequest { this.dispatchOrderId = rec.message }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleFanSessionsMessageLogOnly : SettleFanSessionsMessageSink {
    override suspend fun hand(rec: SettleFanSessionsMessageRecord) {
        LoggerFactory.getLogger("SettleFanSessionsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSettleFanSessionsMessage(kind: String, rec: SettleFanSessionsMessageRecord, channel: ManagedChannel) {
    var sink: SettleFanSessionsMessageSink = SettleFanSessionsMessageLogOnly()
    when (kind) {
        "route-approval" -> sink = SettleFanSessionsMessageRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel))
        "trip-log-sync" -> sink = SettleFanSessionsMessageTripLogSync(TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub(channel))
        "dispatch-order-workflow" -> sink = SettleFanSessionsMessageDispatchOrderWorkflow(DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
