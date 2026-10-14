package com.acme.transport.ops.networkmirror

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.searchAuditEventsRequest
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.TerminateDriverRequestKt
import com.acme.drivers.v1.terminateDriverRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.rejectFuelCardRequest
import com.acme.telematics.v1.TripLogWorkflowServiceGrpcKt
import com.acme.telematics.v1.resumeTripLogWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventService/SearchAuditEvents and hands what it keeps to the sink of its kind. */
suspend fun handOffAuditEventsStatus(channel: ManagedChannel) {
    val origin = AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.searchAuditEvents(searchAuditEventsRequest { this.orderBy = "current-hand-off-audit-events-status" }) }
    val rec = HandOffAuditEventsStatusRecord(
        status = found.result.status,
        correlationId = normalize(found.result.correlationId),
        message = found.result.message,
        nextPageToken = found.pageInfo.nextPageToken,
    )
    val sinks = mapOf<String, HandOffAuditEventsStatusSink>(
        "trip-log-workflow" to HandOffAuditEventsStatusTripLogWorkflow(TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub(channel)),
        "driver-lifecycle" to HandOffAuditEventsStatusDriverLifecycle(DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)),
        "fuel-card-approval" to HandOffAuditEventsStatusFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "trip-log-workflow") continue
        sinks[kind]?.hand(rec)
    }
}

/** Where handOffAuditEventsStatus hands each record. */
interface HandOffAuditEventsStatusSink {
    suspend fun hand(rec: HandOffAuditEventsStatusRecord)
}

/** What handOffAuditEventsStatus keeps of each SearchAuditEvents response. */
data class HandOffAuditEventsStatusRecord(val status: String, val correlationId: String, val message: String, val nextPageToken: String)

/** Hands records to acme.telematics.v1.TripLogWorkflowService. */
class HandOffAuditEventsStatusTripLogWorkflow(private val stub: TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub) : HandOffAuditEventsStatusSink {
    override suspend fun hand(rec: HandOffAuditEventsStatusRecord) {
        stub.resumeTripLogWorkflow(resumeTripLogWorkflowRequest { this.reason = rec.status })
    }
}

/** Writes records to acme.drivers.v1.DriverLifecycleService. */
class HandOffAuditEventsStatusDriverLifecycle(private val stub: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub) : HandOffAuditEventsStatusSink {
    override suspend fun hand(rec: HandOffAuditEventsStatusRecord) {
        val request = terminateDriverRequest {
            this.details = TerminateDriverRequestKt.details { this.reasonCode = rec.nextPageToken }
        }
        stub.terminateDriver(request)
    }
}

/** Sends records to acme.fuel.v1.FuelCardApprovalService. */
class HandOffAuditEventsStatusFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : HandOffAuditEventsStatusSink {
    override suspend fun hand(rec: HandOffAuditEventsStatusRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/RejectFuelCard") { stub.rejectFuelCard(rejectFuelCardRequest { this.fuelCard = fuelCard { this.fuelCardId = rec.correlationId } }) }
    }
}
