package com.acme.transport.ops.terminalrelay

import com.acme.common.v1.pageRequest
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.recalculateSeatMapsRequest
import com.acme.telehealth.v1.CalculateVideoVisitRequestKt
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.calculateVideoVisitRequest
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.findGeoFencesRequest
import io.grpc.ManagedChannel

/** Reads acme.seating.v1.SeatMapCalculationService/RecalculateSeatMaps and hands what it keeps to the sink of its kind. */
suspend fun pushSeatMapsId(channel: ManagedChannel) {
    val origin = SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub(channel)
    val got = origin.recalculateSeatMaps(recalculateSeatMapsRequest { this.seatMapId = "primary-push-seat-maps-id" })
    val entry = PushSeatMapsIdRecord(
        seatMapId = got.seatMap.seatMapId,
        name = got.seatMap.name,
        message = got.result.message,
        currencyCode = got.breakdown.currencyCode,
    )
    val sinks = mapOf<String, PushSeatMapsIdDestination>(
        "video-visit-calculation" to PushSeatMapsIdVideoVisitCalculation(VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(channel)),
        "geo-fence-query" to PushSeatMapsIdGeoFenceQuery(GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "video-visit-calculation") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where pushSeatMapsId hands each record. */
interface PushSeatMapsIdDestination {
    suspend fun deliver(rec: PushSeatMapsIdRecord)
}

/** What pushSeatMapsId keeps of each RecalculateSeatMaps response. */
data class PushSeatMapsIdRecord(val seatMapId: String, val name: String, val message: String, val currencyCode: String)

/** Writes records to acme.telehealth.v1.VideoVisitCalculationService. */
class PushSeatMapsIdVideoVisitCalculation(private val stub: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub) : PushSeatMapsIdDestination {
    override suspend fun deliver(rec: PushSeatMapsIdRecord) {
        val request = calculateVideoVisitRequest {
            this.reason = rec.currencyCode
            this.details = CalculateVideoVisitRequestKt.details { this.currencyCode = rec.seatMapId }
        }
        stub.calculateVideoVisit(request)
    }
}

/** Writes records to acme.telematics.v1.GeoFenceQueryService. */
class PushSeatMapsIdGeoFenceQuery(private val stub: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub) : PushSeatMapsIdDestination {
    override suspend fun deliver(rec: PushSeatMapsIdRecord) {
        val request = findGeoFencesRequest {
            this.orderBy = rec.message
            this.page = pageRequest { this.pageToken = rec.seatMapId }
        }
        stub.findGeoFences(request)
    }
}
