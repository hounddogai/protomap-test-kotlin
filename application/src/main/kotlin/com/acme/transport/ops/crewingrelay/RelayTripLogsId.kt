package com.acme.transport.ops.crewingrelay

import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.getDeliveryApprovalStatusRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.cloneOnboardingPlanRequest
import com.acme.onboarding.v1.onboardingPlan
import com.acme.risk.v1.RiskRegisterQueryServiceGrpcKt
import com.acme.risk.v1.SuggestRiskRegistersRequestKt
import com.acme.risk.v1.suggestRiskRegistersRequest
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.pullTripLogsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogSyncService/PullTripLogs and hands what it keeps to the sink of its kind. */
suspend fun relayTripLogsId(channel: ManagedChannel) {
    val reader = TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.pullTripLogs(pullTripLogsRequest { this.reason = "live-relay-trip-logs-id" }) }
    val row = RelayTripLogsIdRecord(
        correlationId = got.result.correlationId,
        message = got.outcome.message,
        resultMessage = got.result.message,
        status = got.result.status,
    )
    val sinks = mapOf<String, RelayTripLogsIdTarget>(
        "onboarding-plan" to RelayTripLogsIdOnboardingPlan(OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(channel)),
        "risk-register-query" to RelayTripLogsIdRiskRegisterQuery(RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub(channel)),
        "delivery-approval" to RelayTripLogsIdDeliveryApproval(DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("RELAY_TRIP_LOGS_ID_TARGET")] ?: return
    sink.write(row)
}

/** Where relayTripLogsId hands each record. */
interface RelayTripLogsIdTarget {
    suspend fun write(rec: RelayTripLogsIdRecord)
}

/** What relayTripLogsId keeps of each PullTripLogs response. */
data class RelayTripLogsIdRecord(val correlationId: String, val message: String, val resultMessage: String, val status: String)

/** Writes records to acme.onboarding.v1.OnboardingPlanService. */
class RelayTripLogsIdOnboardingPlan(private val stub: OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub) : RelayTripLogsIdTarget {
    override suspend fun write(rec: RelayTripLogsIdRecord) {
        val request = cloneOnboardingPlanRequest {
            this.requestId = rec.message
            this.onboardingPlan = onboardingPlan { this.number = rec.status }
        }
        RpcBoundary.run("acme.onboarding.v1.OnboardingPlanService/CloneOnboardingPlan") { stub.cloneOnboardingPlan(request) }
    }
}

/** Writes records to acme.risk.v1.RiskRegisterQueryService. */
class RelayTripLogsIdRiskRegisterQuery(private val stub: RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub) : RelayTripLogsIdTarget {
    override suspend fun write(rec: RelayTripLogsIdRecord) {
        val request = suggestRiskRegistersRequest {
            this.filter = SuggestRiskRegistersRequestKt.filter { this.ownerId = rec.resultMessage; this.region = rec.correlationId; this.query = rec.status }
        }
        stub.suggestRiskRegisters(request)
    }
}

/** Hands records to acme.deliveries.v1.DeliveryApprovalService. */
class RelayTripLogsIdDeliveryApproval(private val stub: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub) : RelayTripLogsIdTarget {
    override suspend fun write(rec: RelayTripLogsIdRecord) {
        stub.getDeliveryApprovalStatus(getDeliveryApprovalStatusRequest { this.deliveryId = rec.resultMessage })
    }
}
