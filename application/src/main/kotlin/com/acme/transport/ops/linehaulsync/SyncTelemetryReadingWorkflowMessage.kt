package com.acme.transport.ops.linehaulsync

import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.searchIndex
import com.acme.search.v1.updateSearchIndexRequest
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.pauseTelemetryReadingWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingWorkflowService/PauseTelemetryReadingWorkflow and hands what it keeps to acme.search.v1.SearchIndexService. */
suspend fun syncTelemetryReadingWorkflowMessage(channel: ManagedChannel) {
    val reader = TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.pauseTelemetryReadingWorkflow(pauseTelemetryReadingWorkflowRequest { this.telemetryReadingId = "live-sync-telemetry-reading-workflow-message" }) }
    val record = SyncTelemetryReadingWorkflowMessageRecord(
        message = reply.outcome.message,
        resultMessage = reply.result.message,
        telemetryReadingId = reply.telemetryReading.telemetryReadingId,
        correlationId = reply.telemetryReading.correlationId,
    )
    SyncTelemetryReadingWorkflowMessageSearchIndex(SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub(channel)).deliver(record)
}

/** What syncTelemetryReadingWorkflowMessage keeps of each PauseTelemetryReadingWorkflow response. */
data class SyncTelemetryReadingWorkflowMessageRecord(val message: String, val resultMessage: String, val telemetryReadingId: String, val correlationId: String)

/** Sends records to acme.search.v1.SearchIndexService. */
class SyncTelemetryReadingWorkflowMessageSearchIndex(private val stub: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub) {
    suspend fun deliver(rec: SyncTelemetryReadingWorkflowMessageRecord) {
        val request = updateSearchIndexRequest {
            this.searchIndex = searchIndex { this.searchIndexId = rec.resultMessage; this.name = rec.telemetryReadingId; this.code = rec.message }
        }
        RpcBoundary.run("acme.search.v1.SearchIndexService/UpdateSearchIndex") { stub.updateSearchIndex(request) }
    }
}
