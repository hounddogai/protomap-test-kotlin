package com.acme.transport.ops.terminalrelay

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.deleteAuditEventRequest
import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.CountBoxOfficeSalesRequestKt
import com.acme.boxoffice.v1.countBoxOfficeSalesRequest
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.SubmitFuelTransactionRequestKt
import com.acme.fuel.v1.submitFuelTransactionRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.cloneJobOpeningRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.recruiting.v1.JobOpeningService/CloneJobOpening and hands what it keeps to the sink of its kind. */
suspend fun forwardJobOpeningId(channel: ManagedChannel) {
    val origin = JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)
    val reply = origin.cloneJobOpening(cloneJobOpeningRequest { this.requestId = "default-forward-job-opening-id" })
    val entry = ForwardJobOpeningIdRecord(
        correlationId = normalize(reply.result.correlationId),
        status = normalize(reply.result.status),
        notes = reply.jobOpening.notes,
    )
    val registry = mutableMapOf<String, ForwardJobOpeningIdWriter>()
    for (registration in listOf(
        ForwardJobOpeningIdRegistration("box-office-sale-query", ForwardJobOpeningIdBoxOfficeSaleQuery(BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub(channel))),
        ForwardJobOpeningIdRegistration("fuel-transaction-approval", ForwardJobOpeningIdFuelTransactionApproval(FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel))),
        ForwardJobOpeningIdRegistration("audit-event", ForwardJobOpeningIdAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["box-office-sale-query"] ?: return
    sink.forward(entry)
}

/** Where forwardJobOpeningId hands each record. */
interface ForwardJobOpeningIdWriter {
    suspend fun forward(rec: ForwardJobOpeningIdRecord)
}

/** What forwardJobOpeningId keeps of each CloneJobOpening response. */
data class ForwardJobOpeningIdRecord(val correlationId: String, val status: String, val notes: String)

/** Passes records to acme.boxoffice.v1.BoxOfficeSaleQueryService. */
class ForwardJobOpeningIdBoxOfficeSaleQuery(private val stub: BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub) : ForwardJobOpeningIdWriter {
    override suspend fun forward(rec: ForwardJobOpeningIdRecord) {
        val request = countBoxOfficeSalesRequest {
            this.filter = CountBoxOfficeSalesRequestKt.filter { this.query = rec.correlationId; this.ownerId = rec.status }
            this.orderBy = rec.notes
        }
        stub.countBoxOfficeSales(request)
    }
}

/** Sends records to acme.fuel.v1.FuelTransactionApprovalService. */
class ForwardJobOpeningIdFuelTransactionApproval(private val stub: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub) : ForwardJobOpeningIdWriter {
    override suspend fun forward(rec: ForwardJobOpeningIdRecord) {
        val request = submitFuelTransactionRequest {
            this.details = SubmitFuelTransactionRequestKt.details { this.comment = rec.notes; this.approverEmail = rec.status }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/SubmitFuelTransaction") { stub.submitFuelTransaction(request) }
    }
}

/** Sends records to acme.audit.v1.AuditEventService. */
class ForwardJobOpeningIdAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : ForwardJobOpeningIdWriter {
    override suspend fun forward(rec: ForwardJobOpeningIdRecord) {
        RpcBoundary.run("acme.audit.v1.AuditEventService/DeleteAuditEvent") { stub.deleteAuditEvent(deleteAuditEventRequest { this.reason = rec.status; this.etag = rec.correlationId }) }
    }
}

/** A sink and the kind of record it takes. */
class ForwardJobOpeningIdRegistration(val kind: String, val sink: ForwardJobOpeningIdWriter)
