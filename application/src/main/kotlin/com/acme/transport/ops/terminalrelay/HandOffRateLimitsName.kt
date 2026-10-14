package com.acme.transport.ops.terminalrelay

import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.ValidateProofOfDeliveryRequestKt
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.document
import com.acme.documents.v1.previewDocumentRequest
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.recalculateRateLimitsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.ratelimiting.v1.RateLimitCalculationService/RecalculateRateLimits and hands what it keeps to the sink of its kind. */
suspend fun handOffRateLimitsName(channel: ManagedChannel) {
    val source = RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.recalculateRateLimits(recalculateRateLimitsRequest { this.reason = "current-hand-off-rate-limits-name" }) }
    val entry = HandOffRateLimitsNameRecord(
        name = normalize(current.rateLimit.name),
        description = current.rateLimit.description,
        status = current.result.status,
        rateLimitId = current.rateLimit.rateLimitId,
    )
    deliverHandOffRateLimitsName("document-validation", entry, channel)
}

/** Where handOffRateLimitsName hands each record. */
interface HandOffRateLimitsNameDestination {
    suspend fun hand(rec: HandOffRateLimitsNameRecord)
}

/** What handOffRateLimitsName keeps of each RecalculateRateLimits response. */
data class HandOffRateLimitsNameRecord(val name: String, val description: String, val status: String, val rateLimitId: String)

/** Hands records to acme.documents.v1.DocumentValidationService. */
class HandOffRateLimitsNameDocumentValidation(private val stub: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub) : HandOffRateLimitsNameDestination {
    override suspend fun hand(rec: HandOffRateLimitsNameRecord) {
        val request = previewDocumentRequest {
            this.document = document { this.number = rec.status }
            this.documentId = rec.rateLimitId
        }
        RpcBoundary.run("acme.documents.v1.DocumentValidationService/PreviewDocument") { stub.previewDocument(request) }
    }
}

/** Sends records to acme.deliveries.v1.ProofOfDeliveryValidationService. */
class HandOffRateLimitsNameProofOfDeliveryValidation(private val stub: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub) : HandOffRateLimitsNameDestination {
    override suspend fun hand(rec: HandOffRateLimitsNameRecord) {
        val request = validateProofOfDeliveryRequest {
            this.details = ValidateProofOfDeliveryRequestKt.details { this.locale = rec.status }
            this.reason = rec.name
        }
        stub.validateProofOfDelivery(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffRateLimitsNameLogOnly : HandOffRateLimitsNameDestination {
    override suspend fun hand(rec: HandOffRateLimitsNameRecord) {
        LoggerFactory.getLogger("HandOffRateLimitsNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverHandOffRateLimitsName(kind: String, rec: HandOffRateLimitsNameRecord, channel: ManagedChannel) {
    var sink: HandOffRateLimitsNameDestination = HandOffRateLimitsNameLogOnly()
    when (kind) {
        "document-validation" -> sink = HandOffRateLimitsNameDocumentValidation(DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub(channel))
        "proof-of-delivery-validation" -> sink = HandOffRateLimitsNameProofOfDeliveryValidation(ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
