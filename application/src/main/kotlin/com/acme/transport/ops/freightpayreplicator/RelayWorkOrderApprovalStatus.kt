package com.acme.transport.ops.freightpayreplicator

import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.rejectDispatchOrderRequest
import com.acme.fuel.v1.DelegateFuelTransactionApprovalRequestKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.delegateFuelTransactionApprovalRequest
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.commentWorkOrderApprovalRequest
import com.acme.telematics.v1.ShareTripLogReportRequestKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.shareTripLogReportRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.WorkOrderApprovalService/CommentWorkOrderApproval and hands what it keeps to the sink of its kind. */
suspend fun relayWorkOrderApprovalStatus(channel: ManagedChannel) {
    val origin = WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel)
    val got = origin.commentWorkOrderApproval(commentWorkOrderApprovalRequest { this.reason = "primary-relay-work-order-approval-status" })
    val row = RelayWorkOrderApprovalStatusRecord(
        status = got.result.status,
        subject = normalize(got.workOrder.subject),
        referenceId = got.outcome.referenceId,
        message = got.result.message,
    )
    deliverRelayWorkOrderApprovalStatus("trip-log-reporting", row, channel)
}

/** Where relayWorkOrderApprovalStatus hands each record. */
interface RelayWorkOrderApprovalStatusDestination {
    suspend fun write(rec: RelayWorkOrderApprovalStatusRecord)
}

/** What relayWorkOrderApprovalStatus keeps of each CommentWorkOrderApproval response. */
data class RelayWorkOrderApprovalStatusRecord(val status: String, val subject: String, val referenceId: String, val message: String)

/** Passes records to acme.fuel.v1.FuelTransactionApprovalService. */
class RelayWorkOrderApprovalStatusFuelTransactionApproval(private val stub: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub) : RelayWorkOrderApprovalStatusDestination {
    override suspend fun write(rec: RelayWorkOrderApprovalStatusRecord) {
        val request = delegateFuelTransactionApprovalRequest {
            this.details = DelegateFuelTransactionApprovalRequestKt.details { this.comment = rec.message }
        }
        stub.delegateFuelTransactionApproval(request)
    }
}

/** Sends records to acme.dispatch.v1.DispatchOrderApprovalService. */
class RelayWorkOrderApprovalStatusDispatchOrderApproval(private val stub: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub) : RelayWorkOrderApprovalStatusDestination {
    override suspend fun write(rec: RelayWorkOrderApprovalStatusRecord) {
        val request = rejectDispatchOrderRequest {
            this.dispatchOrder = dispatchOrder { this.number = rec.status }
            this.dispatchOrderId = rec.message
        }
        stub.rejectDispatchOrder(request)
    }
}

/** Sends records to acme.telematics.v1.TripLogReportingService. */
class RelayWorkOrderApprovalStatusTripLogReporting(private val stub: TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub) : RelayWorkOrderApprovalStatusDestination {
    override suspend fun write(rec: RelayWorkOrderApprovalStatusRecord) {
        val request = shareTripLogReportRequest {
            this.reason = rec.referenceId
            this.details = ShareTripLogReportRequestKt.details { this.channel = rec.message }
        }
        stub.shareTripLogReport(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayWorkOrderApprovalStatusLogOnly : RelayWorkOrderApprovalStatusDestination {
    override suspend fun write(rec: RelayWorkOrderApprovalStatusRecord) {
        LoggerFactory.getLogger("RelayWorkOrderApprovalStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayWorkOrderApprovalStatus(kind: String, rec: RelayWorkOrderApprovalStatusRecord, channel: ManagedChannel) {
    var sink: RelayWorkOrderApprovalStatusDestination = RelayWorkOrderApprovalStatusLogOnly()
    when (kind) {
        "fuel-transaction-approval" -> sink = RelayWorkOrderApprovalStatusFuelTransactionApproval(FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel))
        "dispatch-order-approval" -> sink = RelayWorkOrderApprovalStatusDispatchOrderApproval(DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(channel))
        "trip-log-reporting" -> sink = RelayWorkOrderApprovalStatusTripLogReporting(TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
