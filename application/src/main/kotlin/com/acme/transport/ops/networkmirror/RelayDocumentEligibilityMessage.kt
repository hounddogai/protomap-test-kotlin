package com.acme.transport.ops.networkmirror

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.NormalizeAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.deliveries.v1.DelegateProofOfDeliveryApprovalRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.delegateProofOfDeliveryApprovalRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.checkDocumentEligibilityRequest
import com.acme.performers.v1.BrowsePerformersRequestKt
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.browsePerformersRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.documents.v1.DocumentValidationService/CheckDocumentEligibility and hands what it keeps to the sink of its kind. */
suspend fun relayDocumentEligibilityMessage(channel: ManagedChannel) {
    val source = DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub(channel)
    val got = source.checkDocumentEligibility(checkDocumentEligibilityRequest { this.reason = "main-relay-document-eligibility-message" })
    val rec = RelayDocumentEligibilityMessageRecord(
        message = got.outcome.message,
        notes = got.document.notes,
        number = got.document.number,
    )
    val sinks = mapOf<String, RelayDocumentEligibilityMessageTarget>(
        "audit-event-validation" to RelayDocumentEligibilityMessageAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        "proof-of-delivery-approval" to RelayDocumentEligibilityMessageProofOfDeliveryApproval(ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub(channel)),
        "performer-query" to RelayDocumentEligibilityMessagePerformerQuery(PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "audit-event-validation") continue
        sinks[kind]?.transmit(rec)
    }
}

/** Where relayDocumentEligibilityMessage hands each record. */
interface RelayDocumentEligibilityMessageTarget {
    suspend fun transmit(rec: RelayDocumentEligibilityMessageRecord)
}

/** What relayDocumentEligibilityMessage keeps of each CheckDocumentEligibility response. */
data class RelayDocumentEligibilityMessageRecord(val message: String, val notes: String, val number: String)

/** Sends records to acme.audit.v1.AuditEventValidationService. */
class RelayDocumentEligibilityMessageAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : RelayDocumentEligibilityMessageTarget {
    override suspend fun transmit(rec: RelayDocumentEligibilityMessageRecord) {
        val request = normalizeAuditEventRequest {
            this.auditEvent = auditEvent { this.kind = rec.notes }
            this.auditEventId = rec.number
            this.details = NormalizeAuditEventRequestKt.details { this.locale = rec.message }
        }
        stub.normalizeAuditEvent(request)
    }
}

/** Passes records to acme.deliveries.v1.ProofOfDeliveryApprovalService. */
class RelayDocumentEligibilityMessageProofOfDeliveryApproval(private val stub: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub) : RelayDocumentEligibilityMessageTarget {
    override suspend fun transmit(rec: RelayDocumentEligibilityMessageRecord) {
        val request = delegateProofOfDeliveryApprovalRequest {
            this.proofOfDelivery = proofOfDelivery { this.notes = rec.number }
            this.details = DelegateProofOfDeliveryApprovalRequestKt.details { this.comment = rec.message }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/DelegateProofOfDeliveryApproval") { stub.delegateProofOfDeliveryApproval(request) }
    }
}

/** Passes records to acme.performers.v1.PerformerQueryService. */
class RelayDocumentEligibilityMessagePerformerQuery(private val stub: PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub) : RelayDocumentEligibilityMessageTarget {
    override suspend fun transmit(rec: RelayDocumentEligibilityMessageRecord) {
        val request = browsePerformersRequest {
            this.filter = BrowsePerformersRequestKt.filter { this.query = rec.message }
        }
        stub.browsePerformers(request)
    }
}
