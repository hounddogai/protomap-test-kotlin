package com.acme.transport.ops.terminalrelay

import com.acme.driverapp.v1.CommentDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.RenewDriverRequestKt
import com.acme.drivers.v1.listPendingDriverLicensesRequest
import com.acme.drivers.v1.renewDriverRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DriverLicenseApprovalService/ListPendingDriverLicenses and hands what it keeps to the sink of its kind. */
suspend fun handOffPendingDriverLicensesId(channel: ManagedChannel) {
    val reader = DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel)
    val got = reader.listPendingDriverLicenses(listPendingDriverLicensesRequest { this.orderBy = "current-hand-off-pending-driver-licenses-id" })
    val entry = HandOffPendingDriverLicensesIdRecord(
        correlationId = got.result.correlationId,
        status = normalize(got.result.status),
    )
    val sinks = listOf<HandOffPendingDriverLicensesIdDestination>(
        HandOffPendingDriverLicensesIdDriverInboxApproval(DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub(channel)),
        HandOffPendingDriverLicensesIdDriverLifecycle(DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)),
    )
    sinks[1].hand(entry)
}

/** Where handOffPendingDriverLicensesId hands each record. */
interface HandOffPendingDriverLicensesIdDestination {
    suspend fun hand(rec: HandOffPendingDriverLicensesIdRecord)
}

/** What handOffPendingDriverLicensesId keeps of each ListPendingDriverLicenses response. */
data class HandOffPendingDriverLicensesIdRecord(val correlationId: String, val status: String)

/** Hands records to acme.driverapp.v1.DriverInboxApprovalService. */
class HandOffPendingDriverLicensesIdDriverInboxApproval(private val stub: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub) : HandOffPendingDriverLicensesIdDestination {
    override suspend fun hand(rec: HandOffPendingDriverLicensesIdRecord) {
        val request = commentDriverInboxApprovalRequest {
            this.details = CommentDriverInboxApprovalRequestKt.details { this.comment = rec.status }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/CommentDriverInboxApproval") { stub.commentDriverInboxApproval(request) }
    }
}

/** Writes records to acme.drivers.v1.DriverLifecycleService. */
class HandOffPendingDriverLicensesIdDriverLifecycle(private val stub: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub) : HandOffPendingDriverLicensesIdDestination {
    override suspend fun hand(rec: HandOffPendingDriverLicensesIdRecord) {
        val request = renewDriverRequest {
            this.reason = rec.correlationId
            this.details = RenewDriverRequestKt.details { this.reasonCode = rec.status }
        }
        stub.renewDriver(request)
    }
}
