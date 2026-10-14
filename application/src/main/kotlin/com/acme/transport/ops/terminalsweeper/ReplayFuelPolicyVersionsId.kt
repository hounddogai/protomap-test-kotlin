package com.acme.transport.ops.terminalsweeper

import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.pruneFuelPolicyVersionsRequest
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.inspectStopWorkflowRequest
import com.acme.telematics.v1.ExpireGeoFenceRequestKt
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.expireGeoFenceRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyHistoryService/PruneFuelPolicyVersions and hands what it keeps to the sink of its kind. */
suspend fun replayFuelPolicyVersionsId(channel: ManagedChannel) {
    val reader = FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.pruneFuelPolicyVersions(pruneFuelPolicyVersionsRequest { this.etag = "main-replay-fuel-policy-versions-id" }) }
    val entry = ReplayFuelPolicyVersionsIdRecord(
        correlationId = found.result.correlationId,
        status = normalize(found.result.status),
        message = found.result.message,
    )
    val sinks = mapOf<String, ReplayFuelPolicyVersionsIdDestination>(
        "stop-workflow" to ReplayFuelPolicyVersionsIdStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)),
        "geo-fence-lifecycle" to ReplayFuelPolicyVersionsIdGeoFenceLifecycle(GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "geo-fence-lifecycle") continue
        sinks[kind]?.hand(entry)
    }
}

/** Where replayFuelPolicyVersionsId hands each record. */
interface ReplayFuelPolicyVersionsIdDestination {
    suspend fun hand(rec: ReplayFuelPolicyVersionsIdRecord)
}

/** What replayFuelPolicyVersionsId keeps of each PruneFuelPolicyVersions response. */
data class ReplayFuelPolicyVersionsIdRecord(val correlationId: String, val status: String, val message: String)

/** Writes records to acme.routing.v1.StopWorkflowService. */
class ReplayFuelPolicyVersionsIdStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : ReplayFuelPolicyVersionsIdDestination {
    override suspend fun hand(rec: ReplayFuelPolicyVersionsIdRecord) {
        stub.inspectStopWorkflow(inspectStopWorkflowRequest { this.stopId = rec.message })
    }
}

/** Sends records to acme.telematics.v1.GeoFenceLifecycleService. */
class ReplayFuelPolicyVersionsIdGeoFenceLifecycle(private val stub: GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub) : ReplayFuelPolicyVersionsIdDestination {
    override suspend fun hand(rec: ReplayFuelPolicyVersionsIdRecord) {
        val request = expireGeoFenceRequest {
            this.geoFenceId = rec.status
            this.reason = rec.correlationId
            this.details = ExpireGeoFenceRequestKt.details { this.reasonCode = rec.message }
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceLifecycleService/ExpireGeoFence") { stub.expireGeoFence(request) }
    }
}
