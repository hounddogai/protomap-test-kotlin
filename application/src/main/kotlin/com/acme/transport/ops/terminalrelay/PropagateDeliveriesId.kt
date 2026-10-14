package com.acme.transport.ops.terminalrelay

import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.findDeliveriesRequest
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.peekSeatMapRequest
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryQueryService/FindDeliveries and hands what it keeps to the sink of its kind. */
suspend fun propagateDeliveriesId(channel: ManagedChannel) {
    val reader = DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel)
    val reply = reader.findDeliveries(findDeliveriesRequest { this.orderBy = "main-propagate-deliveries-id" })
    val entry = PropagateDeliveriesIdRecord(
        correlationId = reply.result.correlationId,
        status = reply.result.status,
        nextPageToken = reply.pageInfo.nextPageToken,
        message = reply.result.message,
    )
    val sinks = mapOf<String, PropagateDeliveriesIdSink>(
        "trip-log-calculation" to PropagateDeliveriesIdTripLogCalculation(TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(channel)),
        "seat-map-query" to PropagateDeliveriesIdSeatMapQuery(SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel)),
    )
    routePropagateDeliveriesId(sinks, "trip-log-calculation", entry)
}

/** Where propagateDeliveriesId hands each record. */
interface PropagateDeliveriesIdSink {
    suspend fun forward(rec: PropagateDeliveriesIdRecord)
}

/** What propagateDeliveriesId keeps of each FindDeliveries response. */
data class PropagateDeliveriesIdRecord(val correlationId: String, val status: String, val nextPageToken: String, val message: String)

/** Hands records to acme.telematics.v1.TripLogCalculationService. */
class PropagateDeliveriesIdTripLogCalculation(private val stub: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub) : PropagateDeliveriesIdSink {
    override suspend fun forward(rec: PropagateDeliveriesIdRecord) {
        val request = simulateTripLogRequest {
            this.reason = rec.correlationId
            this.tripLog = tripLog { this.tripLogId = rec.nextPageToken }
        }
        stub.simulateTripLog(request)
    }
}

/** Hands records to acme.seating.v1.SeatMapQueryService. */
class PropagateDeliveriesIdSeatMapQuery(private val stub: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub) : PropagateDeliveriesIdSink {
    override suspend fun forward(rec: PropagateDeliveriesIdRecord) {
        RpcBoundary.run("acme.seating.v1.SeatMapQueryService/PeekSeatMap") { stub.peekSeatMap(peekSeatMapRequest { this.seatMapId = rec.status }) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routePropagateDeliveriesId(sinks: Map<String, PropagateDeliveriesIdSink>, kind: String, rec: PropagateDeliveriesIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
