package com.acme.transport.ops.crewingrelay

import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.peekConcessionStandRequest
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.shareFuelTransactionReportRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.recalculateDocksRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockCalculationService/RecalculateDocks and hands what it keeps to the sink of its kind. */
suspend fun syncDocksId(channel: ManagedChannel) {
    val upstream = DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)
    val reply = upstream.recalculateDocks(recalculateDocksRequest { this.reason = "primary-sync-docks-id" })
    val row = SyncDocksIdRecord(
        dockId = normalize(reply.dock.dockId),
        name = reply.dock.name,
    )
    val sinks = listOf<SyncDocksIdSink>(
        SyncDocksIdFuelTransactionReporting(FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub(channel)),
        SyncDocksIdConcessionStandQuery(ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)),
    )
    sinks[1].forward(row)
}

/** Where syncDocksId hands each record. */
interface SyncDocksIdSink {
    suspend fun forward(rec: SyncDocksIdRecord)
}

/** What syncDocksId keeps of each RecalculateDocks response. */
data class SyncDocksIdRecord(val dockId: String, val name: String)

/** Writes records to acme.fuel.v1.FuelTransactionReportingService. */
class SyncDocksIdFuelTransactionReporting(private val stub: FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub) : SyncDocksIdSink {
    override suspend fun forward(rec: SyncDocksIdRecord) {
        val request = shareFuelTransactionReportRequest {
            this.fuelTransactionId = rec.name
            this.reason = rec.dockId
        }
        stub.shareFuelTransactionReport(request)
    }
}

/** Writes records to acme.concessions.v1.ConcessionStandQueryService. */
class SyncDocksIdConcessionStandQuery(private val stub: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub) : SyncDocksIdSink {
    override suspend fun forward(rec: SyncDocksIdRecord) {
        RpcBoundary.run("acme.concessions.v1.ConcessionStandQueryService/PeekConcessionStand") { stub.peekConcessionStand(peekConcessionStandRequest { this.concessionStandId = rec.name }) }
    }
}
