package com.acme.transport.ops.terminalsweeper

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionConflictsRequestKt
import com.acme.fuel.v1.listFuelTransactionConflictsRequest
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.rejectStopRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventValidationService/PreviewAuditEvent and hands what it keeps to the sink of its kind. */
suspend fun backfillAuditEventKind(channel: ManagedChannel) {
    val origin = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.previewAuditEvent(previewAuditEventRequest { this.reason = "primary-backfill-audit-event-kind" }) }
    val rec = BackfillAuditEventKindRecord(
        kind = got.auditEvent.kind,
        message = got.outcome.message,
        resultMessage = got.result.message,
    )
    val sinks = mapOf<String, BackfillAuditEventKindTarget>(
        "fuel-transaction-sync" to BackfillAuditEventKindFuelTransactionSync(FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub(channel)),
        "stop-approval" to BackfillAuditEventKindStopApproval(StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("BACKFILL_AUDIT_EVENT_KIND_TARGET")] ?: return
    sink.relay(rec)
}

/** Where backfillAuditEventKind hands each record. */
interface BackfillAuditEventKindTarget {
    suspend fun relay(rec: BackfillAuditEventKindRecord)
}

/** What backfillAuditEventKind keeps of each PreviewAuditEvent response. */
data class BackfillAuditEventKindRecord(val kind: String, val message: String, val resultMessage: String)

/** Sends records to acme.fuel.v1.FuelTransactionSyncService. */
class BackfillAuditEventKindFuelTransactionSync(private val stub: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub) : BackfillAuditEventKindTarget {
    override suspend fun relay(rec: BackfillAuditEventKindRecord) {
        val request = listFuelTransactionConflictsRequest {
            this.orderBy = rec.kind
            this.filter = ListFuelTransactionConflictsRequestKt.filter { this.region = rec.message; this.query = rec.resultMessage }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/ListFuelTransactionConflicts") { stub.listFuelTransactionConflicts(request) }
    }
}

/** Sends records to acme.routing.v1.StopApprovalService. */
class BackfillAuditEventKindStopApproval(private val stub: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub) : BackfillAuditEventKindTarget {
    override suspend fun relay(rec: BackfillAuditEventKindRecord) {
        RpcBoundary.run("acme.routing.v1.StopApprovalService/RejectStop") { stub.rejectStop(rejectStopRequest { this.stopId = rec.resultMessage }) }
    }
}
