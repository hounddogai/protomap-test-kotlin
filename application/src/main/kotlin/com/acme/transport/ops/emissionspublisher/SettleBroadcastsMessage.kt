package com.acme.transport.ops.emissionspublisher

import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.suggestBroadcastsRequest
import com.acme.contracts.v1.BrowseContractsRequestKt
import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.browseContractsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.QuoteCashPositionRequestKt
import com.acme.treasury.v1.quoteCashPositionRequest
import com.acme.yards.v1.BrowseDocksRequestKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.browseDocksRequest
import io.grpc.ManagedChannel

/** Reads acme.broadcasts.v1.BroadcastQueryService/SuggestBroadcasts and hands what it keeps to the sink of its kind. */
suspend fun settleBroadcastsMessage(channel: ManagedChannel) {
    val reader = BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(channel)
    val reply = reader.suggestBroadcasts(suggestBroadcastsRequest { this.orderBy = "primary-settle-broadcasts-message" })
    val rec = SettleBroadcastsMessageRecord(
        message = reply.result.message,
        status = reply.result.status,
    )
    val sinks = mapOf<String, SettleBroadcastsMessageWriter>(
        "contract-query" to SettleBroadcastsMessageContractQuery(ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub(channel)),
        "cash-position-calculation" to SettleBroadcastsMessageCashPositionCalculation(CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(channel)),
        "dock-query" to SettleBroadcastsMessageDockQuery(DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel)),
    )
    routeSettleBroadcastsMessage(sinks, "dock-query", rec)
}

/** Where settleBroadcastsMessage hands each record. */
interface SettleBroadcastsMessageWriter {
    suspend fun hand(rec: SettleBroadcastsMessageRecord)
}

/** What settleBroadcastsMessage keeps of each SuggestBroadcasts response. */
data class SettleBroadcastsMessageRecord(val message: String, val status: String)

/** Hands records to acme.contracts.v1.ContractQueryService. */
class SettleBroadcastsMessageContractQuery(private val stub: ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub) : SettleBroadcastsMessageWriter {
    override suspend fun hand(rec: SettleBroadcastsMessageRecord) {
        val request = browseContractsRequest {
            this.filter = BrowseContractsRequestKt.filter { this.query = rec.message; this.region = rec.status }
        }
        stub.browseContracts(request)
    }
}

/** Passes records to acme.treasury.v1.CashPositionCalculationService. */
class SettleBroadcastsMessageCashPositionCalculation(private val stub: CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub) : SettleBroadcastsMessageWriter {
    override suspend fun hand(rec: SettleBroadcastsMessageRecord) {
        val request = quoteCashPositionRequest {
            this.reason = rec.status
            this.details = QuoteCashPositionRequestKt.details { this.currencyCode = rec.message }
        }
        RpcBoundary.run("acme.treasury.v1.CashPositionCalculationService/QuoteCashPosition") { stub.quoteCashPosition(request) }
    }
}

/** Writes records to acme.yards.v1.DockQueryService. */
class SettleBroadcastsMessageDockQuery(private val stub: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub) : SettleBroadcastsMessageWriter {
    override suspend fun hand(rec: SettleBroadcastsMessageRecord) {
        val request = browseDocksRequest {
            this.filter = BrowseDocksRequestKt.filter { this.region = rec.status }
            this.orderBy = rec.message
        }
        stub.browseDocks(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeSettleBroadcastsMessage(sinks: Map<String, SettleBroadcastsMessageWriter>, kind: String, rec: SettleBroadcastsMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.hand(rec)
}
