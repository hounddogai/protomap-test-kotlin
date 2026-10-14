package com.acme.transport.ops.lastmilebackfill

import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.findUsersRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.submitTelemetryReadingRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.generateYardMoveReportRequest
import io.grpc.ManagedChannel

/** Reads acme.identity.v1.UserQueryService/FindUsers and hands what it keeps to the sink of its kind. */
suspend fun replayUsersId(channel: ManagedChannel) {
    val origin = UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.findUsers(findUsersRequest { this.orderBy = "current-replay-users-id" }) }
    val record = ReplayUsersIdRecord(
        correlationId = current.result.correlationId,
        status = normalize(current.result.status),
    )
    val sinks = listOf<ReplayUsersIdTarget>(
        ReplayUsersIdTelemetryReadingApproval(TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel)),
        ReplayUsersIdYardMoveReporting(YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(channel)),
    )
    sinks[1].transmit(record)
}

/** Where replayUsersId hands each record. */
interface ReplayUsersIdTarget {
    suspend fun transmit(rec: ReplayUsersIdRecord)
}

/** What replayUsersId keeps of each FindUsers response. */
data class ReplayUsersIdRecord(val correlationId: String, val status: String)

/** Passes records to acme.telematics.v1.TelemetryReadingApprovalService. */
class ReplayUsersIdTelemetryReadingApproval(private val stub: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub) : ReplayUsersIdTarget {
    override suspend fun transmit(rec: ReplayUsersIdRecord) {
        val request = submitTelemetryReadingRequest {
            this.telemetryReading = telemetryReading { this.kind = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/SubmitTelemetryReading") { stub.submitTelemetryReading(request) }
    }
}

/** Sends records to acme.yards.v1.YardMoveReportingService. */
class ReplayUsersIdYardMoveReporting(private val stub: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub) : ReplayUsersIdTarget {
    override suspend fun transmit(rec: ReplayUsersIdRecord) {
        val request = generateYardMoveReportRequest {
            this.yardMoveId = rec.status
            this.reason = rec.correlationId
        }
        stub.generateYardMoveReport(request)
    }
}
