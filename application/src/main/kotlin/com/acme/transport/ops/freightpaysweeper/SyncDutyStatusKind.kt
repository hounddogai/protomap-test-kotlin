package com.acme.transport.ops.freightpaysweeper

import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.trackDriverHomeRequest
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.activateDutyStatusRequest
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.delegateTripLogApprovalRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.previewYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusLifecycleService/ActivateDutyStatus and hands what it keeps to the sink of its kind. */
suspend fun syncDutyStatusKind(channel: ManagedChannel) {
    val upstream = DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.activateDutyStatus(activateDutyStatusRequest { this.dutyStatusId = "primary-sync-duty-status-kind" }) }
    val record = SyncDutyStatusKindRecord(
        kind = reply.dutyStatus.kind,
        correlationId = normalize(reply.dutyStatus.correlationId),
    )
    val sinks = mapOf<String, SyncDutyStatusKindDestination>(
        "driver-home-gateway" to SyncDutyStatusKindDriverHomeGateway(DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel)),
        "yard-move-validation" to SyncDutyStatusKindYardMoveValidation(YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel)),
        "trip-log-approval" to SyncDutyStatusKindTripLogApproval(TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "driver-home-gateway") continue
        sinks[kind]?.deliver(record)
    }
}

/** Where syncDutyStatusKind hands each record. */
interface SyncDutyStatusKindDestination {
    suspend fun deliver(rec: SyncDutyStatusKindRecord)
}

/** What syncDutyStatusKind keeps of each ActivateDutyStatus response. */
data class SyncDutyStatusKindRecord(val kind: String, val correlationId: String)

/** Sends records to acme.driverapp.v1.DriverHomeGatewayService. */
class SyncDutyStatusKindDriverHomeGateway(private val stub: DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub) : SyncDutyStatusKindDestination {
    override suspend fun deliver(rec: SyncDutyStatusKindRecord) {
        stub.trackDriverHome(trackDriverHomeRequest { this.driverHome = driverHome { this.subject = rec.kind } })
    }
}

/** Hands records to acme.yards.v1.YardMoveValidationService. */
class SyncDutyStatusKindYardMoveValidation(private val stub: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub) : SyncDutyStatusKindDestination {
    override suspend fun deliver(rec: SyncDutyStatusKindRecord) {
        stub.previewYardMove(previewYardMoveRequest { this.yardMoveId = rec.correlationId })
    }
}

/** Writes records to acme.telematics.v1.TripLogApprovalService. */
class SyncDutyStatusKindTripLogApproval(private val stub: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub) : SyncDutyStatusKindDestination {
    override suspend fun deliver(rec: SyncDutyStatusKindRecord) {
        val request = delegateTripLogApprovalRequest {
            this.tripLog = tripLog { this.number = rec.correlationId }
        }
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/DelegateTripLogApproval") { stub.delegateTripLogApproval(request) }
    }
}
