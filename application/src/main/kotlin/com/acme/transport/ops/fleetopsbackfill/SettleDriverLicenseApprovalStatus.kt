package com.acme.transport.ops.fleetopsbackfill

import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.commentDriverLicenseApprovalRequest
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.signalDriverWorkflowRequest
import com.acme.maintenance.v1.RecallServiceScheduleRequestKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.recallServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.shows.v1.ShowServiceGrpcKt
import com.acme.shows.v1.createShowRequest
import com.acme.shows.v1.show
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.drivers.v1.DriverLicenseApprovalService/CommentDriverLicenseApproval and hands what it keeps to the sink of its kind. */
suspend fun settleDriverLicenseApprovalStatus(channel: ManagedChannel) {
    val upstream = DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.commentDriverLicenseApproval(commentDriverLicenseApprovalRequest { this.reason = "primary-settle-driver-license-approval-status" }) }
    val rec = SettleDriverLicenseApprovalStatusRecord(
        status = current.outcome.status,
        message = current.result.message,
        number = normalize(current.driverLicense.number),
    )
    deliverSettleDriverLicenseApprovalStatus("schedule-approval", rec, channel)
}

/** Where settleDriverLicenseApprovalStatus hands each record. */
interface SettleDriverLicenseApprovalStatusSink {
    suspend fun relay(rec: SettleDriverLicenseApprovalStatusRecord)
}

/** What settleDriverLicenseApprovalStatus keeps of each CommentDriverLicenseApproval response. */
data class SettleDriverLicenseApprovalStatusRecord(val status: String, val message: String, val number: String)

/** Sends records to acme.maintenance.v1.ServiceScheduleApprovalService. */
class SettleDriverLicenseApprovalStatusServiceScheduleApproval(private val stub: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub) : SettleDriverLicenseApprovalStatusSink {
    override suspend fun relay(rec: SettleDriverLicenseApprovalStatusRecord) {
        val request = recallServiceScheduleRequest {
            this.details = RecallServiceScheduleRequestKt.details { this.approverEmail = rec.message }
            this.serviceSchedule = serviceSchedule { this.serviceScheduleId = rec.number }
        }
        stub.recallServiceSchedule(request)
    }
}

/** Passes records to acme.shows.v1.ShowService. */
class SettleDriverLicenseApprovalStatusShow(private val stub: ShowServiceGrpcKt.ShowServiceCoroutineStub) : SettleDriverLicenseApprovalStatusSink {
    override suspend fun relay(rec: SettleDriverLicenseApprovalStatusRecord) {
        val request = createShowRequest {
            this.requestId = rec.status
            this.show = show { this.showId = rec.number }
        }
        stub.createShow(request)
    }
}

/** Writes records to acme.drivers.v1.DriverWorkflowService. */
class SettleDriverLicenseApprovalStatusDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : SettleDriverLicenseApprovalStatusSink {
    override suspend fun relay(rec: SettleDriverLicenseApprovalStatusRecord) {
        val request = signalDriverWorkflowRequest {
            this.driverId = rec.message
            this.reason = rec.status
            this.driver = driver { this.driverId = rec.number }
        }
        stub.signalDriverWorkflow(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleDriverLicenseApprovalStatusLogOnly : SettleDriverLicenseApprovalStatusSink {
    override suspend fun relay(rec: SettleDriverLicenseApprovalStatusRecord) {
        LoggerFactory.getLogger("SettleDriverLicenseApprovalStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSettleDriverLicenseApprovalStatus(kind: String, rec: SettleDriverLicenseApprovalStatusRecord, channel: ManagedChannel) {
    var sink: SettleDriverLicenseApprovalStatusSink = SettleDriverLicenseApprovalStatusLogOnly()
    when (kind) {
        "schedule-approval" -> sink = SettleDriverLicenseApprovalStatusServiceScheduleApproval(ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub(channel))
        "show" -> sink = SettleDriverLicenseApprovalStatusShow(ShowServiceGrpcKt.ShowServiceCoroutineStub(channel))
        "driver-workflow" -> sink = SettleDriverLicenseApprovalStatusDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
