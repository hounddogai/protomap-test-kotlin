package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.BrowseHouseholdsRequestKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.browseHouseholdsRequest
import com.acme.featurestore.v1.FeatureViewQueryServiceGrpcKt
import com.acme.featurestore.v1.summarizeFeatureViewsRequest
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.normalizeLedgerAccountRequest
import com.acme.transport.kit.text.normalize
import com.acme.venues.v1.NormalizeVenueRequestKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.normalizeVenueRequest
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.browseYardsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.ledger.v1.LedgerAccountValidationService/NormalizeLedgerAccount and hands what it keeps to the sink of its kind. */
suspend fun pushLedgerAccountStatus(channel: ManagedChannel) {
    val reader = LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(channel)
    val current = reader.normalizeLedgerAccount(normalizeLedgerAccountRequest { this.ledgerAccountId = "current-push-ledger-account-status" })
    val entry = PushLedgerAccountStatusRecord(
        status = current.outcome.status,
        resultStatus = normalize(current.result.status),
    )
    dispatchPushLedgerAccountStatus("venue-validation", entry, channel)
}

/** Where pushLedgerAccountStatus hands each record. */
interface PushLedgerAccountStatusTarget {
    suspend fun forward(rec: PushLedgerAccountStatusRecord)
}

/** What pushLedgerAccountStatus keeps of each NormalizeLedgerAccount response. */
data class PushLedgerAccountStatusRecord(val status: String, val resultStatus: String)

/** Passes records to acme.customers.v1.HouseholdQueryService. */
class PushLedgerAccountStatusHouseholdQuery(private val stub: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub) : PushLedgerAccountStatusTarget {
    override suspend fun forward(rec: PushLedgerAccountStatusRecord) {
        val request = browseHouseholdsRequest {
            this.filter = BrowseHouseholdsRequestKt.filter { this.region = rec.status }
        }
        stub.browseHouseholds(request)
    }
}

/** Sends records to acme.yards.v1.YardQueryService. */
class PushLedgerAccountStatusYardQuery(private val stub: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub) : PushLedgerAccountStatusTarget {
    override suspend fun forward(rec: PushLedgerAccountStatusRecord) {
        stub.browseYards(browseYardsRequest { this.orderBy = rec.resultStatus })
    }
}

/** Passes records to acme.venues.v1.VenueValidationService. */
class PushLedgerAccountStatusVenueValidation(private val stub: VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub) : PushLedgerAccountStatusTarget {
    override suspend fun forward(rec: PushLedgerAccountStatusRecord) {
        val request = normalizeVenueRequest {
            this.reason = rec.resultStatus
            this.details = NormalizeVenueRequestKt.details { this.locale = rec.status }
        }
        stub.normalizeVenue(request)
    }
}

/** Writes records to acme.featurestore.v1.FeatureViewQueryService. */
class PushLedgerAccountStatusFeatureViewQuery(private val stub: FeatureViewQueryServiceGrpcKt.FeatureViewQueryServiceCoroutineStub) : PushLedgerAccountStatusTarget {
    override suspend fun forward(rec: PushLedgerAccountStatusRecord) {
        val request = summarizeFeatureViewsRequest {
            this.featureViewId = rec.resultStatus
            this.reason = rec.status
        }
        stub.summarizeFeatureViews(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PushLedgerAccountStatusLogOnly : PushLedgerAccountStatusTarget {
    override suspend fun forward(rec: PushLedgerAccountStatusRecord) {
        LoggerFactory.getLogger("PushLedgerAccountStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPushLedgerAccountStatus(kind: String, rec: PushLedgerAccountStatusRecord, channel: ManagedChannel) {
    var write: suspend (PushLedgerAccountStatusRecord) -> Unit = PushLedgerAccountStatusLogOnly()::forward
    when (kind) {
        "household-query" -> write = PushLedgerAccountStatusHouseholdQuery(HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel))::forward
        "yard-query" -> write = PushLedgerAccountStatusYardQuery(YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(channel))::forward
        "venue-validation" -> write = PushLedgerAccountStatusVenueValidation(VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub(channel))::forward
        "feature-view-query" -> write = PushLedgerAccountStatusFeatureViewQuery(FeatureViewQueryServiceGrpcKt.FeatureViewQueryServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
