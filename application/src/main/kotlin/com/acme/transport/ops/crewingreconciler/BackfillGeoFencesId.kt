package com.acme.transport.ops.crewingreconciler

import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.deleteDatasetRequest
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.retryProofOfDeliveryWorkflowRequest
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.cancelRoutePlanWorkflowRequest
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.calculateSponsorRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.pullGeoFencesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceSyncService/PullGeoFences and hands what it keeps to the sink of its kind. */
suspend fun backfillGeoFencesId(channel: ManagedChannel) {
    val reader = GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.pullGeoFences(pullGeoFencesRequest { this.reason = "live-backfill-geo-fences-id" }) }
    val rec = BackfillGeoFencesIdRecord(
        referenceId = current.outcome.referenceId,
        name = current.geoFence.name,
        geoFenceId = normalize(current.geoFence.geoFenceId),
    )
    actOnBackfillGeoFencesId("route-plan-workflow", rec, channel)
}

/** Where backfillGeoFencesId hands each record. */
interface BackfillGeoFencesIdSink {
    suspend fun relay(rec: BackfillGeoFencesIdRecord)
}

/** What backfillGeoFencesId keeps of each PullGeoFences response. */
data class BackfillGeoFencesIdRecord(val referenceId: String, val name: String, val geoFenceId: String)

/** Sends records to acme.routing.v1.RoutePlanWorkflowService. */
class BackfillGeoFencesIdRoutePlanWorkflow(private val stub: RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub) : BackfillGeoFencesIdSink {
    override suspend fun relay(rec: BackfillGeoFencesIdRecord) {
        stub.cancelRoutePlanWorkflow(cancelRoutePlanWorkflowRequest { this.reason = rec.name })
    }
}

/** Writes records to acme.deliveries.v1.ProofOfDeliveryWorkflowService. */
class BackfillGeoFencesIdProofOfDeliveryWorkflow(private val stub: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub) : BackfillGeoFencesIdSink {
    override suspend fun relay(rec: BackfillGeoFencesIdRecord) {
        val request = retryProofOfDeliveryWorkflowRequest {
            this.reason = rec.geoFenceId
            this.proofOfDelivery = proofOfDelivery { this.number = rec.name; this.proofOfDeliveryId = rec.referenceId }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/RetryProofOfDeliveryWorkflow") { stub.retryProofOfDeliveryWorkflow(request) }
    }
}

/** Sends records to acme.datasets.v1.DatasetService. */
class BackfillGeoFencesIdDataset(private val stub: DatasetServiceGrpcKt.DatasetServiceCoroutineStub) : BackfillGeoFencesIdSink {
    override suspend fun relay(rec: BackfillGeoFencesIdRecord) {
        RpcBoundary.run("acme.datasets.v1.DatasetService/DeleteDataset") { stub.deleteDataset(deleteDatasetRequest { this.etag = rec.name }) }
    }
}

/** Sends records to acme.sponsorships.v1.SponsorCalculationService. */
class BackfillGeoFencesIdSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : BackfillGeoFencesIdSink {
    override suspend fun relay(rec: BackfillGeoFencesIdRecord) {
        stub.calculateSponsor(calculateSponsorRequest { this.sponsorId = rec.name })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnBackfillGeoFencesId(kind: String, rec: BackfillGeoFencesIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "route-plan-workflow" to { BackfillGeoFencesIdRoutePlanWorkflow(RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel)).relay(rec) },
        "proof-of-delivery-workflow" to { BackfillGeoFencesIdProofOfDeliveryWorkflow(ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel)).relay(rec) },
        "dataset" to { BackfillGeoFencesIdDataset(DatasetServiceGrpcKt.DatasetServiceCoroutineStub(channel)).relay(rec) },
        "sponsor-calculation" to { BackfillGeoFencesIdSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
