package com.acme.transport.ops.terminalrelay

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.ValidateAuditEventRequestKt
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.compareWorkOrderOptionsRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.estimatePrescriptionRequest
import com.acme.prescriptions.v1.prescription
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.summarizeSeatMapsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderCalculationService/CompareWorkOrderOptions and hands what it keeps to the sink of its kind. */
suspend fun mirrorWorkOrderOptionsCode(channel: ManagedChannel) {
    val upstream = WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel)
    val got = upstream.compareWorkOrderOptions(compareWorkOrderOptionsRequest { this.reason = "live-mirror-work-order-options-code" })
    val rec = MirrorWorkOrderOptionsCodeRecord(
        currencyCode = got.breakdown.currencyCode,
        workOrderId = got.workOrder.workOrderId,
        subject = got.workOrder.subject,
    )
    val registry = mutableMapOf<String, MirrorWorkOrderOptionsCodeWriter>()
    for (registration in listOf(
        MirrorWorkOrderOptionsCodeRegistration("audit-event-validation", MirrorWorkOrderOptionsCodeAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel))),
        MirrorWorkOrderOptionsCodeRegistration("prescription-calculation", MirrorWorkOrderOptionsCodePrescriptionCalculation(PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel))),
        MirrorWorkOrderOptionsCodeRegistration("seat-map-query", MirrorWorkOrderOptionsCodeSeatMapQuery(SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["prescription-calculation"] ?: return
    sink.transmit(rec)
}

/** Where mirrorWorkOrderOptionsCode hands each record. */
interface MirrorWorkOrderOptionsCodeWriter {
    suspend fun transmit(rec: MirrorWorkOrderOptionsCodeRecord)
}

/** What mirrorWorkOrderOptionsCode keeps of each CompareWorkOrderOptions response. */
data class MirrorWorkOrderOptionsCodeRecord(val currencyCode: String, val workOrderId: String, val subject: String)

/** Hands records to acme.audit.v1.AuditEventValidationService. */
class MirrorWorkOrderOptionsCodeAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : MirrorWorkOrderOptionsCodeWriter {
    override suspend fun transmit(rec: MirrorWorkOrderOptionsCodeRecord) {
        val request = validateAuditEventRequest {
            this.reason = rec.currencyCode
            this.details = ValidateAuditEventRequestKt.details { this.locale = rec.workOrderId }
            this.auditEventId = rec.subject
        }
        stub.validateAuditEvent(request)
    }
}

/** Hands records to acme.prescriptions.v1.PrescriptionCalculationService. */
class MirrorWorkOrderOptionsCodePrescriptionCalculation(private val stub: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub) : MirrorWorkOrderOptionsCodeWriter {
    override suspend fun transmit(rec: MirrorWorkOrderOptionsCodeRecord) {
        val request = estimatePrescriptionRequest {
            this.prescription = prescription { this.notes = rec.currencyCode; this.number = rec.workOrderId }
        }
        RpcBoundary.run("acme.prescriptions.v1.PrescriptionCalculationService/EstimatePrescription") { stub.estimatePrescription(request) }
    }
}

/** Sends records to acme.seating.v1.SeatMapQueryService. */
class MirrorWorkOrderOptionsCodeSeatMapQuery(private val stub: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub) : MirrorWorkOrderOptionsCodeWriter {
    override suspend fun transmit(rec: MirrorWorkOrderOptionsCodeRecord) {
        stub.summarizeSeatMaps(summarizeSeatMapsRequest { this.reason = rec.workOrderId })
    }
}

/** A sink and the kind of record it takes. */
class MirrorWorkOrderOptionsCodeRegistration(val kind: String, val sink: MirrorWorkOrderOptionsCodeWriter)
