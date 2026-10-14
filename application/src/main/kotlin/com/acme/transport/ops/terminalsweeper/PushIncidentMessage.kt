package com.acme.transport.ops.terminalsweeper

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.scoreAuditEventRequest
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.updateDriverRequest
import com.acme.itsm.v1.IncidentServiceGrpcKt
import com.acme.itsm.v1.getIncidentRequest
import com.acme.maintenance.v1.LintServiceSchedulesRequestKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.lintServiceSchedulesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.itsm.v1.IncidentService/GetIncident and hands what it keeps to the sink of its kind. */
suspend fun pushIncidentMessage(channel: ManagedChannel) {
    val upstream = IncidentServiceGrpcKt.IncidentServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.getIncident(getIncidentRequest { this.incidentId = "live-push-incident-message" }) }
    val record = PushIncidentMessageRecord(
        message = reply.result.message,
        incidentId = reply.incident.incidentId,
    )
    val sinks = mapOf<String, PushIncidentMessageDestination>(
        "audit-event-validation" to PushIncidentMessageAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        "schedule-validation" to PushIncidentMessageServiceScheduleValidation(ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub(channel)),
        "driver" to PushIncidentMessageDriver(DriverServiceGrpcKt.DriverServiceCoroutineStub(channel)),
    )
    routePushIncidentMessage(sinks, "schedule-validation", record)
}

/** Where pushIncidentMessage hands each record. */
interface PushIncidentMessageDestination {
    suspend fun forward(rec: PushIncidentMessageRecord)
}

/** What pushIncidentMessage keeps of each GetIncident response. */
data class PushIncidentMessageRecord(val message: String, val incidentId: String)

/** Passes records to acme.audit.v1.AuditEventValidationService. */
class PushIncidentMessageAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : PushIncidentMessageDestination {
    override suspend fun forward(rec: PushIncidentMessageRecord) {
        RpcBoundary.run("acme.audit.v1.AuditEventValidationService/ScoreAuditEvent") { stub.scoreAuditEvent(scoreAuditEventRequest { this.auditEvent = auditEvent { this.kind = rec.incidentId } }) }
    }
}

/** Hands records to acme.maintenance.v1.ServiceScheduleValidationService. */
class PushIncidentMessageServiceScheduleValidation(private val stub: ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub) : PushIncidentMessageDestination {
    override suspend fun forward(rec: PushIncidentMessageRecord) {
        val request = lintServiceSchedulesRequest {
            this.details = LintServiceSchedulesRequestKt.details { this.locale = rec.incidentId }
        }
        stub.lintServiceSchedules(request)
    }
}

/** Hands records to acme.drivers.v1.DriverService. */
class PushIncidentMessageDriver(private val stub: DriverServiceGrpcKt.DriverServiceCoroutineStub) : PushIncidentMessageDestination {
    override suspend fun forward(rec: PushIncidentMessageRecord) {
        stub.updateDriver(updateDriverRequest { this.driver = driver { this.driverId = rec.incidentId } })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routePushIncidentMessage(sinks: Map<String, PushIncidentMessageDestination>, kind: String, rec: PushIncidentMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
