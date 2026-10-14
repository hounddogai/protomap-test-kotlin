package com.acme.transport.ops.freightpayreplicator

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.explainAdmissionCalculationRequest
import com.acme.common.v1.pageRequest
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.browseSearchIndexesRequest
import com.acme.search.v1.deleteSearchIndexRequest
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.suggestSeatMapsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.admissions.v1.AdmissionCalculationService/ExplainAdmissionCalculation and hands what it keeps to the sink of its kind. */
suspend fun settleAdmissionCalculationKind(channel: ManagedChannel) {
    val origin = AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.explainAdmissionCalculation(explainAdmissionCalculationRequest { this.reason = "main-settle-admission-calculation-kind" }) }
    val entry = SettleAdmissionCalculationKindRecord(
        kind = normalize(got.admission.kind),
        message = got.outcome.message,
    )
    dispatchSettleAdmissionCalculationKind("search-index-query", entry, channel)
}

/** Where settleAdmissionCalculationKind hands each record. */
interface SettleAdmissionCalculationKindWriter {
    suspend fun transmit(rec: SettleAdmissionCalculationKindRecord)
}

/** What settleAdmissionCalculationKind keeps of each ExplainAdmissionCalculation response. */
data class SettleAdmissionCalculationKindRecord(val kind: String, val message: String)

/** Writes records to acme.search.v1.SearchIndexQueryService. */
class SettleAdmissionCalculationKindSearchIndexQuery(private val stub: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub) : SettleAdmissionCalculationKindWriter {
    override suspend fun transmit(rec: SettleAdmissionCalculationKindRecord) {
        stub.browseSearchIndexes(browseSearchIndexesRequest { this.page = pageRequest { this.pageToken = rec.kind } })
    }
}

/** Hands records to acme.seating.v1.SeatMapQueryService. */
class SettleAdmissionCalculationKindSeatMapQuery(private val stub: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub) : SettleAdmissionCalculationKindWriter {
    override suspend fun transmit(rec: SettleAdmissionCalculationKindRecord) {
        RpcBoundary.run("acme.seating.v1.SeatMapQueryService/SuggestSeatMaps") { stub.suggestSeatMaps(suggestSeatMapsRequest { this.page = pageRequest { this.pageToken = rec.message } }) }
    }
}

/** Writes records to acme.search.v1.SearchIndexService. */
class SettleAdmissionCalculationKindSearchIndex(private val stub: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub) : SettleAdmissionCalculationKindWriter {
    override suspend fun transmit(rec: SettleAdmissionCalculationKindRecord) {
        stub.deleteSearchIndex(deleteSearchIndexRequest { this.reason = rec.message; this.etag = rec.kind })
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleAdmissionCalculationKindLogOnly : SettleAdmissionCalculationKindWriter {
    override suspend fun transmit(rec: SettleAdmissionCalculationKindRecord) {
        LoggerFactory.getLogger("SettleAdmissionCalculationKindRecord").atInfo().addKeyValue("kind", rec.kind).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchSettleAdmissionCalculationKind(kind: String, rec: SettleAdmissionCalculationKindRecord, channel: ManagedChannel) {
    var write: suspend (SettleAdmissionCalculationKindRecord) -> Unit = SettleAdmissionCalculationKindLogOnly()::transmit
    when (kind) {
        "search-index-query" -> write = SettleAdmissionCalculationKindSearchIndexQuery(SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub(channel))::transmit
        "seat-map-query" -> write = SettleAdmissionCalculationKindSeatMapQuery(SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel))::transmit
        "search-index" -> write = SettleAdmissionCalculationKindSearchIndex(SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub(channel))::transmit
    }
    write(rec)
}
