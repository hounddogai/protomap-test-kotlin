package com.acme.transport.ops.lastmilesync

import com.acme.common.v1.pageRequest
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.suggestLedgerAccountsRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.simulateShowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.findVenuesRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.shows.v1.ShowCalculationService/SimulateShow and hands what it keeps to the sink of its kind. */
suspend fun copyShowId(channel: ManagedChannel) {
    val source = ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.simulateShow(simulateShowRequest { this.showId = "primary-copy-show-id" }) }
    val row = CopyShowIdRecord(
        correlationId = current.result.correlationId,
        currencyCode = current.breakdown.currencyCode,
        status = normalize(current.result.status),
    )
    deliverCopyShowId("ledger-account-query", row, channel)
}

/** Where copyShowId hands each record. */
interface CopyShowIdTarget {
    suspend fun write(rec: CopyShowIdRecord)
}

/** What copyShowId keeps of each SimulateShow response. */
data class CopyShowIdRecord(val correlationId: String, val currencyCode: String, val status: String)

/** Writes records to acme.ledger.v1.LedgerAccountQueryService. */
class CopyShowIdLedgerAccountQuery(private val stub: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub) : CopyShowIdTarget {
    override suspend fun write(rec: CopyShowIdRecord) {
        val request = suggestLedgerAccountsRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
        }
        stub.suggestLedgerAccounts(request)
    }
}

/** Passes records to acme.venues.v1.VenueQueryService. */
class CopyShowIdVenueQuery(private val stub: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub) : CopyShowIdTarget {
    override suspend fun write(rec: CopyShowIdRecord) {
        val request = findVenuesRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
            this.filter = FindVenuesRequestKt.filter { this.region = rec.currencyCode }
        }
        RpcBoundary.run("acme.venues.v1.VenueQueryService/FindVenues") { stub.findVenues(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyShowIdLogOnly : CopyShowIdTarget {
    override suspend fun write(rec: CopyShowIdRecord) {
        LoggerFactory.getLogger("CopyShowIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyShowId(kind: String, rec: CopyShowIdRecord, channel: ManagedChannel) {
    var sink: CopyShowIdTarget = CopyShowIdLogOnly()
    when (kind) {
        "ledger-account-query" -> sink = CopyShowIdLedgerAccountQuery(LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub(channel))
        "venue-query" -> sink = CopyShowIdVenueQuery(VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
