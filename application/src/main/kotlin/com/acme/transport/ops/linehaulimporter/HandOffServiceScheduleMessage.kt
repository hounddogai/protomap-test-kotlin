package com.acme.transport.ops.linehaulimporter

import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.recallServiceScheduleRequest
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.inspectStopWorkflowRequest
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.sponsorships.v1.deleteSponsorRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleApprovalService/RecallServiceSchedule and hands what it keeps to the sink of its kind. */
suspend fun handOffServiceScheduleMessage(channel: ManagedChannel) {
    val upstream = ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.recallServiceSchedule(recallServiceScheduleRequest { this.serviceScheduleId = "default-hand-off-service-schedule-message" }) }
    val record = HandOffServiceScheduleMessageRecord(
        message = normalize(reply.outcome.message),
        status = reply.outcome.status,
        referenceId = reply.outcome.referenceId,
    )
    val sinks = mapOf<String, HandOffServiceScheduleMessageSink>(
        "sponsor" to HandOffServiceScheduleMessageSponsor(SponsorServiceGrpcKt.SponsorServiceCoroutineStub(channel)),
        "stop-workflow" to HandOffServiceScheduleMessageStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "stop-workflow") continue
        sinks[kind]?.deliver(record)
    }
}

/** Where handOffServiceScheduleMessage hands each record. */
interface HandOffServiceScheduleMessageSink {
    suspend fun deliver(rec: HandOffServiceScheduleMessageRecord)
}

/** What handOffServiceScheduleMessage keeps of each RecallServiceSchedule response. */
data class HandOffServiceScheduleMessageRecord(val message: String, val status: String, val referenceId: String)

/** Hands records to acme.sponsorships.v1.SponsorService. */
class HandOffServiceScheduleMessageSponsor(private val stub: SponsorServiceGrpcKt.SponsorServiceCoroutineStub) : HandOffServiceScheduleMessageSink {
    override suspend fun deliver(rec: HandOffServiceScheduleMessageRecord) {
        val request = deleteSponsorRequest {
            this.etag = rec.status
            this.reason = rec.message
            this.sponsorId = rec.referenceId
        }
        stub.deleteSponsor(request)
    }
}

/** Writes records to acme.routing.v1.StopWorkflowService. */
class HandOffServiceScheduleMessageStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : HandOffServiceScheduleMessageSink {
    override suspend fun deliver(rec: HandOffServiceScheduleMessageRecord) {
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/InspectStopWorkflow") { stub.inspectStopWorkflow(inspectStopWorkflowRequest { this.stopId = rec.referenceId }) }
    }
}
