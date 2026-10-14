package com.acme.transport.ops.customsauditor

import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.getFuelTransactionImportStatusRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.calculatePayRunRequest
import com.acme.payroll.v1.payRun
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.getDockRatesRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.DockCalculationService/GetDockRates and hands what it keeps to the sink of its kind. */
suspend fun mirrorDockRatesId(channel: ManagedChannel) {
    val upstream = DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)
    val found = upstream.getDockRates(getDockRatesRequest { this.dockId = "primary-mirror-dock-rates-id" })
    val row = MirrorDockRatesIdRecord(
        dockId = found.dock.dockId,
        correlationId = found.result.correlationId,
        code = found.dock.code,
        name = normalize(found.dock.name),
    )
    dispatchMirrorDockRatesId("fuel-transaction-import", row, channel)
}

/** Where mirrorDockRatesId hands each record. */
interface MirrorDockRatesIdWriter {
    suspend fun deliver(rec: MirrorDockRatesIdRecord)
}

/** What mirrorDockRatesId keeps of each GetDockRates response. */
data class MirrorDockRatesIdRecord(val dockId: String, val correlationId: String, val code: String, val name: String)

/** Writes records to acme.fuel.v1.FuelTransactionImportService. */
class MirrorDockRatesIdFuelTransactionImport(private val stub: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub) : MirrorDockRatesIdWriter {
    override suspend fun deliver(rec: MirrorDockRatesIdRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelTransactionImportService/GetFuelTransactionImportStatus") { stub.getFuelTransactionImportStatus(getFuelTransactionImportStatusRequest { this.fuelTransactionId = rec.code }) }
    }
}

/** Passes records to acme.payroll.v1.PayRunCalculationService. */
class MirrorDockRatesIdPayRunCalculation(private val stub: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub) : MirrorDockRatesIdWriter {
    override suspend fun deliver(rec: MirrorDockRatesIdRecord) {
        val request = calculatePayRunRequest {
            this.reason = rec.dockId
            this.payRun = payRun { this.notes = rec.correlationId }
        }
        stub.calculatePayRun(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorDockRatesIdLogOnly : MirrorDockRatesIdWriter {
    override suspend fun deliver(rec: MirrorDockRatesIdRecord) {
        LoggerFactory.getLogger("MirrorDockRatesIdRecord").atInfo().addKeyValue("dockId", rec.dockId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchMirrorDockRatesId(kind: String, rec: MirrorDockRatesIdRecord, channel: ManagedChannel) {
    var write: suspend (MirrorDockRatesIdRecord) -> Unit = MirrorDockRatesIdLogOnly()::deliver
    when (kind) {
        "fuel-transaction-import" -> write = MirrorDockRatesIdFuelTransactionImport(FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub(channel))::deliver
        "pay-run-calculation" -> write = MirrorDockRatesIdPayRunCalculation(PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
