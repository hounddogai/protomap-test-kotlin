package com.acme.transport.ops.linehaulimporter

import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.activateDriverLicenseRequest
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.peekVehicleRequest
import com.acme.fuel.v1.FindFuelCardsRequestKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.findFuelCardsRequest
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.media.v1.browseMediaAssetsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.media.v1.MediaAssetQueryService/BrowseMediaAssets and hands what it keeps to the sink of its kind. */
suspend fun relayMediaAssetsStatus(channel: ManagedChannel) {
    val source = MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.browseMediaAssets(browseMediaAssetsRequest { this.orderBy = "current-relay-media-assets-status" }) }
    val entry = RelayMediaAssetsStatusRecord(
        status = normalize(reply.result.status),
        message = normalize(reply.result.message),
    )
    deliverRelayMediaAssetsStatus("fuel-card-query", entry, channel)
}

/** Where relayMediaAssetsStatus hands each record. */
interface RelayMediaAssetsStatusSink {
    suspend fun relay(rec: RelayMediaAssetsStatusRecord)
}

/** What relayMediaAssetsStatus keeps of each BrowseMediaAssets response. */
data class RelayMediaAssetsStatusRecord(val status: String, val message: String)

/** Passes records to acme.fuel.v1.FuelCardQueryService. */
class RelayMediaAssetsStatusFuelCardQuery(private val stub: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub) : RelayMediaAssetsStatusSink {
    override suspend fun relay(rec: RelayMediaAssetsStatusRecord) {
        val request = findFuelCardsRequest {
            this.filter = FindFuelCardsRequestKt.filter { this.query = rec.status }
            this.orderBy = rec.message
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/FindFuelCards") { stub.findFuelCards(request) }
    }
}

/** Writes records to acme.fleet.v1.VehicleQueryService. */
class RelayMediaAssetsStatusVehicleQuery(private val stub: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub) : RelayMediaAssetsStatusSink {
    override suspend fun relay(rec: RelayMediaAssetsStatusRecord) {
        stub.peekVehicle(peekVehicleRequest { this.vehicleId = rec.status })
    }
}

/** Sends records to acme.drivers.v1.DriverLicenseLifecycleService. */
class RelayMediaAssetsStatusDriverLicenseLifecycle(private val stub: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub) : RelayMediaAssetsStatusSink {
    override suspend fun relay(rec: RelayMediaAssetsStatusRecord) {
        val request = activateDriverLicenseRequest {
            this.driverLicenseId = rec.message
            this.reason = rec.status
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ActivateDriverLicense") { stub.activateDriverLicense(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayMediaAssetsStatusLogOnly : RelayMediaAssetsStatusSink {
    override suspend fun relay(rec: RelayMediaAssetsStatusRecord) {
        LoggerFactory.getLogger("RelayMediaAssetsStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayMediaAssetsStatus(kind: String, rec: RelayMediaAssetsStatusRecord, channel: ManagedChannel) {
    var sink: RelayMediaAssetsStatusSink = RelayMediaAssetsStatusLogOnly()
    when (kind) {
        "fuel-card-query" -> sink = RelayMediaAssetsStatusFuelCardQuery(FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub(channel))
        "vehicle-query" -> sink = RelayMediaAssetsStatusVehicleQuery(VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub(channel))
        "driver-license-lifecycle" -> sink = RelayMediaAssetsStatusDriverLicenseLifecycle(DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
