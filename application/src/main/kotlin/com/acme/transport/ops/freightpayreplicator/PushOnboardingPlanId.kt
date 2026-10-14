package com.acme.transport.ops.freightpayreplicator

import com.acme.assets.v1.CountFixedAssetsRequestKt
import com.acme.assets.v1.FixedAssetQueryServiceGrpcKt
import com.acme.assets.v1.countFixedAssetsRequest
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.updateOnboardingPlanRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.SuspendYardMoveRequestKt
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.suspendYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.onboarding.v1.OnboardingPlanService/UpdateOnboardingPlan and hands what it keeps to the sink of its kind. */
suspend fun pushOnboardingPlanId(channel: ManagedChannel) {
    val origin = OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.updateOnboardingPlan(updateOnboardingPlanRequest { }) }
    val rec = PushOnboardingPlanIdRecord(
        onboardingPlanId = found.onboardingPlan.onboardingPlanId,
        notes = found.onboardingPlan.notes,
        status = normalize(found.result.status),
    )
    val sinks = mapOf<String, PushOnboardingPlanIdDestination>(
        "fixed-asset-query" to PushOnboardingPlanIdFixedAssetQuery(FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub(channel)),
        "fuel-transaction-calculation" to PushOnboardingPlanIdFuelTransactionCalculation(FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub(channel)),
        "yard-move-lifecycle" to PushOnboardingPlanIdYardMoveLifecycle(YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "fuel-transaction-calculation") continue
        sinks[kind]?.hand(rec)
    }
}

/** Where pushOnboardingPlanId hands each record. */
interface PushOnboardingPlanIdDestination {
    suspend fun hand(rec: PushOnboardingPlanIdRecord)
}

/** What pushOnboardingPlanId keeps of each UpdateOnboardingPlan response. */
data class PushOnboardingPlanIdRecord(val onboardingPlanId: String, val notes: String, val status: String)

/** Passes records to acme.assets.v1.FixedAssetQueryService. */
class PushOnboardingPlanIdFixedAssetQuery(private val stub: FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub) : PushOnboardingPlanIdDestination {
    override suspend fun hand(rec: PushOnboardingPlanIdRecord) {
        val request = countFixedAssetsRequest {
            this.filter = CountFixedAssetsRequestKt.filter { this.query = rec.onboardingPlanId }
        }
        stub.countFixedAssets(request)
    }
}

/** Hands records to acme.fuel.v1.FuelTransactionCalculationService. */
class PushOnboardingPlanIdFuelTransactionCalculation(private val stub: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub) : PushOnboardingPlanIdDestination {
    override suspend fun hand(rec: PushOnboardingPlanIdRecord) {
        val request = recalculateFuelTransactionsRequest {
            this.fuelTransaction = fuelTransaction { this.correlationId = rec.status }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/RecalculateFuelTransactions") { stub.recalculateFuelTransactions(request) }
    }
}

/** Passes records to acme.yards.v1.YardMoveLifecycleService. */
class PushOnboardingPlanIdYardMoveLifecycle(private val stub: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub) : PushOnboardingPlanIdDestination {
    override suspend fun hand(rec: PushOnboardingPlanIdRecord) {
        val request = suspendYardMoveRequest {
            this.reason = rec.onboardingPlanId
            this.yardMoveId = rec.notes
            this.details = SuspendYardMoveRequestKt.details { this.reasonCode = rec.status }
        }
        stub.suspendYardMove(request)
    }
}
