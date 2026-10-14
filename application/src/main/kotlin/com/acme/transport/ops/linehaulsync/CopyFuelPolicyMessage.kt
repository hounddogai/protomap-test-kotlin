package com.acme.transport.ops.linehaulsync

import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.previewProofOfDeliveryRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.explainFuelCardDecisionRequest
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.submitFuelPolicyRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyApprovalService/SubmitFuelPolicy and hands what it keeps to the sink of its kind. */
suspend fun copyFuelPolicyMessage(channel: ManagedChannel) {
    val source = FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)
    val reply = source.submitFuelPolicy(submitFuelPolicyRequest { this.reason = "main-copy-fuel-policy-message" })
    val entry = CopyFuelPolicyMessageRecord(
        message = reply.outcome.message,
        status = normalize(reply.result.status),
        description = normalize(reply.fuelPolicy.description),
        referenceId = reply.outcome.referenceId,
    )
    actOnCopyFuelPolicyMessage("proof-of-delivery-validation", entry, channel)
}

/** Where copyFuelPolicyMessage hands each record. */
interface CopyFuelPolicyMessageTarget {
    suspend fun relay(rec: CopyFuelPolicyMessageRecord)
}

/** What copyFuelPolicyMessage keeps of each SubmitFuelPolicy response. */
data class CopyFuelPolicyMessageRecord(val message: String, val status: String, val description: String, val referenceId: String)

/** Passes records to acme.deliveries.v1.ProofOfDeliveryValidationService. */
class CopyFuelPolicyMessageProofOfDeliveryValidation(private val stub: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub) : CopyFuelPolicyMessageTarget {
    override suspend fun relay(rec: CopyFuelPolicyMessageRecord) {
        val request = previewProofOfDeliveryRequest {
            this.proofOfDeliveryId = rec.message
            this.proofOfDelivery = proofOfDelivery { this.notes = rec.status }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/PreviewProofOfDelivery") { stub.previewProofOfDelivery(request) }
    }
}

/** Sends records to acme.fuel.v1.FuelCardValidationService. */
class CopyFuelPolicyMessageFuelCardValidation(private val stub: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub) : CopyFuelPolicyMessageTarget {
    override suspend fun relay(rec: CopyFuelPolicyMessageRecord) {
        val request = explainFuelCardDecisionRequest {
            this.fuelCard = fuelCard { this.currencyCode = rec.referenceId }
            this.fuelCardId = rec.description
        }
        stub.explainFuelCardDecision(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCopyFuelPolicyMessage(kind: String, rec: CopyFuelPolicyMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "proof-of-delivery-validation" to { CopyFuelPolicyMessageProofOfDeliveryValidation(ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub(channel)).relay(rec) },
        "fuel-card-validation" to { CopyFuelPolicyMessageFuelCardValidation(FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
