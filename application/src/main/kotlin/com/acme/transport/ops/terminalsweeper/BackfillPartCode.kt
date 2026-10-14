package com.acme.transport.ops.terminalsweeper

import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.compareCorporateEventOptionsRequest
import com.acme.corporateevents.v1.corporateEvent
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.RenewFuelCardRequestKt
import com.acme.fuel.v1.renewFuelCardRequest
import com.acme.inventory.v1.FindStockItemsRequestKt
import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.findStockItemsRequest
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.estimatePartRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.PartCalculationService/EstimatePart and hands what it keeps to the sink of its kind. */
suspend fun backfillPartCode(channel: ManagedChannel) {
    val upstream = PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub(channel)
    val reply = upstream.estimatePart(estimatePartRequest { this.reason = "primary-backfill-part-code" })
    val rec = BackfillPartCodeRecord(
        currencyCode = reply.breakdown.currencyCode,
        code = reply.part.code,
        name = reply.part.name,
        correlationId = reply.result.correlationId,
    )
    dispatchBackfillPartCode("corporate-event-calculation", rec, channel)
}

/** Where backfillPartCode hands each record. */
interface BackfillPartCodeSink {
    suspend fun forward(rec: BackfillPartCodeRecord)
}

/** What backfillPartCode keeps of each EstimatePart response. */
data class BackfillPartCodeRecord(val currencyCode: String, val code: String, val name: String, val correlationId: String)

/** Passes records to acme.inventory.v1.StockItemQueryService. */
class BackfillPartCodeStockItemQuery(private val stub: StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub) : BackfillPartCodeSink {
    override suspend fun forward(rec: BackfillPartCodeRecord) {
        val request = findStockItemsRequest {
            this.orderBy = rec.name
            this.filter = FindStockItemsRequestKt.filter { this.region = rec.currencyCode }
        }
        stub.findStockItems(request)
    }
}

/** Passes records to acme.fuel.v1.FuelCardLifecycleService. */
class BackfillPartCodeFuelCardLifecycle(private val stub: FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub) : BackfillPartCodeSink {
    override suspend fun forward(rec: BackfillPartCodeRecord) {
        val request = renewFuelCardRequest {
            this.reason = rec.code
            this.details = RenewFuelCardRequestKt.details { this.reasonCode = rec.name }
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/RenewFuelCard") { stub.renewFuelCard(request) }
    }
}

/** Sends records to acme.corporateevents.v1.CorporateEventCalculationService. */
class BackfillPartCodeCorporateEventCalculation(private val stub: CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub) : BackfillPartCodeSink {
    override suspend fun forward(rec: BackfillPartCodeRecord) {
        val request = compareCorporateEventOptionsRequest {
            this.corporateEvent = corporateEvent { this.title = rec.correlationId; this.corporateEventId = rec.currencyCode }
            this.corporateEventId = rec.name
        }
        stub.compareCorporateEventOptions(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillPartCodeLogOnly : BackfillPartCodeSink {
    override suspend fun forward(rec: BackfillPartCodeRecord) {
        LoggerFactory.getLogger("BackfillPartCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchBackfillPartCode(kind: String, rec: BackfillPartCodeRecord, channel: ManagedChannel) {
    var write: suspend (BackfillPartCodeRecord) -> Unit = BackfillPartCodeLogOnly()::forward
    when (kind) {
        "stock-item-query" -> write = BackfillPartCodeStockItemQuery(StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub(channel))::forward
        "fuel-card-lifecycle" -> write = BackfillPartCodeFuelCardLifecycle(FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(channel))::forward
        "corporate-event-calculation" -> write = BackfillPartCodeCorporateEventCalculation(CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
