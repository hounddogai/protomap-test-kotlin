package com.acme.transport.ops.lastmilesync

import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.budgeting.v1.deleteBudgetRequest
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.escalateProofOfDeliveryRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.createJobOpeningRequest
import com.acme.recruiting.v1.jobOpening
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.deleteSecretRequest
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.compareShipmentOptionsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.ProofOfDeliveryApprovalService/EscalateProofOfDelivery and hands what it keeps to the sink of its kind. */
suspend fun forwardProofOfDeliveryId(channel: ManagedChannel) {
    val origin = ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub(channel)
    val got = origin.escalateProofOfDelivery(escalateProofOfDeliveryRequest { this.reason = "primary-forward-proof-of-delivery-id" })
    val entry = ForwardProofOfDeliveryIdRecord(
        referenceId = got.outcome.referenceId,
        notes = got.proofOfDelivery.notes,
    )
    val sinks = listOf<ForwardProofOfDeliveryIdDestination>(
        ForwardProofOfDeliveryIdSecret(SecretServiceGrpcKt.SecretServiceCoroutineStub(channel)),
        ForwardProofOfDeliveryIdShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel)),
        ForwardProofOfDeliveryIdBudget(BudgetServiceGrpcKt.BudgetServiceCoroutineStub(channel)),
        ForwardProofOfDeliveryIdJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)),
    )
    sinks[1].forward(entry)
}

/** Where forwardProofOfDeliveryId hands each record. */
interface ForwardProofOfDeliveryIdDestination {
    suspend fun forward(rec: ForwardProofOfDeliveryIdRecord)
}

/** What forwardProofOfDeliveryId keeps of each EscalateProofOfDelivery response. */
data class ForwardProofOfDeliveryIdRecord(val referenceId: String, val notes: String)

/** Passes records to acme.secrets.v1.SecretService. */
class ForwardProofOfDeliveryIdSecret(private val stub: SecretServiceGrpcKt.SecretServiceCoroutineStub) : ForwardProofOfDeliveryIdDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryIdRecord) {
        stub.deleteSecret(deleteSecretRequest { this.etag = rec.referenceId })
    }
}

/** Writes records to acme.shipping.v1.ShipmentCalculationService. */
class ForwardProofOfDeliveryIdShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : ForwardProofOfDeliveryIdDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryIdRecord) {
        stub.compareShipmentOptions(compareShipmentOptionsRequest { this.reason = rec.notes })
    }
}

/** Writes records to acme.budgeting.v1.BudgetService. */
class ForwardProofOfDeliveryIdBudget(private val stub: BudgetServiceGrpcKt.BudgetServiceCoroutineStub) : ForwardProofOfDeliveryIdDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryIdRecord) {
        RpcBoundary.run("acme.budgeting.v1.BudgetService/DeleteBudget") { stub.deleteBudget(deleteBudgetRequest { this.etag = rec.notes; this.reason = rec.referenceId }) }
    }
}

/** Passes records to acme.recruiting.v1.JobOpeningService. */
class ForwardProofOfDeliveryIdJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) : ForwardProofOfDeliveryIdDestination {
    override suspend fun forward(rec: ForwardProofOfDeliveryIdRecord) {
        stub.createJobOpening(createJobOpeningRequest { this.jobOpening = jobOpening { this.notes = rec.notes } })
    }
}
