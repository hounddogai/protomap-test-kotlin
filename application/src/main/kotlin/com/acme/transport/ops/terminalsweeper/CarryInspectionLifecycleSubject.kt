package com.acme.transport.ops.terminalsweeper

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.createAuditEventRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.getInspectionLifecycleRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.comparePrescriptionOptionsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.InspectionLifecycleService/GetInspectionLifecycle and hands what it keeps to the sink of its kind. */
suspend fun carryInspectionLifecycleSubject(channel: ManagedChannel) {
    val upstream = InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)
    val got = upstream.getInspectionLifecycle(getInspectionLifecycleRequest { this.inspectionId = "live-carry-inspection-lifecycle-subject" })
    val row = CarryInspectionLifecycleSubjectRecord(
        subject = got.inspection.subject,
        inspectionId = got.inspection.inspectionId,
        correlationId = got.result.correlationId,
        status = normalize(got.result.status),
    )
    val sinks = mapOf<String, CarryInspectionLifecycleSubjectDestination>(
        "prescription-calculation" to CarryInspectionLifecycleSubjectPrescriptionCalculation(PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel)),
        "notification-validation" to CarryInspectionLifecycleSubjectNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
        "audit-event" to CarryInspectionLifecycleSubjectAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "audit-event") continue
        sinks[kind]?.deliver(row)
    }
}

/** Where carryInspectionLifecycleSubject hands each record. */
interface CarryInspectionLifecycleSubjectDestination {
    suspend fun deliver(rec: CarryInspectionLifecycleSubjectRecord)
}

/** What carryInspectionLifecycleSubject keeps of each GetInspectionLifecycle response. */
data class CarryInspectionLifecycleSubjectRecord(val subject: String, val inspectionId: String, val correlationId: String, val status: String)

/** Passes records to acme.prescriptions.v1.PrescriptionCalculationService. */
class CarryInspectionLifecycleSubjectPrescriptionCalculation(private val stub: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub) : CarryInspectionLifecycleSubjectDestination {
    override suspend fun deliver(rec: CarryInspectionLifecycleSubjectRecord) {
        val request = comparePrescriptionOptionsRequest {
            this.prescriptionId = rec.inspectionId
            this.reason = rec.correlationId
        }
        stub.comparePrescriptionOptions(request)
    }
}

/** Passes records to acme.notifications.v1.NotificationValidationService. */
class CarryInspectionLifecycleSubjectNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : CarryInspectionLifecycleSubjectDestination {
    override suspend fun deliver(rec: CarryInspectionLifecycleSubjectRecord) {
        RpcBoundary.run("acme.notifications.v1.NotificationValidationService/ExplainNotificationDecision") { stub.explainNotificationDecision(explainNotificationDecisionRequest { this.notificationId = rec.status }) }
    }
}

/** Hands records to acme.audit.v1.AuditEventService. */
class CarryInspectionLifecycleSubjectAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : CarryInspectionLifecycleSubjectDestination {
    override suspend fun deliver(rec: CarryInspectionLifecycleSubjectRecord) {
        val request = createAuditEventRequest {
            this.auditEvent = auditEvent { this.auditEventId = rec.status }
        }
        stub.createAuditEvent(request)
    }
}
