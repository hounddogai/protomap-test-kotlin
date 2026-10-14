package com.acme.transport.ops.lastmilesync

import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.pauseDriverWorkflowRequest
import com.acme.edgegateway.v1.ListRouteBindingsRequestKt
import com.acme.edgegateway.v1.RouteBindingServiceGrpcKt
import com.acme.edgegateway.v1.listRouteBindingsRequest
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.createVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.deletePerformerRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.SummarizeSponsorsRequestKt
import com.acme.sponsorships.v1.summarizeSponsorsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.performers.v1.PerformerService/DeletePerformer and hands what it keeps to the sink of its kind. */
suspend fun syncPerformerId(channel: ManagedChannel) {
    val source = PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel)
    val got = source.deletePerformer(deletePerformerRequest { this.etag = "live-sync-performer-id" })
    val entry = SyncPerformerIdRecord(
        correlationId = got.result.correlationId,
        message = got.result.message,
    )
    actOnSyncPerformerId("route-binding", entry, channel)
}

/** Where syncPerformerId hands each record. */
interface SyncPerformerIdDestination {
    suspend fun relay(rec: SyncPerformerIdRecord)
}

/** What syncPerformerId keeps of each DeletePerformer response. */
data class SyncPerformerIdRecord(val correlationId: String, val message: String)

/** Passes records to acme.fleet.v1.VehicleService. */
class SyncPerformerIdVehicle(private val stub: VehicleServiceGrpcKt.VehicleServiceCoroutineStub) : SyncPerformerIdDestination {
    override suspend fun relay(rec: SyncPerformerIdRecord) {
        stub.createVehicle(createVehicleRequest { this.vehicle = vehicle { this.name = rec.correlationId } })
    }
}

/** Hands records to acme.drivers.v1.DriverWorkflowService. */
class SyncPerformerIdDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : SyncPerformerIdDestination {
    override suspend fun relay(rec: SyncPerformerIdRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/PauseDriverWorkflow") { stub.pauseDriverWorkflow(pauseDriverWorkflowRequest { this.driver = driver { this.driverId = rec.correlationId } }) }
    }
}

/** Hands records to acme.edgegateway.v1.RouteBindingService. */
class SyncPerformerIdRouteBinding(private val stub: RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub) : SyncPerformerIdDestination {
    override suspend fun relay(rec: SyncPerformerIdRecord) {
        val request = listRouteBindingsRequest {
            this.filter = ListRouteBindingsRequestKt.filter { this.query = rec.message }
        }
        stub.listRouteBindings(request)
    }
}

/** Passes records to acme.sponsorships.v1.SponsorQueryService. */
class SyncPerformerIdSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : SyncPerformerIdDestination {
    override suspend fun relay(rec: SyncPerformerIdRecord) {
        val request = summarizeSponsorsRequest {
            this.details = SummarizeSponsorsRequestKt.details { this.cron = rec.correlationId }
        }
        stub.summarizeSponsors(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSyncPerformerId(kind: String, rec: SyncPerformerIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "vehicle" to { SyncPerformerIdVehicle(VehicleServiceGrpcKt.VehicleServiceCoroutineStub(channel)).relay(rec) },
        "driver-workflow" to { SyncPerformerIdDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel)).relay(rec) },
        "route-binding" to { SyncPerformerIdRouteBinding(RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub(channel)).relay(rec) },
        "sponsor-query" to { SyncPerformerIdSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
