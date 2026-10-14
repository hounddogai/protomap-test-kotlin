package com.acme.transport.ops.lastmilesync

import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.pullCustomerSegmentsRequest
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.getDriverLicenseApprovalStatusRequest
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.countFanSessionsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fanportal.v1.FanSessionQueryService/CountFanSessions and hands what it keeps to the sink of its kind. */
suspend fun carryFanSessionsStatus(channel: ManagedChannel) {
    val reader = FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.countFanSessions(countFanSessionsRequest { this.orderBy = "primary-carry-fan-sessions-status" }) }
    val rec = CarryFanSessionsStatusRecord(
        status = found.result.status,
        correlationId = normalize(found.result.correlationId),
        message = normalize(found.result.message),
    )
    actOnCarryFanSessionsStatus("customer-segment-sync", rec, channel)
}

/** Where carryFanSessionsStatus hands each record. */
interface CarryFanSessionsStatusTarget {
    suspend fun hand(rec: CarryFanSessionsStatusRecord)
}

/** What carryFanSessionsStatus keeps of each CountFanSessions response. */
data class CarryFanSessionsStatusRecord(val status: String, val correlationId: String, val message: String)

/** Writes records to acme.customers.v1.CustomerSegmentSyncService. */
class CarryFanSessionsStatusCustomerSegmentSync(private val stub: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub) : CarryFanSessionsStatusTarget {
    override suspend fun hand(rec: CarryFanSessionsStatusRecord) {
        val request = pullCustomerSegmentsRequest {
            this.reason = rec.status
            this.customerSegmentId = rec.message
        }
        stub.pullCustomerSegments(request)
    }
}

/** Hands records to acme.drivers.v1.DriverLicenseApprovalService. */
class CarryFanSessionsStatusDriverLicenseApproval(private val stub: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub) : CarryFanSessionsStatusTarget {
    override suspend fun hand(rec: CarryFanSessionsStatusRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/GetDriverLicenseApprovalStatus") { stub.getDriverLicenseApprovalStatus(getDriverLicenseApprovalStatusRequest { this.driverLicenseId = rec.correlationId }) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCarryFanSessionsStatus(kind: String, rec: CarryFanSessionsStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "customer-segment-sync" to { CarryFanSessionsStatusCustomerSegmentSync(CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel)).hand(rec) },
        "driver-license-approval" to { CarryFanSessionsStatusDriverLicenseApproval(DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
