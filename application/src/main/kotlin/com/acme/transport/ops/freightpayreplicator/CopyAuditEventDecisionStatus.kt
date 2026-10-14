package com.acme.transport.ops.freightpayreplicator

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.explainAuditEventDecisionRequest
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.advanceDriverHomeWorkflowRequest
import com.acme.driverapp.v1.driverHome
import com.acme.maintenance.v1.PartApprovalServiceGrpcKt
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.rejectPartRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventValidationService/ExplainAuditEventDecision and hands what it keeps to the sink of its kind. */
suspend fun copyAuditEventDecisionStatus(channel: ManagedChannel) {
    val source = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.explainAuditEventDecision(explainAuditEventDecisionRequest { this.reason = "default-copy-audit-event-decision-status" }) }
    val record = CopyAuditEventDecisionStatusRecord(
        status = normalize(got.result.status),
        message = got.outcome.message,
        kind = normalize(got.auditEvent.kind),
        correlationId = got.auditEvent.correlationId,
    )
    val sinks = mapOf<String, CopyAuditEventDecisionStatusDestination>(
        "audit-event-validation" to CopyAuditEventDecisionStatusAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        "driver-home-workflow" to CopyAuditEventDecisionStatusDriverHomeWorkflow(DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(channel)),
        "part-approval" to CopyAuditEventDecisionStatusPartApproval(PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("COPY_AUDIT_EVENT_DECISION_STATUS_TARGET")] ?: return
    sink.relay(record)
}

/** Where copyAuditEventDecisionStatus hands each record. */
interface CopyAuditEventDecisionStatusDestination {
    suspend fun relay(rec: CopyAuditEventDecisionStatusRecord)
}

/** What copyAuditEventDecisionStatus keeps of each ExplainAuditEventDecision response. */
data class CopyAuditEventDecisionStatusRecord(val status: String, val message: String, val kind: String, val correlationId: String)

/** Hands records to acme.audit.v1.AuditEventValidationService. */
class CopyAuditEventDecisionStatusAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : CopyAuditEventDecisionStatusDestination {
    override suspend fun relay(rec: CopyAuditEventDecisionStatusRecord) {
        val request = validateAuditEventRequest {
            this.auditEventId = rec.status
            this.auditEvent = auditEvent { this.source = rec.message; this.auditEventId = rec.correlationId }
        }
        stub.validateAuditEvent(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverHomeWorkflowService. */
class CopyAuditEventDecisionStatusDriverHomeWorkflow(private val stub: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub) : CopyAuditEventDecisionStatusDestination {
    override suspend fun relay(rec: CopyAuditEventDecisionStatusRecord) {
        val request = advanceDriverHomeWorkflowRequest {
            this.reason = rec.kind
            this.driverHome = driverHome { this.subject = rec.status }
            this.driverHomeId = rec.message
        }
        stub.advanceDriverHomeWorkflow(request)
    }
}

/** Writes records to acme.maintenance.v1.PartApprovalService. */
class CopyAuditEventDecisionStatusPartApproval(private val stub: PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineStub) : CopyAuditEventDecisionStatusDestination {
    override suspend fun relay(rec: CopyAuditEventDecisionStatusRecord) {
        val request = rejectPartRequest {
            this.part = part { this.code = rec.message; this.name = rec.correlationId }
            this.partId = rec.kind
        }
        stub.rejectPart(request)
    }
}
