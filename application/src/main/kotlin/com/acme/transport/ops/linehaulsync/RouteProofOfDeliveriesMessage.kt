package com.acme.transport.ops.linehaulsync

import com.acme.deliveries.v1.ProofOfDeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.resolveProofOfDeliveriesRequest
import com.acme.seating.v1.CountSeatMapsRequestKt
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.countSeatMapsRequest
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.estimateGeoFenceRequest
import com.acme.telematics.v1.geoFence
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.ProofOfDeliveryQueryService/ResolveProofOfDeliveries and hands what it keeps to the sink of its kind. */
suspend fun routeProofOfDeliveriesMessage(channel: ManagedChannel) {
    val reader = ProofOfDeliveryQueryServiceGrpcKt.ProofOfDeliveryQueryServiceCoroutineStub(channel)
    val got = reader.resolveProofOfDeliveries(resolveProofOfDeliveriesRequest { })
    val row = RouteProofOfDeliveriesMessageRecord(
        message = got.result.message,
        status = got.result.status,
        correlationId = got.result.correlationId,
    )
    actOnRouteProofOfDeliveriesMessage("seat-map-query", row, channel)
}

/** Where routeProofOfDeliveriesMessage hands each record. */
interface RouteProofOfDeliveriesMessageSink {
    suspend fun forward(rec: RouteProofOfDeliveriesMessageRecord)
}

/** What routeProofOfDeliveriesMessage keeps of each ResolveProofOfDeliveries response. */
data class RouteProofOfDeliveriesMessageRecord(val message: String, val status: String, val correlationId: String)

/** Sends records to acme.seating.v1.SeatMapQueryService. */
class RouteProofOfDeliveriesMessageSeatMapQuery(private val stub: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub) : RouteProofOfDeliveriesMessageSink {
    override suspend fun forward(rec: RouteProofOfDeliveriesMessageRecord) {
        val request = countSeatMapsRequest {
            this.filter = CountSeatMapsRequestKt.filter { this.ownerId = rec.status; this.region = rec.message }
        }
        stub.countSeatMaps(request)
    }
}

/** Writes records to acme.telematics.v1.GeoFenceCalculationService. */
class RouteProofOfDeliveriesMessageGeoFenceCalculation(private val stub: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub) : RouteProofOfDeliveriesMessageSink {
    override suspend fun forward(rec: RouteProofOfDeliveriesMessageRecord) {
        val request = estimateGeoFenceRequest {
            this.geoFence = geoFence { this.geoFenceId = rec.message; this.description = rec.status }
        }
        stub.estimateGeoFence(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnRouteProofOfDeliveriesMessage(kind: String, rec: RouteProofOfDeliveriesMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "seat-map-query" to { RouteProofOfDeliveriesMessageSeatMapQuery(SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel)).forward(rec) },
        "geo-fence-calculation" to { RouteProofOfDeliveriesMessageGeoFenceCalculation(GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
