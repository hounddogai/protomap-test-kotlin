package com.acme.transport.ops.safetyexporter

import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.resumeProofOfDeliveryWorkflowRequest
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.explainDriverCalculationRequest
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.ListCryptoKeysRequestKt
import com.acme.keymanagement.v1.listCryptoKeysRequest
import com.acme.subscriptions.v1.SubscriptionQueryServiceGrpcKt
import com.acme.subscriptions.v1.lookupSubscriptionRequest
import com.acme.telematics.v1.CommentGeoFenceApprovalRequestKt
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.commentGeoFenceApprovalRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.ProofOfDeliveryWorkflowService/ResumeProofOfDeliveryWorkflow and hands what it keeps to the sink of its kind. */
suspend fun forwardProofOfDeliveryWorkflowStatus(channel: ManagedChannel) {
    val upstream = ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel)
    val found = upstream.resumeProofOfDeliveryWorkflow(resumeProofOfDeliveryWorkflowRequest { this.proofOfDeliveryId = "main-forward-proof-of-delivery-workflow-status" })
    val row = ForwardProofOfDeliveryWorkflowStatusRecord(
        status = found.result.status,
        message = normalize(found.result.message),
    )
    val sinks = mapOf<String, ForwardProofOfDeliveryWorkflowStatusDestination>(
        "crypto-key" to ForwardProofOfDeliveryWorkflowStatusCryptoKey(CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub(channel)),
        "subscription-query" to ForwardProofOfDeliveryWorkflowStatusSubscriptionQuery(SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub(channel)),
        "geo-fence-approval" to ForwardProofOfDeliveryWorkflowStatusGeoFenceApproval(GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub(channel)),
        "driver-calculation" to ForwardProofOfDeliveryWorkflowStatusDriverCalculation(DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "driver-calculation") continue
        sinks[kind]?.forward(row)
    }
}

/** Where forwardProofOfDeliveryWorkflowStatus hands each record. */
interface ForwardProofOfDeliveryWorkflowStatusDestination {
    suspend fun forward(rec: ForwardProofOfDeliveryWorkflowStatusRecord)
}

/** What forwardProofOfDeliveryWorkflowStatus keeps of each ResumeProofOfDeliveryWorkflow response. */
data class ForwardProofOfDeliveryWorkflowStatusRecord(val status: String, val message: String)

/** Sends records to acme.keymanagement.v1.CryptoKeyService. */
class ForwardProofOfDeliveryWorkflowStatusCryptoKey(private val stub: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub) : ForwardProofOfDeliveryWorkflowStatusDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryWorkflowStatusRecord) {
        val request = listCryptoKeysRequest {
            this.filter = ListCryptoKeysRequestKt.filter { this.ownerId = rec.message }
            this.orderBy = rec.status
        }
        stub.listCryptoKeys(request)
    }
}

/** Writes records to acme.subscriptions.v1.SubscriptionQueryService. */
class ForwardProofOfDeliveryWorkflowStatusSubscriptionQuery(private val stub: SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub) : ForwardProofOfDeliveryWorkflowStatusDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryWorkflowStatusRecord) {
        RpcBoundary.run("acme.subscriptions.v1.SubscriptionQueryService/LookupSubscription") { stub.lookupSubscription(lookupSubscriptionRequest { this.subscriptionId = rec.message }) }
    }
}

/** Sends records to acme.telematics.v1.GeoFenceApprovalService. */
class ForwardProofOfDeliveryWorkflowStatusGeoFenceApproval(private val stub: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub) : ForwardProofOfDeliveryWorkflowStatusDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryWorkflowStatusRecord) {
        val request = commentGeoFenceApprovalRequest {
            this.details = CommentGeoFenceApprovalRequestKt.details { this.comment = rec.message }
            this.geoFenceId = rec.status
        }
        stub.commentGeoFenceApproval(request)
    }
}

/** Writes records to acme.drivers.v1.DriverCalculationService. */
class ForwardProofOfDeliveryWorkflowStatusDriverCalculation(private val stub: DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub) : ForwardProofOfDeliveryWorkflowStatusDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryWorkflowStatusRecord) {
        stub.explainDriverCalculation(explainDriverCalculationRequest { this.reason = rec.status })
    }
}
