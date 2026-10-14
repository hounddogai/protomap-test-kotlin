package com.acme.transport.ops.linehaulsync

import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.FindCarePlansRequestKt
import com.acme.careplans.v1.findCarePlansRequest
import com.acme.common.v1.pageRequest
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelPolicyConflictsRequestKt
import com.acme.fuel.v1.listFuelPolicyConflictsRequest
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.estimateRouteRequest
import com.acme.routing.v1.getStopApprovalStatusRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.RouteCalculationService/EstimateRoute and hands what it keeps to the sink of its kind. */
suspend fun handOffRouteMessage(channel: ManagedChannel) {
    val origin = RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.estimateRoute(estimateRouteRequest { this.reason = "live-hand-off-route-message" }) }
    val record = HandOffRouteMessageRecord(
        message = current.result.message,
        currencyCode = current.breakdown.currencyCode,
        correlationId = normalize(current.result.correlationId),
        status = current.result.status,
    )
    deliverHandOffRouteMessage("fuel-policy-sync", record, channel)
}

/** Where handOffRouteMessage hands each record. */
interface HandOffRouteMessageSink {
    suspend fun forward(rec: HandOffRouteMessageRecord)
}

/** What handOffRouteMessage keeps of each EstimateRoute response. */
data class HandOffRouteMessageRecord(val message: String, val currencyCode: String, val correlationId: String, val status: String)

/** Writes records to acme.careplans.v1.CarePlanQueryService. */
class HandOffRouteMessageCarePlanQuery(private val stub: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub) : HandOffRouteMessageSink {
    override suspend fun forward(rec: HandOffRouteMessageRecord) {
        val request = findCarePlansRequest {
            this.page = pageRequest { this.pageToken = rec.message }
            this.filter = FindCarePlansRequestKt.filter { this.query = rec.correlationId }
            this.orderBy = rec.currencyCode
        }
        stub.findCarePlans(request)
    }
}

/** Hands records to acme.routing.v1.StopApprovalService. */
class HandOffRouteMessageStopApproval(private val stub: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub) : HandOffRouteMessageSink {
    override suspend fun forward(rec: HandOffRouteMessageRecord) {
        stub.getStopApprovalStatus(getStopApprovalStatusRequest { this.stopId = rec.correlationId })
    }
}

/** Writes records to acme.fuel.v1.FuelPolicySyncService. */
class HandOffRouteMessageFuelPolicySync(private val stub: FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub) : HandOffRouteMessageSink {
    override suspend fun forward(rec: HandOffRouteMessageRecord) {
        val request = listFuelPolicyConflictsRequest {
            this.filter = ListFuelPolicyConflictsRequestKt.filter { this.query = rec.currencyCode; this.region = rec.message }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.listFuelPolicyConflicts(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffRouteMessageLogOnly : HandOffRouteMessageSink {
    override suspend fun forward(rec: HandOffRouteMessageRecord) {
        LoggerFactory.getLogger("HandOffRouteMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverHandOffRouteMessage(kind: String, rec: HandOffRouteMessageRecord, channel: ManagedChannel) {
    var sink: HandOffRouteMessageSink = HandOffRouteMessageLogOnly()
    when (kind) {
        "care-plan-query" -> sink = HandOffRouteMessageCarePlanQuery(CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel))
        "stop-approval" -> sink = HandOffRouteMessageStopApproval(StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(channel))
        "fuel-policy-sync" -> sink = HandOffRouteMessageFuelPolicySync(FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
