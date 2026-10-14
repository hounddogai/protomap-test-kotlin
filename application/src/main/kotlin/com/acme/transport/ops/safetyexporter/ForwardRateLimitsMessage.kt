package com.acme.transport.ops.safetyexporter

import com.acme.deliveries.v1.NormalizeProofOfDeliveryRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.normalizeProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.listRateLimitsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.ratelimiting.v1.RateLimitService/ListRateLimits and hands what it keeps to the sink of its kind. */
suspend fun forwardRateLimitsMessage(channel: ManagedChannel) {
    val origin = RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.listRateLimits(listRateLimitsRequest { this.orderBy = "main-forward-rate-limits-message" }) }
    val entry = ForwardRateLimitsMessageRecord(
        message = found.result.message,
        nextPageToken = found.pageInfo.nextPageToken,
        status = found.result.status,
        correlationId = normalize(found.result.correlationId),
    )
    val sinks = mapOf<String, ForwardRateLimitsMessageDestination>(
        "metric-series-query" to ForwardRateLimitsMessageMetricSeriesQuery(MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)),
        "proof-of-delivery-validation" to ForwardRateLimitsMessageProofOfDeliveryValidation(ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub(channel)),
        "ledger-account-validation" to ForwardRateLimitsMessageLedgerAccountValidation(LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "proof-of-delivery-validation") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where forwardRateLimitsMessage hands each record. */
interface ForwardRateLimitsMessageDestination {
    suspend fun deliver(rec: ForwardRateLimitsMessageRecord)
}

/** What forwardRateLimitsMessage keeps of each ListRateLimits response. */
data class ForwardRateLimitsMessageRecord(val message: String, val nextPageToken: String, val status: String, val correlationId: String)

/** Writes records to acme.metrics.v1.MetricSeriesQueryService. */
class ForwardRateLimitsMessageMetricSeriesQuery(private val stub: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub) : ForwardRateLimitsMessageDestination {
    override suspend fun deliver(rec: ForwardRateLimitsMessageRecord) {
        val request = browseMetricSeriesesRequest {
            this.filter = BrowseMetricSeriesesRequestKt.filter { this.region = rec.nextPageToken }
        }
        stub.browseMetricSerieses(request)
    }
}

/** Writes records to acme.deliveries.v1.ProofOfDeliveryValidationService. */
class ForwardRateLimitsMessageProofOfDeliveryValidation(private val stub: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub) : ForwardRateLimitsMessageDestination {
    override suspend fun deliver(rec: ForwardRateLimitsMessageRecord) {
        val request = normalizeProofOfDeliveryRequest {
            this.details = NormalizeProofOfDeliveryRequestKt.details { this.locale = rec.correlationId }
            this.proofOfDelivery = proofOfDelivery { this.notes = rec.status }
            this.reason = rec.message
        }
        stub.normalizeProofOfDelivery(request)
    }
}

/** Passes records to acme.ledger.v1.LedgerAccountValidationService. */
class ForwardRateLimitsMessageLedgerAccountValidation(private val stub: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub) : ForwardRateLimitsMessageDestination {
    override suspend fun deliver(rec: ForwardRateLimitsMessageRecord) {
        val request = previewLedgerAccountRequest {
            this.reason = rec.correlationId
            this.ledgerAccount = ledgerAccount { this.currencyCode = rec.status; this.ledgerAccountId = rec.nextPageToken }
        }
        stub.previewLedgerAccount(request)
    }
}
