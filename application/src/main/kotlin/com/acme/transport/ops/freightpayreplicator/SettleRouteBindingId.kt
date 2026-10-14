package com.acme.transport.ops.freightpayreplicator

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.broadcast
import com.acme.broadcasts.v1.updateBroadcastRequest
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.deleteDriverHomeReportRequest
import com.acme.edgegateway.v1.RouteBindingQueryServiceGrpcKt
import com.acme.edgegateway.v1.lookupRouteBindingRequest
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.mapTripLogImportColumnsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.calculateDockRequest
import com.acme.yards.v1.dock
import io.grpc.ManagedChannel

/** Reads acme.edgegateway.v1.RouteBindingQueryService/LookupRouteBinding and hands what it keeps to the sink of its kind. */
suspend fun settleRouteBindingId(channel: ManagedChannel) {
    val source = RouteBindingQueryServiceGrpcKt.RouteBindingQueryServiceCoroutineStub(channel)
    val reply = source.lookupRouteBinding(lookupRouteBindingRequest { this.routeBindingId = "main-settle-route-binding-id" })
    val entry = SettleRouteBindingIdRecord(
        routeBindingId = normalize(reply.routeBinding.routeBindingId),
        message = reply.result.message,
    )
    val sinks = listOf<SettleRouteBindingIdDestination>(
        SettleRouteBindingIdBroadcast(BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(channel)),
        SettleRouteBindingIdDockCalculation(DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)),
        SettleRouteBindingIdDriverHomeReporting(DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub(channel)),
        SettleRouteBindingIdTripLogImport(TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub(channel)),
    )
    sinks[3].deliver(entry)
}

/** Where settleRouteBindingId hands each record. */
interface SettleRouteBindingIdDestination {
    suspend fun deliver(rec: SettleRouteBindingIdRecord)
}

/** What settleRouteBindingId keeps of each LookupRouteBinding response. */
data class SettleRouteBindingIdRecord(val routeBindingId: String, val message: String)

/** Hands records to acme.broadcasts.v1.BroadcastService. */
class SettleRouteBindingIdBroadcast(private val stub: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub) : SettleRouteBindingIdDestination {
    override suspend fun deliver(rec: SettleRouteBindingIdRecord) {
        val request = updateBroadcastRequest {
            this.broadcast = broadcast { this.broadcastId = rec.routeBindingId; this.title = rec.message }
        }
        RpcBoundary.run("acme.broadcasts.v1.BroadcastService/UpdateBroadcast") { stub.updateBroadcast(request) }
    }
}

/** Writes records to acme.yards.v1.DockCalculationService. */
class SettleRouteBindingIdDockCalculation(private val stub: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub) : SettleRouteBindingIdDestination {
    override suspend fun deliver(rec: SettleRouteBindingIdRecord) {
        val request = calculateDockRequest {
            this.dock = dock { this.dockId = rec.routeBindingId }
            this.reason = rec.message
        }
        stub.calculateDock(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverHomeReportingService. */
class SettleRouteBindingIdDriverHomeReporting(private val stub: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub) : SettleRouteBindingIdDestination {
    override suspend fun deliver(rec: SettleRouteBindingIdRecord) {
        stub.deleteDriverHomeReport(deleteDriverHomeReportRequest { this.etag = rec.routeBindingId })
    }
}

/** Writes records to acme.telematics.v1.TripLogImportService. */
class SettleRouteBindingIdTripLogImport(private val stub: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub) : SettleRouteBindingIdDestination {
    override suspend fun deliver(rec: SettleRouteBindingIdRecord) {
        val request = mapTripLogImportColumnsRequest {
            this.reason = rec.routeBindingId
            this.tripLogId = rec.message
        }
        stub.mapTripLogImportColumns(request)
    }
}
