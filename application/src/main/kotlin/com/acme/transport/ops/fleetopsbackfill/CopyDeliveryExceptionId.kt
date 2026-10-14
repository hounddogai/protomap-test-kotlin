package com.acme.transport.ops.fleetopsbackfill

import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.QuoteConsentRecordRequestKt
import com.acme.customers.v1.quoteConsentRecordRequest
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.PauseProofOfDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.pauseProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.scoreDeliveryExceptionRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.user
import com.acme.identity.v1.validateUserRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.deliveries.v1.DeliveryExceptionValidationService/ScoreDeliveryException and hands what it keeps to the sink of its kind. */
suspend fun copyDeliveryExceptionId(channel: ManagedChannel) {
    val upstream = DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub(channel)
    val found = upstream.scoreDeliveryException(scoreDeliveryExceptionRequest { this.deliveryExceptionId = "live-copy-delivery-exception-id" })
    val row = CopyDeliveryExceptionIdRecord(
        deliveryExceptionId = found.deliveryException.deliveryExceptionId,
        currencyCode = found.breakdown.currencyCode,
        correlationId = found.result.correlationId,
    )
    deliverCopyDeliveryExceptionId("proof-of-delivery-workflow", row, channel)
}

/** Where copyDeliveryExceptionId hands each record. */
interface CopyDeliveryExceptionIdSink {
    suspend fun forward(rec: CopyDeliveryExceptionIdRecord)
}

/** What copyDeliveryExceptionId keeps of each ScoreDeliveryException response. */
data class CopyDeliveryExceptionIdRecord(val deliveryExceptionId: String, val currencyCode: String, val correlationId: String)

/** Hands records to acme.deliveries.v1.ProofOfDeliveryWorkflowService. */
class CopyDeliveryExceptionIdProofOfDeliveryWorkflow(private val stub: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub) : CopyDeliveryExceptionIdSink {
    override suspend fun forward(rec: CopyDeliveryExceptionIdRecord) {
        val request = pauseProofOfDeliveryWorkflowRequest {
            this.proofOfDelivery = proofOfDelivery { this.number = rec.currencyCode }
            this.details = PauseProofOfDeliveryWorkflowRequestKt.details { this.step = rec.deliveryExceptionId }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/PauseProofOfDeliveryWorkflow") { stub.pauseProofOfDeliveryWorkflow(request) }
    }
}

/** Hands records to acme.identity.v1.UserValidationService. */
class CopyDeliveryExceptionIdUserValidation(private val stub: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub) : CopyDeliveryExceptionIdSink {
    override suspend fun forward(rec: CopyDeliveryExceptionIdRecord) {
        val request = validateUserRequest {
            this.user = user { this.userId = rec.correlationId }
            this.reason = rec.currencyCode
        }
        RpcBoundary.run("acme.identity.v1.UserValidationService/ValidateUser") { stub.validateUser(request) }
    }
}

/** Writes records to acme.customers.v1.ConsentRecordCalculationService. */
class CopyDeliveryExceptionIdConsentRecordCalculation(private val stub: ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub) : CopyDeliveryExceptionIdSink {
    override suspend fun forward(rec: CopyDeliveryExceptionIdRecord) {
        val request = quoteConsentRecordRequest {
            this.details = QuoteConsentRecordRequestKt.details { this.currencyCode = rec.correlationId }
        }
        RpcBoundary.run("acme.customers.v1.ConsentRecordCalculationService/QuoteConsentRecord") { stub.quoteConsentRecord(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyDeliveryExceptionIdLogOnly : CopyDeliveryExceptionIdSink {
    override suspend fun forward(rec: CopyDeliveryExceptionIdRecord) {
        LoggerFactory.getLogger("CopyDeliveryExceptionIdRecord").atInfo().addKeyValue("deliveryExceptionId", rec.deliveryExceptionId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyDeliveryExceptionId(kind: String, rec: CopyDeliveryExceptionIdRecord, channel: ManagedChannel) {
    var sink: CopyDeliveryExceptionIdSink = CopyDeliveryExceptionIdLogOnly()
    when (kind) {
        "proof-of-delivery-workflow" -> sink = CopyDeliveryExceptionIdProofOfDeliveryWorkflow(ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel))
        "user-validation" -> sink = CopyDeliveryExceptionIdUserValidation(UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub(channel))
        "consent-record-calculation" -> sink = CopyDeliveryExceptionIdConsentRecordCalculation(ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
