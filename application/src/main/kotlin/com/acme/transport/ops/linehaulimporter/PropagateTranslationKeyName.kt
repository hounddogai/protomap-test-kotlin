package com.acme.transport.ops.linehaulimporter

import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.estimateTranslationKeyRequest
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.estimateRateLimitRequest
import com.acme.ratelimiting.v1.rateLimit
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.localization.v1.TranslationKeyCalculationService/EstimateTranslationKey and hands what it keeps to acme.ratelimiting.v1.RateLimitCalculationService. */
suspend fun propagateTranslationKeyName(channel: ManagedChannel) {
    val source = TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.estimateTranslationKey(estimateTranslationKeyRequest { this.translationKeyId = "primary-propagate-translation-key-name" }) }
    val entry = PropagateTranslationKeyNameRecord(
        name = reply.translationKey.name,
        message = reply.result.message,
        status = reply.result.status,
    )
    PropagateTranslationKeyNameRateLimitCalculation(RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(channel)).forward(entry)
}

/** What propagateTranslationKeyName keeps of each EstimateTranslationKey response. */
data class PropagateTranslationKeyNameRecord(val name: String, val message: String, val status: String)

/** Hands records to acme.ratelimiting.v1.RateLimitCalculationService. */
class PropagateTranslationKeyNameRateLimitCalculation(private val stub: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub) {
    suspend fun forward(rec: PropagateTranslationKeyNameRecord) {
        val request = estimateRateLimitRequest {
            this.rateLimit = rateLimit { this.description = rec.status }
            this.reason = rec.message
        }
        stub.estimateRateLimit(request)
    }
}
