package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.quoteCorporateEventRequest
import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.personalization.v1.getProfileRatesRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.BrowseYardsRequestKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.browseYardsRequest
import io.grpc.ManagedChannel

/** Reads acme.corporateevents.v1.CorporateEventCalculationService/QuoteCorporateEvent and hands what it keeps to the sink of its kind. */
suspend fun relayCorporateEventStatus(channel: ManagedChannel) {
    val upstream = CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.quoteCorporateEvent(quoteCorporateEventRequest { this.reason = "nightly-relay-corporate-event-status" }) }
    val rec = RelayCorporateEventStatusRecord(
        status = got.result.status,
        corporateEventId = got.corporateEvent.corporateEventId,
        correlationId = got.result.correlationId,
    )
    for (sink in listOf<RelayCorporateEventStatusSink>(
        RelayCorporateEventStatusYardQuery(YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(channel)),
        RelayCorporateEventStatusProfileCalculation(ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(channel)),
    )) {
        sink.write(rec)
    }
}

/** Where relayCorporateEventStatus hands each record. */
interface RelayCorporateEventStatusSink {
    suspend fun write(rec: RelayCorporateEventStatusRecord)
}

/** What relayCorporateEventStatus keeps of each QuoteCorporateEvent response. */
data class RelayCorporateEventStatusRecord(val status: String, val corporateEventId: String, val correlationId: String)

/** Hands records to acme.yards.v1.YardQueryService. */
class RelayCorporateEventStatusYardQuery(private val stub: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub) : RelayCorporateEventStatusSink {
    override suspend fun write(rec: RelayCorporateEventStatusRecord) {
        val request = browseYardsRequest {
            this.page = pageRequest { this.pageToken = rec.corporateEventId }
            this.filter = BrowseYardsRequestKt.filter { this.query = rec.correlationId }
        }
        stub.browseYards(request)
    }
}

/** Writes records to acme.personalization.v1.ProfileCalculationService. */
class RelayCorporateEventStatusProfileCalculation(private val stub: ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub) : RelayCorporateEventStatusSink {
    override suspend fun write(rec: RelayCorporateEventStatusRecord) {
        RpcBoundary.run("acme.personalization.v1.ProfileCalculationService/GetProfileRates") { stub.getProfileRates(getProfileRatesRequest { this.profileId = rec.correlationId }) }
    }
}
