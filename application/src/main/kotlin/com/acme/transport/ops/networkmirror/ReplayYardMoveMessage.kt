package com.acme.transport.ops.networkmirror

import com.acme.common.v1.pageRequest
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.normalizeZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.recalculateVehicleGroupsRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.SubmitRoutePlanRequestKt
import com.acme.routing.v1.submitRoutePlanRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.rejectYardMoveRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.YardMoveApprovalService/RejectYardMove and hands what it keeps to the sink of its kind. */
suspend fun replayYardMoveMessage(channel: ManagedChannel) {
    val source = YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(channel)
    val reply = source.rejectYardMove(rejectYardMoveRequest { this.yardMoveId = "main-replay-yard-move-message" })
    val rec = ReplayYardMoveMessageRecord(
        message = reply.result.message,
        subject = reply.yardMove.subject,
        status = reply.outcome.status,
        referenceId = reply.outcome.referenceId,
    )
    deliverReplayYardMoveMessage("route-plan-approval", rec, channel)
}

/** Where replayYardMoveMessage hands each record. */
interface ReplayYardMoveMessageTarget {
    suspend fun forward(rec: ReplayYardMoveMessageRecord)
}

/** What replayYardMoveMessage keeps of each RejectYardMove response. */
data class ReplayYardMoveMessageRecord(val message: String, val subject: String, val status: String, val referenceId: String)

/** Sends records to acme.dispatch.v1.ZoneValidationService. */
class ReplayYardMoveMessageZoneValidation(private val stub: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub) : ReplayYardMoveMessageTarget {
    override suspend fun forward(rec: ReplayYardMoveMessageRecord) {
        val request = normalizeZoneRequest {
            this.zone = zone { this.parentId = rec.referenceId; this.name = rec.subject; this.zoneId = rec.status }
        }
        stub.normalizeZone(request)
    }
}

/** Writes records to acme.fuel.v1.FuelTransactionQueryService. */
class ReplayYardMoveMessageFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : ReplayYardMoveMessageTarget {
    override suspend fun forward(rec: ReplayYardMoveMessageRecord) {
        val request = browseFuelTransactionsRequest {
            this.page = pageRequest { this.pageToken = rec.referenceId }
        }
        stub.browseFuelTransactions(request)
    }
}

/** Sends records to acme.routing.v1.RoutePlanApprovalService. */
class ReplayYardMoveMessageRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : ReplayYardMoveMessageTarget {
    override suspend fun forward(rec: ReplayYardMoveMessageRecord) {
        val request = submitRoutePlanRequest {
            this.routePlanId = rec.status
            this.details = SubmitRoutePlanRequestKt.details { this.approverEmail = rec.referenceId }
        }
        RpcBoundary.run("acme.routing.v1.RoutePlanApprovalService/SubmitRoutePlan") { stub.submitRoutePlan(request) }
    }
}

/** Hands records to acme.fleet.v1.VehicleGroupCalculationService. */
class ReplayYardMoveMessageVehicleGroupCalculation(private val stub: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub) : ReplayYardMoveMessageTarget {
    override suspend fun forward(rec: ReplayYardMoveMessageRecord) {
        val request = recalculateVehicleGroupsRequest {
            this.vehicleGroup = vehicleGroup { this.parentId = rec.message; this.name = rec.referenceId }
            this.reason = rec.subject
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupCalculationService/RecalculateVehicleGroups") { stub.recalculateVehicleGroups(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayYardMoveMessageLogOnly : ReplayYardMoveMessageTarget {
    override suspend fun forward(rec: ReplayYardMoveMessageRecord) {
        LoggerFactory.getLogger("ReplayYardMoveMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverReplayYardMoveMessage(kind: String, rec: ReplayYardMoveMessageRecord, channel: ManagedChannel) {
    var sink: ReplayYardMoveMessageTarget = ReplayYardMoveMessageLogOnly()
    when (kind) {
        "zone-validation" -> sink = ReplayYardMoveMessageZoneValidation(ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub(channel))
        "fuel-transaction-query" -> sink = ReplayYardMoveMessageFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel))
        "route-plan-approval" -> sink = ReplayYardMoveMessageRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel))
        "vehicle-group-calculation" -> sink = ReplayYardMoveMessageVehicleGroupCalculation(VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
