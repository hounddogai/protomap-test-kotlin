package com.acme.transport.ops.customsauditor

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.checkAuditEventEligibilityRequest
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.explainCustomerSegmentDecisionRequest
import com.acme.localization.v1.QuoteTranslationKeyRequestKt
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.quoteTranslationKeyRequest
import com.acme.localization.v1.translationKey
import com.acme.sponsorships.v1.CountSponsorsRequestKt
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.countSponsorsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventValidationService/CheckAuditEventEligibility and hands what it keeps to the sink of its kind. */
suspend fun copyAuditEventEligibilityId(channel: ManagedChannel) {
    val origin = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.checkAuditEventEligibility(checkAuditEventEligibilityRequest { this.reason = "main-copy-audit-event-eligibility-id" }) }
    val row = CopyAuditEventEligibilityIdRecord(
        correlationId = got.auditEvent.correlationId,
        auditEventId = got.auditEvent.auditEventId,
    )
    val sinks = mapOf<String, CopyAuditEventEligibilityIdWriter>(
        "customer-segment-validation" to CopyAuditEventEligibilityIdCustomerSegmentValidation(CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub(channel)),
        "translation-key-calculation" to CopyAuditEventEligibilityIdTranslationKeyCalculation(TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel)),
        "sponsor-query" to CopyAuditEventEligibilityIdSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "translation-key-calculation") continue
        sinks[kind]?.relay(row)
    }
}

/** Where copyAuditEventEligibilityId hands each record. */
interface CopyAuditEventEligibilityIdWriter {
    suspend fun relay(rec: CopyAuditEventEligibilityIdRecord)
}

/** What copyAuditEventEligibilityId keeps of each CheckAuditEventEligibility response. */
data class CopyAuditEventEligibilityIdRecord(val correlationId: String, val auditEventId: String)

/** Passes records to acme.customers.v1.CustomerSegmentValidationService. */
class CopyAuditEventEligibilityIdCustomerSegmentValidation(private val stub: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub) : CopyAuditEventEligibilityIdWriter {
    override suspend fun relay(rec: CopyAuditEventEligibilityIdRecord) {
        val request = explainCustomerSegmentDecisionRequest {
            this.customerSegment = customerSegment { this.name = rec.correlationId }
        }
        RpcBoundary.run("acme.customers.v1.CustomerSegmentValidationService/ExplainCustomerSegmentDecision") { stub.explainCustomerSegmentDecision(request) }
    }
}

/** Passes records to acme.localization.v1.TranslationKeyCalculationService. */
class CopyAuditEventEligibilityIdTranslationKeyCalculation(private val stub: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub) : CopyAuditEventEligibilityIdWriter {
    override suspend fun relay(rec: CopyAuditEventEligibilityIdRecord) {
        val request = quoteTranslationKeyRequest {
            this.details = QuoteTranslationKeyRequestKt.details { this.currencyCode = rec.correlationId }
            this.translationKey = translationKey { this.name = rec.auditEventId }
        }
        RpcBoundary.run("acme.localization.v1.TranslationKeyCalculationService/QuoteTranslationKey") { stub.quoteTranslationKey(request) }
    }
}

/** Sends records to acme.sponsorships.v1.SponsorQueryService. */
class CopyAuditEventEligibilityIdSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : CopyAuditEventEligibilityIdWriter {
    override suspend fun relay(rec: CopyAuditEventEligibilityIdRecord) {
        val request = countSponsorsRequest {
            this.filter = CountSponsorsRequestKt.filter { this.region = rec.correlationId }
        }
        stub.countSponsors(request)
    }
}
