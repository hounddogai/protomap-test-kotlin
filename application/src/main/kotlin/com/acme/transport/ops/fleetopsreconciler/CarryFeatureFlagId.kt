package com.acme.transport.ops.fleetopsreconciler

import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.peekFeatureFlagRequest
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.getTranslationKeyRatesRequest
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.approveStopRequest
import com.acme.routing.v1.stop
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.featureflags.v1.FeatureFlagQueryService/PeekFeatureFlag and hands what it keeps to the sink of its kind. */
suspend fun carryFeatureFlagId(channel: ManagedChannel) {
    val upstream = FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.peekFeatureFlag(peekFeatureFlagRequest { this.featureFlagId = "live-carry-feature-flag-id" }) }
    val rec = CarryFeatureFlagIdRecord(
        correlationId = normalize(got.result.correlationId),
        status = got.result.status,
        description = got.featureFlag.description,
        name = normalize(got.featureFlag.name),
    )
    val sinks = mapOf<String, CarryFeatureFlagIdDestination>(
        "stop-approval" to CarryFeatureFlagIdStopApproval(StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(channel)),
        "translation-key-calculation" to CarryFeatureFlagIdTranslationKeyCalculation(TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("CARRY_FEATURE_FLAG_ID_TARGET")] ?: return
    sink.write(rec)
}

/** Where carryFeatureFlagId hands each record. */
interface CarryFeatureFlagIdDestination {
    suspend fun write(rec: CarryFeatureFlagIdRecord)
}

/** What carryFeatureFlagId keeps of each PeekFeatureFlag response. */
data class CarryFeatureFlagIdRecord(val correlationId: String, val status: String, val description: String, val name: String)

/** Writes records to acme.routing.v1.StopApprovalService. */
class CarryFeatureFlagIdStopApproval(private val stub: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub) : CarryFeatureFlagIdDestination {
    override suspend fun write(rec: CarryFeatureFlagIdRecord) {
        stub.approveStop(approveStopRequest { this.stop = stop { this.stopId = rec.name } })
    }
}

/** Hands records to acme.localization.v1.TranslationKeyCalculationService. */
class CarryFeatureFlagIdTranslationKeyCalculation(private val stub: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub) : CarryFeatureFlagIdDestination {
    override suspend fun write(rec: CarryFeatureFlagIdRecord) {
        stub.getTranslationKeyRates(getTranslationKeyRatesRequest { this.translationKeyId = rec.status })
    }
}
