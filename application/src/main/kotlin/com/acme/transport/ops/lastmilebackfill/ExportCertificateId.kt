package com.acme.transport.ops.lastmilebackfill

import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.peekCertificateRequest
import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.modelregistry.v1.peekRegisteredModelRequest
import com.acme.notifications.v1.ExplainNotificationDecisionRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.certificates.v1.CertificateQueryService/PeekCertificate and hands what it keeps to the sink of its kind. */
suspend fun exportCertificateId(channel: ManagedChannel) {
    val source = CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.peekCertificate(peekCertificateRequest { this.certificateId = "main-export-certificate-id" }) }
    val record = ExportCertificateIdRecord(
        correlationId = found.result.correlationId,
        notes = found.certificate.notes,
        certificateId = found.certificate.certificateId,
        status = found.result.status,
    )
    val sinks = mapOf<String, ExportCertificateIdWriter>(
        "registered-model-query" to ExportCertificateIdRegisteredModelQuery(RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub(channel)),
        "notification-validation" to ExportCertificateIdNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
    )
    routeExportCertificateId(sinks, "notification-validation", record)
}

/** Where exportCertificateId hands each record. */
interface ExportCertificateIdWriter {
    suspend fun transmit(rec: ExportCertificateIdRecord)
}

/** What exportCertificateId keeps of each PeekCertificate response. */
data class ExportCertificateIdRecord(val correlationId: String, val notes: String, val certificateId: String, val status: String)

/** Hands records to acme.modelregistry.v1.RegisteredModelQueryService. */
class ExportCertificateIdRegisteredModelQuery(private val stub: RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub) : ExportCertificateIdWriter {
    override suspend fun transmit(rec: ExportCertificateIdRecord) {
        stub.peekRegisteredModel(peekRegisteredModelRequest { this.registeredModelId = rec.certificateId })
    }
}

/** Sends records to acme.notifications.v1.NotificationValidationService. */
class ExportCertificateIdNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : ExportCertificateIdWriter {
    override suspend fun transmit(rec: ExportCertificateIdRecord) {
        val request = explainNotificationDecisionRequest {
            this.notificationId = rec.correlationId
            this.details = ExplainNotificationDecisionRequestKt.details { this.locale = rec.notes }
            this.reason = rec.status
        }
        stub.explainNotificationDecision(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeExportCertificateId(sinks: Map<String, ExportCertificateIdWriter>, kind: String, rec: ExportCertificateIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
