package com.acme.transport.ops.fleetopsbackfill

import com.acme.common.v1.pageRequest
import com.acme.dispatch.v1.BrowseDispatchAssignmentsRequestKt
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.browseDispatchAssignmentsRequest
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.advanceFuelPolicyWorkflowRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.explainMediaAssetCalculationRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.media.v1.MediaAssetCalculationService/ExplainMediaAssetCalculation and hands what it keeps to the sink of its kind. */
suspend fun syncMediaAssetCalculationId(channel: ManagedChannel) {
    val origin = MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub(channel)
    val reply = origin.explainMediaAssetCalculation(explainMediaAssetCalculationRequest { this.mediaAssetId = "default-sync-media-asset-calculation-id" })
    val record = SyncMediaAssetCalculationIdRecord(
        mediaAssetId = reply.mediaAsset.mediaAssetId,
        status = reply.result.status,
        message = normalize(reply.result.message),
    )
    deliverSyncMediaAssetCalculationId("dispatch-assignment-query", record, channel)
}

/** Where syncMediaAssetCalculationId hands each record. */
interface SyncMediaAssetCalculationIdSink {
    suspend fun hand(rec: SyncMediaAssetCalculationIdRecord)
}

/** What syncMediaAssetCalculationId keeps of each ExplainMediaAssetCalculation response. */
data class SyncMediaAssetCalculationIdRecord(val mediaAssetId: String, val status: String, val message: String)

/** Passes records to acme.fuel.v1.FuelPolicyWorkflowService. */
class SyncMediaAssetCalculationIdFuelPolicyWorkflow(private val stub: FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub) : SyncMediaAssetCalculationIdSink {
    override suspend fun hand(rec: SyncMediaAssetCalculationIdRecord) {
        val request = advanceFuelPolicyWorkflowRequest {
            this.fuelPolicy = fuelPolicy { this.description = rec.mediaAssetId }
            this.reason = rec.message
        }
        stub.advanceFuelPolicyWorkflow(request)
    }
}

/** Writes records to acme.dispatch.v1.DispatchAssignmentQueryService. */
class SyncMediaAssetCalculationIdDispatchAssignmentQuery(private val stub: DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub) : SyncMediaAssetCalculationIdSink {
    override suspend fun hand(rec: SyncMediaAssetCalculationIdRecord) {
        val request = browseDispatchAssignmentsRequest {
            this.page = pageRequest { this.pageToken = rec.mediaAssetId }
            this.filter = BrowseDispatchAssignmentsRequestKt.filter { this.query = rec.message; this.ownerId = rec.status }
        }
        stub.browseDispatchAssignments(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncMediaAssetCalculationIdLogOnly : SyncMediaAssetCalculationIdSink {
    override suspend fun hand(rec: SyncMediaAssetCalculationIdRecord) {
        LoggerFactory.getLogger("SyncMediaAssetCalculationIdRecord").atInfo().addKeyValue("mediaAssetId", rec.mediaAssetId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncMediaAssetCalculationId(kind: String, rec: SyncMediaAssetCalculationIdRecord, channel: ManagedChannel) {
    var sink: SyncMediaAssetCalculationIdSink = SyncMediaAssetCalculationIdLogOnly()
    when (kind) {
        "fuel-policy-workflow" -> sink = SyncMediaAssetCalculationIdFuelPolicyWorkflow(FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub(channel))
        "dispatch-assignment-query" -> sink = SyncMediaAssetCalculationIdDispatchAssignmentQuery(DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
