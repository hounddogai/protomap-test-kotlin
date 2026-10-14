package com.acme.transport.ops.fleetopsbackfill

import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.listPreferenceReportsRequest
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.recalculateFuelTransactionsRequest
import com.acme.fuel.v1.resumeFuelCardWorkflowRequest
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.inventory.v1.stockItem
import com.acme.inventory.v1.updateStockItemRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.PreferenceReportingService/ListPreferenceReports and hands what it keeps to the sink of its kind. */
suspend fun backfillPreferenceReportsToken(channel: ManagedChannel) {
    val origin = PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.listPreferenceReports(listPreferenceReportsRequest { this.orderBy = "live-backfill-preference-reports-token" }) }
    val row = BackfillPreferenceReportsTokenRecord(
        nextPageToken = found.pageInfo.nextPageToken,
        correlationId = found.result.correlationId,
        message = found.result.message,
        status = found.result.status,
    )
    actOnBackfillPreferenceReportsToken("fuel-card-workflow", row, channel)
}

/** Where backfillPreferenceReportsToken hands each record. */
interface BackfillPreferenceReportsTokenTarget {
    suspend fun relay(rec: BackfillPreferenceReportsTokenRecord)
}

/** What backfillPreferenceReportsToken keeps of each ListPreferenceReports response. */
data class BackfillPreferenceReportsTokenRecord(val nextPageToken: String, val correlationId: String, val message: String, val status: String)

/** Hands records to acme.fuel.v1.FuelCardWorkflowService. */
class BackfillPreferenceReportsTokenFuelCardWorkflow(private val stub: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub) : BackfillPreferenceReportsTokenTarget {
    override suspend fun relay(rec: BackfillPreferenceReportsTokenRecord) {
        val request = resumeFuelCardWorkflowRequest {
            this.fuelCard = fuelCard { this.fuelCardId = rec.correlationId }
        }
        stub.resumeFuelCardWorkflow(request)
    }
}

/** Hands records to acme.fuel.v1.FuelTransactionCalculationService. */
class BackfillPreferenceReportsTokenFuelTransactionCalculation(private val stub: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub) : BackfillPreferenceReportsTokenTarget {
    override suspend fun relay(rec: BackfillPreferenceReportsTokenRecord) {
        val request = recalculateFuelTransactionsRequest {
            this.fuelTransaction = fuelTransaction { this.source = rec.message; this.correlationId = rec.status }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionCalculationService/RecalculateFuelTransactions") { stub.recalculateFuelTransactions(request) }
    }
}

/** Writes records to acme.inventory.v1.StockItemService. */
class BackfillPreferenceReportsTokenStockItem(private val stub: StockItemServiceGrpcKt.StockItemServiceCoroutineStub) : BackfillPreferenceReportsTokenTarget {
    override suspend fun relay(rec: BackfillPreferenceReportsTokenRecord) {
        val request = updateStockItemRequest {
            this.stockItem = stockItem { this.name = rec.correlationId; this.stockItemId = rec.nextPageToken }
        }
        stub.updateStockItem(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnBackfillPreferenceReportsToken(kind: String, rec: BackfillPreferenceReportsTokenRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "fuel-card-workflow" to { BackfillPreferenceReportsTokenFuelCardWorkflow(FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(channel)).relay(rec) },
        "fuel-transaction-calculation" to { BackfillPreferenceReportsTokenFuelTransactionCalculation(FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub(channel)).relay(rec) },
        "stock-item" to { BackfillPreferenceReportsTokenStockItem(StockItemServiceGrpcKt.StockItemServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
