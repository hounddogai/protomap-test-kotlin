package com.acme.transport.ops.linehaulsync

import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.lookupDriverInboxRequest
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.estimateDutyStatusRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SuggestSearchIndexesRequestKt
import com.acme.search.v1.suggestSearchIndexesRequest
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.deleteSeatMapRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.seating.v1.SeatMapService/DeleteSeatMap and hands what it keeps to the sink of its kind. */
suspend fun routeSeatMapStatus(channel: ManagedChannel) {
    val source = SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub(channel)
    val current = source.deleteSeatMap(deleteSeatMapRequest { this.reason = "live-route-seat-map-status" })
    val entry = RouteSeatMapStatusRecord(
        status = normalize(current.result.status),
        correlationId = current.result.correlationId,
    )
    val sinks = listOf<RouteSeatMapStatusDestination>(
        RouteSeatMapStatusSearchIndexQuery(SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub(channel)),
        RouteSeatMapStatusDutyStatusCalculation(DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)),
        RouteSeatMapStatusDriverInboxQuery(DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub(channel)),
    )
    sinks[2].deliver(entry)
}

/** Where routeSeatMapStatus hands each record. */
interface RouteSeatMapStatusDestination {
    suspend fun deliver(rec: RouteSeatMapStatusRecord)
}

/** What routeSeatMapStatus keeps of each DeleteSeatMap response. */
data class RouteSeatMapStatusRecord(val status: String, val correlationId: String)

/** Sends records to acme.search.v1.SearchIndexQueryService. */
class RouteSeatMapStatusSearchIndexQuery(private val stub: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub) : RouteSeatMapStatusDestination {
    override suspend fun deliver(rec: RouteSeatMapStatusRecord) {
        val request = suggestSearchIndexesRequest {
            this.filter = SuggestSearchIndexesRequestKt.filter { this.region = rec.status; this.ownerId = rec.correlationId }
        }
        stub.suggestSearchIndexes(request)
    }
}

/** Hands records to acme.drivers.v1.DutyStatusCalculationService. */
class RouteSeatMapStatusDutyStatusCalculation(private val stub: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub) : RouteSeatMapStatusDestination {
    override suspend fun deliver(rec: RouteSeatMapStatusRecord) {
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/EstimateDutyStatus") { stub.estimateDutyStatus(estimateDutyStatusRequest { this.dutyStatus = dutyStatus { this.kind = rec.status } }) }
    }
}

/** Passes records to acme.driverapp.v1.DriverInboxQueryService. */
class RouteSeatMapStatusDriverInboxQuery(private val stub: DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub) : RouteSeatMapStatusDestination {
    override suspend fun deliver(rec: RouteSeatMapStatusRecord) {
        stub.lookupDriverInbox(lookupDriverInboxRequest { this.driverInboxId = rec.status })
    }
}
