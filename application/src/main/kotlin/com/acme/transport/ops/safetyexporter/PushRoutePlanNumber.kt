package com.acme.transport.ops.safetyexporter

import com.acme.customers.v1.ConsentRecordWorkflowServiceGrpcKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.resumeConsentRecordWorkflowRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.validateFeatureFlagRequest
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.prescriptions.v1.getPrescriptionRequest
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.normalizeRoutePlanRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RoutePlanValidationService/NormalizeRoutePlan and hands what it keeps to the sink of its kind. */
suspend fun pushRoutePlanNumber(channel: ManagedChannel) {
    val reader = RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub(channel)
    val current = reader.normalizeRoutePlan(normalizeRoutePlanRequest { this.routePlanId = "live-push-route-plan-number" })
    val record = PushRoutePlanNumberRecord(
        number = current.routePlan.number,
        message = current.outcome.message,
        correlationId = normalize(current.result.correlationId),
        resultMessage = current.result.message,
    )
    val sinks = listOf<PushRoutePlanNumberSink>(
        PushRoutePlanNumberConsentRecordWorkflow(ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub(channel)),
        PushRoutePlanNumberFeatureFlagValidation(FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(channel)),
        PushRoutePlanNumberPrescription(PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub(channel)),
    )
    sinks[2].transmit(record)
}

/** Where pushRoutePlanNumber hands each record. */
interface PushRoutePlanNumberSink {
    suspend fun transmit(rec: PushRoutePlanNumberRecord)
}

/** What pushRoutePlanNumber keeps of each NormalizeRoutePlan response. */
data class PushRoutePlanNumberRecord(val number: String, val message: String, val correlationId: String, val resultMessage: String)

/** Sends records to acme.customers.v1.ConsentRecordWorkflowService. */
class PushRoutePlanNumberConsentRecordWorkflow(private val stub: ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub) : PushRoutePlanNumberSink {
    override suspend fun transmit(rec: PushRoutePlanNumberRecord) {
        val request = resumeConsentRecordWorkflowRequest {
            this.consentRecord = consentRecord { this.kind = rec.correlationId; this.consentRecordId = rec.number }
        }
        RpcBoundary.run("acme.customers.v1.ConsentRecordWorkflowService/ResumeConsentRecordWorkflow") { stub.resumeConsentRecordWorkflow(request) }
    }
}

/** Writes records to acme.featureflags.v1.FeatureFlagValidationService. */
class PushRoutePlanNumberFeatureFlagValidation(private val stub: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub) : PushRoutePlanNumberSink {
    override suspend fun transmit(rec: PushRoutePlanNumberRecord) {
        val request = validateFeatureFlagRequest {
            this.featureFlag = featureFlag { this.description = rec.correlationId; this.featureFlagId = rec.message; this.name = rec.number }
        }
        RpcBoundary.run("acme.featureflags.v1.FeatureFlagValidationService/ValidateFeatureFlag") { stub.validateFeatureFlag(request) }
    }
}

/** Passes records to acme.prescriptions.v1.PrescriptionService. */
class PushRoutePlanNumberPrescription(private val stub: PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub) : PushRoutePlanNumberSink {
    override suspend fun transmit(rec: PushRoutePlanNumberRecord) {
        stub.getPrescription(getPrescriptionRequest { this.prescriptionId = rec.resultMessage })
    }
}
