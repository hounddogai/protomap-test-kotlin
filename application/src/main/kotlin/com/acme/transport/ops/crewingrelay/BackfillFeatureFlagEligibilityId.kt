package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.SummarizeHouseholdsRequestKt
import com.acme.customers.v1.summarizeHouseholdsRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.fuel.v1.ApproveFuelPolicyRequestKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.approveFuelPolicyRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.featureflags.v1.FeatureFlagValidationService/CheckFeatureFlagEligibility and hands what it keeps to the sink of its kind. */
suspend fun backfillFeatureFlagEligibilityId(channel: ManagedChannel) {
    val upstream = FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.checkFeatureFlagEligibility(checkFeatureFlagEligibilityRequest { this.reason = "live-backfill-feature-flag-eligibility-id" }) }
    val rec = BackfillFeatureFlagEligibilityIdRecord(
        correlationId = normalize(reply.result.correlationId),
        featureFlagId = reply.featureFlag.featureFlagId,
        message = reply.result.message,
        status = normalize(reply.outcome.status),
    )
    val sinks = mapOf<String, BackfillFeatureFlagEligibilityIdDestination>(
        "fuel-policy-approval" to BackfillFeatureFlagEligibilityIdFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)),
        "household-query" to BackfillFeatureFlagEligibilityIdHouseholdQuery(HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)),
    )
    routeBackfillFeatureFlagEligibilityId(sinks, "fuel-policy-approval", rec)
}

/** Where backfillFeatureFlagEligibilityId hands each record. */
interface BackfillFeatureFlagEligibilityIdDestination {
    suspend fun relay(rec: BackfillFeatureFlagEligibilityIdRecord)
}

/** What backfillFeatureFlagEligibilityId keeps of each CheckFeatureFlagEligibility response. */
data class BackfillFeatureFlagEligibilityIdRecord(val correlationId: String, val featureFlagId: String, val message: String, val status: String)

/** Hands records to acme.fuel.v1.FuelPolicyApprovalService. */
class BackfillFeatureFlagEligibilityIdFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : BackfillFeatureFlagEligibilityIdDestination {
    override suspend fun relay(rec: BackfillFeatureFlagEligibilityIdRecord) {
        val request = approveFuelPolicyRequest {
            this.fuelPolicyId = rec.featureFlagId
            this.details = ApproveFuelPolicyRequestKt.details { this.approverEmail = rec.status }
        }
        stub.approveFuelPolicy(request)
    }
}

/** Hands records to acme.customers.v1.HouseholdQueryService. */
class BackfillFeatureFlagEligibilityIdHouseholdQuery(private val stub: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub) : BackfillFeatureFlagEligibilityIdDestination {
    override suspend fun relay(rec: BackfillFeatureFlagEligibilityIdRecord) {
        val request = summarizeHouseholdsRequest {
            this.details = SummarizeHouseholdsRequestKt.details { this.cron = rec.status }
        }
        stub.summarizeHouseholds(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeBackfillFeatureFlagEligibilityId(sinks: Map<String, BackfillFeatureFlagEligibilityIdDestination>, kind: String, rec: BackfillFeatureFlagEligibilityIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.relay(rec)
}
