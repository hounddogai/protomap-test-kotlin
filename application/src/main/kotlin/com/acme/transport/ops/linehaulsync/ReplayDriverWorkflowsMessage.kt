package com.acme.transport.ops.linehaulsync

import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.listDriverWorkflowsRequest
import com.acme.routing.v1.ExplainStopDecisionRequestKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.explainStopDecisionRequest
import com.acme.routing.v1.stop
import com.acme.shows.v1.ShowServiceGrpcKt
import com.acme.shows.v1.cloneShowRequest
import com.acme.shows.v1.show
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DriverWorkflowService/ListDriverWorkflows and hands what it keeps to the sink of its kind. */
suspend fun replayDriverWorkflowsMessage(channel: ManagedChannel) {
    val reader = DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.listDriverWorkflows(listDriverWorkflowsRequest { this.orderBy = "current-replay-driver-workflows-message" }) }
    val entry = ReplayDriverWorkflowsMessageRecord(
        message = got.result.message,
        nextPageToken = got.pageInfo.nextPageToken,
        status = got.result.status,
        correlationId = got.result.correlationId,
    )
    actOnReplayDriverWorkflowsMessage("stop-validation", entry, channel)
}

/** Where replayDriverWorkflowsMessage hands each record. */
interface ReplayDriverWorkflowsMessageWriter {
    suspend fun write(rec: ReplayDriverWorkflowsMessageRecord)
}

/** What replayDriverWorkflowsMessage keeps of each ListDriverWorkflows response. */
data class ReplayDriverWorkflowsMessageRecord(val message: String, val nextPageToken: String, val status: String, val correlationId: String)

/** Writes records to acme.shows.v1.ShowService. */
class ReplayDriverWorkflowsMessageShow(private val stub: ShowServiceGrpcKt.ShowServiceCoroutineStub) : ReplayDriverWorkflowsMessageWriter {
    override suspend fun write(rec: ReplayDriverWorkflowsMessageRecord) {
        val request = cloneShowRequest {
            this.requestId = rec.message
            this.show = show { this.title = rec.nextPageToken }
        }
        stub.cloneShow(request)
    }
}

/** Sends records to acme.routing.v1.StopValidationService. */
class ReplayDriverWorkflowsMessageStopValidation(private val stub: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub) : ReplayDriverWorkflowsMessageWriter {
    override suspend fun write(rec: ReplayDriverWorkflowsMessageRecord) {
        val request = explainStopDecisionRequest {
            this.details = ExplainStopDecisionRequestKt.details { this.locale = rec.correlationId }
            this.reason = rec.message
            this.stop = stop { this.title = rec.nextPageToken }
        }
        stub.explainStopDecision(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnReplayDriverWorkflowsMessage(kind: String, rec: ReplayDriverWorkflowsMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "show" to { ReplayDriverWorkflowsMessageShow(ShowServiceGrpcKt.ShowServiceCoroutineStub(channel)).write(rec) },
        "stop-validation" to { ReplayDriverWorkflowsMessageStopValidation(StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
