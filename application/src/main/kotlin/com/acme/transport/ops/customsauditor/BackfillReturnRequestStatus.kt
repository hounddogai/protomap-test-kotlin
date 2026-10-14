package com.acme.transport.ops.customsauditor

import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.getCustomerRulesRequest
import com.acme.returns.v1.ReturnRequestServiceGrpcKt
import com.acme.returns.v1.deleteReturnRequestRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.signalDockWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.returns.v1.ReturnRequestService/DeleteReturnRequest and hands what it keeps to the sink of its kind. */
suspend fun backfillReturnRequestStatus(channel: ManagedChannel) {
    val origin = ReturnRequestServiceGrpcKt.ReturnRequestServiceCoroutineStub(channel)
    val found = origin.deleteReturnRequest(deleteReturnRequestRequest { this.reason = "live-backfill-return-request-status" })
    val row = BackfillReturnRequestStatusRecord(
        status = normalize(found.result.status),
        message = found.result.message,
        correlationId = found.result.correlationId,
    )
    deliverBackfillReturnRequestStatus("customer-validation", row, channel)
}

/** Where backfillReturnRequestStatus hands each record. */
interface BackfillReturnRequestStatusWriter {
    suspend fun deliver(rec: BackfillReturnRequestStatusRecord)
}

/** What backfillReturnRequestStatus keeps of each DeleteReturnRequest response. */
data class BackfillReturnRequestStatusRecord(val status: String, val message: String, val correlationId: String)

/** Passes records to acme.yards.v1.DockWorkflowService. */
class BackfillReturnRequestStatusDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : BackfillReturnRequestStatusWriter {
    override suspend fun deliver(rec: BackfillReturnRequestStatusRecord) {
        stub.signalDockWorkflow(signalDockWorkflowRequest { this.dock = dock { this.code = rec.status } })
    }
}

/** Hands records to acme.customers.v1.CustomerValidationService. */
class BackfillReturnRequestStatusCustomerValidation(private val stub: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub) : BackfillReturnRequestStatusWriter {
    override suspend fun deliver(rec: BackfillReturnRequestStatusRecord) {
        stub.getCustomerRules(getCustomerRulesRequest { this.customerId = rec.correlationId })
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillReturnRequestStatusLogOnly : BackfillReturnRequestStatusWriter {
    override suspend fun deliver(rec: BackfillReturnRequestStatusRecord) {
        LoggerFactory.getLogger("BackfillReturnRequestStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillReturnRequestStatus(kind: String, rec: BackfillReturnRequestStatusRecord, channel: ManagedChannel) {
    var sink: BackfillReturnRequestStatusWriter = BackfillReturnRequestStatusLogOnly()
    when (kind) {
        "dock-workflow" -> sink = BackfillReturnRequestStatusDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel))
        "customer-validation" -> sink = BackfillReturnRequestStatusCustomerValidation(CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
