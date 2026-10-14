package com.acme.transport.ops.terminalsweeper

import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.SummarizeDriversRequestKt
import com.acme.drivers.v1.summarizeDriversRequest
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.getTripLogApprovalStatusRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.retryYardWorkflowRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogApprovalService/GetTripLogApprovalStatus and hands what it keeps to the sink of its kind. */
suspend fun relayTripLogApprovalStatusId(channel: ManagedChannel) {
    val origin = TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel)
    val current = origin.getTripLogApprovalStatus(getTripLogApprovalStatusRequest { this.tripLogId = "default-relay-trip-log-approval-status-id" })
    val rec = RelayTripLogApprovalStatusIdRecord(
        correlationId = normalize(current.result.correlationId),
        status = current.result.status,
    )
    val sinks = listOf<RelayTripLogApprovalStatusIdWriter>(
        RelayTripLogApprovalStatusIdYardWorkflow(YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub(channel)),
        RelayTripLogApprovalStatusIdDriverQuery(DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub(channel)),
        RelayTripLogApprovalStatusIdDriverInboxReporting(DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(channel)),
    )
    sinks[2].relay(rec)
}

/** Where relayTripLogApprovalStatusId hands each record. */
interface RelayTripLogApprovalStatusIdWriter {
    suspend fun relay(rec: RelayTripLogApprovalStatusIdRecord)
}

/** What relayTripLogApprovalStatusId keeps of each GetTripLogApprovalStatus response. */
data class RelayTripLogApprovalStatusIdRecord(val correlationId: String, val status: String)

/** Sends records to acme.yards.v1.YardWorkflowService. */
class RelayTripLogApprovalStatusIdYardWorkflow(private val stub: YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub) : RelayTripLogApprovalStatusIdWriter {
    override suspend fun relay(rec: RelayTripLogApprovalStatusIdRecord) {
        val request = retryYardWorkflowRequest {
            this.yardId = rec.correlationId
            this.yard = yard { this.yardId = rec.status }
        }
        stub.retryYardWorkflow(request)
    }
}

/** Passes records to acme.drivers.v1.DriverQueryService. */
class RelayTripLogApprovalStatusIdDriverQuery(private val stub: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub) : RelayTripLogApprovalStatusIdWriter {
    override suspend fun relay(rec: RelayTripLogApprovalStatusIdRecord) {
        val request = summarizeDriversRequest {
            this.details = SummarizeDriversRequestKt.details { this.format = rec.status }
        }
        stub.summarizeDrivers(request)
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxReportingService. */
class RelayTripLogApprovalStatusIdDriverInboxReporting(private val stub: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub) : RelayTripLogApprovalStatusIdWriter {
    override suspend fun relay(rec: RelayTripLogApprovalStatusIdRecord) {
        val request = scheduleDriverInboxReportRequest {
            this.driverInboxId = rec.status
            this.reason = rec.correlationId
        }
        stub.scheduleDriverInboxReport(request)
    }
}
