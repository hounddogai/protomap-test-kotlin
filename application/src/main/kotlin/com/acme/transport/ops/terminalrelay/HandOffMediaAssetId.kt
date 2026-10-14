package com.acme.transport.ops.terminalrelay

import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.delegateDispatchOrderApprovalRequest
import com.acme.dispatch.v1.dispatchOrder
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDutyStatusOptionsRequest
import com.acme.fleet.v1.SubmitVehicleGroupRequestKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.submitVehicleGroupRequest
import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.calculateMediaAssetRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.getGeoFenceSyncStateRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.media.v1.MediaAssetCalculationService/CalculateMediaAsset and hands what it keeps to the sink of its kind. */
suspend fun handOffMediaAssetId(channel: ManagedChannel) {
    val upstream = MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub(channel)
    val current = upstream.calculateMediaAsset(calculateMediaAssetRequest { this.reason = "primary-hand-off-media-asset-id" })
    val rec = HandOffMediaAssetIdRecord(
        mediaAssetId = current.mediaAsset.mediaAssetId,
        status = current.result.status,
        correlationId = current.result.correlationId,
        name = current.mediaAsset.name,
    )
    val sinks = mapOf<String, HandOffMediaAssetIdSink>(
        "dispatch-order-approval" to HandOffMediaAssetIdDispatchOrderApproval(DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(channel)),
        "vehicle-group-approval" to HandOffMediaAssetIdVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)),
        "geo-fence-sync" to HandOffMediaAssetIdGeoFenceSync(GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel)),
        "duty-status-calculation" to HandOffMediaAssetIdDutyStatusCalculation(DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "geo-fence-sync") continue
        sinks[kind]?.write(rec)
    }
}

/** Where handOffMediaAssetId hands each record. */
interface HandOffMediaAssetIdSink {
    suspend fun write(rec: HandOffMediaAssetIdRecord)
}

/** What handOffMediaAssetId keeps of each CalculateMediaAsset response. */
data class HandOffMediaAssetIdRecord(val mediaAssetId: String, val status: String, val correlationId: String, val name: String)

/** Writes records to acme.dispatch.v1.DispatchOrderApprovalService. */
class HandOffMediaAssetIdDispatchOrderApproval(private val stub: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub) : HandOffMediaAssetIdSink {
    override suspend fun write(rec: HandOffMediaAssetIdRecord) {
        val request = delegateDispatchOrderApprovalRequest {
            this.dispatchOrder = dispatchOrder { this.dispatchOrderId = rec.correlationId }
        }
        stub.delegateDispatchOrderApproval(request)
    }
}

/** Passes records to acme.fleet.v1.VehicleGroupApprovalService. */
class HandOffMediaAssetIdVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : HandOffMediaAssetIdSink {
    override suspend fun write(rec: HandOffMediaAssetIdRecord) {
        val request = submitVehicleGroupRequest {
            this.details = SubmitVehicleGroupRequestKt.details { this.approverEmail = rec.name }
        }
        stub.submitVehicleGroup(request)
    }
}

/** Writes records to acme.telematics.v1.GeoFenceSyncService. */
class HandOffMediaAssetIdGeoFenceSync(private val stub: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub) : HandOffMediaAssetIdSink {
    override suspend fun write(rec: HandOffMediaAssetIdRecord) {
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/GetGeoFenceSyncState") { stub.getGeoFenceSyncState(getGeoFenceSyncStateRequest { this.geoFenceId = rec.name }) }
    }
}

/** Sends records to acme.drivers.v1.DutyStatusCalculationService. */
class HandOffMediaAssetIdDutyStatusCalculation(private val stub: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub) : HandOffMediaAssetIdSink {
    override suspend fun write(rec: HandOffMediaAssetIdRecord) {
        stub.compareDutyStatusOptions(compareDutyStatusOptionsRequest { this.dutyStatusId = rec.name })
    }
}
