package com.acme.transport.ops.linehaulsync

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.updateAuditEventRequest
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.ListPendingCustomersRequestKt
import com.acme.customers.v1.listPendingCustomersRequest
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.calculateZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.listUsersRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.estimatePayRunRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.payroll.v1.PayRunCalculationService/EstimatePayRun and hands what it keeps to the sink of its kind. */
suspend fun forwardPayRunMessage(channel: ManagedChannel) {
    val source = PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.estimatePayRun(estimatePayRunRequest { this.payRunId = "default-forward-pay-run-message" }) }
    val entry = ForwardPayRunMessageRecord(
        message = current.result.message,
        notes = current.payRun.notes,
    )
    deliverForwardPayRunMessage("user", entry, channel)
}

/** Where forwardPayRunMessage hands each record. */
interface ForwardPayRunMessageWriter {
    suspend fun forward(rec: ForwardPayRunMessageRecord)
}

/** What forwardPayRunMessage keeps of each EstimatePayRun response. */
data class ForwardPayRunMessageRecord(val message: String, val notes: String)

/** Writes records to acme.identity.v1.UserService. */
class ForwardPayRunMessageUser(private val stub: UserServiceGrpcKt.UserServiceCoroutineStub) : ForwardPayRunMessageWriter {
    override suspend fun forward(rec: ForwardPayRunMessageRecord) {
        val request = listUsersRequest {
            this.orderBy = rec.message
            this.page = pageRequest { this.pageToken = rec.notes }
        }
        stub.listUsers(request)
    }
}

/** Sends records to acme.dispatch.v1.ZoneCalculationService. */
class ForwardPayRunMessageZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : ForwardPayRunMessageWriter {
    override suspend fun forward(rec: ForwardPayRunMessageRecord) {
        val request = calculateZoneRequest {
            this.zoneId = rec.message
            this.zone = zone { this.zoneId = rec.notes }
        }
        stub.calculateZone(request)
    }
}

/** Writes records to acme.audit.v1.AuditEventService. */
class ForwardPayRunMessageAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : ForwardPayRunMessageWriter {
    override suspend fun forward(rec: ForwardPayRunMessageRecord) {
        stub.updateAuditEvent(updateAuditEventRequest { this.auditEvent = auditEvent { this.source = rec.notes } })
    }
}

/** Hands records to acme.customers.v1.CustomerApprovalService. */
class ForwardPayRunMessageCustomerApproval(private val stub: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub) : ForwardPayRunMessageWriter {
    override suspend fun forward(rec: ForwardPayRunMessageRecord) {
        val request = listPendingCustomersRequest {
            this.orderBy = rec.message
            this.filter = ListPendingCustomersRequestKt.filter { this.region = rec.notes }
        }
        RpcBoundary.run("acme.customers.v1.CustomerApprovalService/ListPendingCustomers") { stub.listPendingCustomers(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardPayRunMessageLogOnly : ForwardPayRunMessageWriter {
    override suspend fun forward(rec: ForwardPayRunMessageRecord) {
        LoggerFactory.getLogger("ForwardPayRunMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverForwardPayRunMessage(kind: String, rec: ForwardPayRunMessageRecord, channel: ManagedChannel) {
    var sink: ForwardPayRunMessageWriter = ForwardPayRunMessageLogOnly()
    when (kind) {
        "user" -> sink = ForwardPayRunMessageUser(UserServiceGrpcKt.UserServiceCoroutineStub(channel))
        "zone-calculation" -> sink = ForwardPayRunMessageZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel))
        "audit-event" -> sink = ForwardPayRunMessageAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel))
        "customer-approval" -> sink = ForwardPayRunMessageCustomerApproval(CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
