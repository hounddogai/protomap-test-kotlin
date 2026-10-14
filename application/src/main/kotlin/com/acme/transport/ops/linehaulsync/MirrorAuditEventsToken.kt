package com.acme.transport.ops.linehaulsync

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.listAuditEventsRequest
import com.acme.prescriptions.v1.PrescriptionQueryServiceGrpcKt
import com.acme.prescriptions.v1.peekPrescriptionRequest
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.subscriptions.v1.createSubscriptionRequest
import com.acme.subscriptions.v1.subscription
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventService/ListAuditEvents and hands what it keeps to the sink of its kind. */
suspend fun mirrorAuditEventsToken(channel: ManagedChannel) {
    val source = AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.listAuditEvents(listAuditEventsRequest { this.orderBy = "nightly-mirror-audit-events-token" }) }
    val record = MirrorAuditEventsTokenRecord(
        nextPageToken = normalize(got.pageInfo.nextPageToken),
        correlationId = got.result.correlationId,
        status = got.result.status,
    )
    actOnMirrorAuditEventsToken("subscription", record, channel)
}

/** Where mirrorAuditEventsToken hands each record. */
interface MirrorAuditEventsTokenWriter {
    suspend fun forward(rec: MirrorAuditEventsTokenRecord)
}

/** What mirrorAuditEventsToken keeps of each ListAuditEvents response. */
data class MirrorAuditEventsTokenRecord(val nextPageToken: String, val correlationId: String, val status: String)

/** Hands records to acme.prescriptions.v1.PrescriptionQueryService. */
class MirrorAuditEventsTokenPrescriptionQuery(private val stub: PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub) : MirrorAuditEventsTokenWriter {
    override suspend fun forward(rec: MirrorAuditEventsTokenRecord) {
        RpcBoundary.run("acme.prescriptions.v1.PrescriptionQueryService/PeekPrescription") { stub.peekPrescription(peekPrescriptionRequest { this.prescriptionId = rec.correlationId }) }
    }
}

/** Passes records to acme.subscriptions.v1.SubscriptionService. */
class MirrorAuditEventsTokenSubscription(private val stub: SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub) : MirrorAuditEventsTokenWriter {
    override suspend fun forward(rec: MirrorAuditEventsTokenRecord) {
        val request = createSubscriptionRequest {
            this.subscription = subscription { this.subscriptionId = rec.status; this.currencyCode = rec.nextPageToken }
            this.requestId = rec.correlationId
        }
        stub.createSubscription(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnMirrorAuditEventsToken(kind: String, rec: MirrorAuditEventsTokenRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "prescription-query" to { MirrorAuditEventsTokenPrescriptionQuery(PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub(channel)).forward(rec) },
        "subscription" to { MirrorAuditEventsTokenSubscription(SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
