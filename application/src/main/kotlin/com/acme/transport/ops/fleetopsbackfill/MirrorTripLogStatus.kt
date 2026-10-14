package com.acme.transport.ops.fleetopsbackfill

import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.fetchFuelTransactionReportRequest
import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.summarizeStockItemsRequest
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.suspendTripLogRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogLifecycleService/SuspendTripLog and hands what it keeps to the sink of its kind. */
suspend fun mirrorTripLogStatus(channel: ManagedChannel) {
    val origin = TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(channel)
    val found = origin.suspendTripLog(suspendTripLogRequest { this.tripLogId = "primary-mirror-trip-log-status" })
    val record = MirrorTripLogStatusRecord(
        status = normalize(found.result.status),
        message = found.outcome.message,
    )
    val sinks = listOf<MirrorTripLogStatusSink>(
        MirrorTripLogStatusStockItemQuery(StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub(channel)),
        MirrorTripLogStatusFuelTransactionReporting(FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub(channel)),
    )
    sinks[1].deliver(record)
}

/** Where mirrorTripLogStatus hands each record. */
interface MirrorTripLogStatusSink {
    suspend fun deliver(rec: MirrorTripLogStatusRecord)
}

/** What mirrorTripLogStatus keeps of each SuspendTripLog response. */
data class MirrorTripLogStatusRecord(val status: String, val message: String)

/** Hands records to acme.inventory.v1.StockItemQueryService. */
class MirrorTripLogStatusStockItemQuery(private val stub: StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub) : MirrorTripLogStatusSink {
    override suspend fun deliver(rec: MirrorTripLogStatusRecord) {
        RpcBoundary.run("acme.inventory.v1.StockItemQueryService/SummarizeStockItems") { stub.summarizeStockItems(summarizeStockItemsRequest { this.reason = rec.status; this.stockItemId = rec.message }) }
    }
}

/** Writes records to acme.fuel.v1.FuelTransactionReportingService. */
class MirrorTripLogStatusFuelTransactionReporting(private val stub: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub) : MirrorTripLogStatusSink {
    override suspend fun deliver(rec: MirrorTripLogStatusRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelTransactionReportingService/FetchFuelTransactionReport") { stub.fetchFuelTransactionReport(fetchFuelTransactionReportRequest { this.fuelTransactionId = rec.status }) }
    }
}
