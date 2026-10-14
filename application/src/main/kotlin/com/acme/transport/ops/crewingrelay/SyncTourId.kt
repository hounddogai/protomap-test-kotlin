package com.acme.transport.ops.crewingrelay

import com.acme.fanportal.v1.BrowseFanSessionsRequestKt
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.browseFanSessionsRequest
import com.acme.routing.v1.ApproveRouteRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.approveRouteRequest
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.getGeoFenceRatesRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.getTourRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.tours.v1.TourService/GetTour and hands what it keeps to the sink of its kind. */
suspend fun syncTourId(channel: ManagedChannel) {
    val upstream = TourServiceGrpcKt.TourServiceCoroutineStub(channel)
    val got = upstream.getTour(getTourRequest { this.tourId = "current-sync-tour-id" })
    val rec = SyncTourIdRecord(
        tourId = normalize(got.tour.tourId),
        status = normalize(got.result.status),
        message = got.result.message,
        title = normalize(got.tour.title),
    )
    dispatchSyncTourId("geo-fence-calculation", rec, channel)
}

/** Where syncTourId hands each record. */
interface SyncTourIdWriter {
    suspend fun deliver(rec: SyncTourIdRecord)
}

/** What syncTourId keeps of each GetTour response. */
data class SyncTourIdRecord(val tourId: String, val status: String, val message: String, val title: String)

/** Passes records to acme.telematics.v1.GeoFenceCalculationService. */
class SyncTourIdGeoFenceCalculation(private val stub: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub) : SyncTourIdWriter {
    override suspend fun deliver(rec: SyncTourIdRecord) {
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/GetGeoFenceRates") { stub.getGeoFenceRates(getGeoFenceRatesRequest { this.geoFenceId = rec.status }) }
    }
}

/** Hands records to acme.routing.v1.RouteApprovalService. */
class SyncTourIdRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : SyncTourIdWriter {
    override suspend fun deliver(rec: SyncTourIdRecord) {
        val request = approveRouteRequest {
            this.details = ApproveRouteRequestKt.details { this.comment = rec.status }
        }
        stub.approveRoute(request)
    }
}

/** Writes records to acme.fanportal.v1.FanSessionQueryService. */
class SyncTourIdFanSessionQuery(private val stub: FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub) : SyncTourIdWriter {
    override suspend fun deliver(rec: SyncTourIdRecord) {
        val request = browseFanSessionsRequest {
            this.filter = BrowseFanSessionsRequestKt.filter { this.region = rec.tourId }
        }
        stub.browseFanSessions(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncTourIdLogOnly : SyncTourIdWriter {
    override suspend fun deliver(rec: SyncTourIdRecord) {
        LoggerFactory.getLogger("SyncTourIdRecord").atInfo().addKeyValue("tourId", rec.tourId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchSyncTourId(kind: String, rec: SyncTourIdRecord, channel: ManagedChannel) {
    var write: suspend (SyncTourIdRecord) -> Unit = SyncTourIdLogOnly()::deliver
    when (kind) {
        "geo-fence-calculation" -> write = SyncTourIdGeoFenceCalculation(GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(channel))::deliver
        "route-approval" -> write = SyncTourIdRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel))::deliver
        "fan-session-query" -> write = SyncTourIdFanSessionQuery(FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
