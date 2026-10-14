package com.acme.transport.ops.customsauditor

import com.acme.checkout.v1.CheckoutSessionQueryServiceGrpcKt
import com.acme.checkout.v1.CountCheckoutSessionsRequestKt
import com.acme.checkout.v1.countCheckoutSessionsRequest
import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.SummarizeContractsRequestKt
import com.acme.contracts.v1.summarizeContractsRequest
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.quoteRateLimitRequest
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.lookupSeatMapRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.ListPendingYardsRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.listPendingYardsRequest
import io.grpc.ManagedChannel

/** Reads acme.seating.v1.SeatMapQueryService/LookupSeatMap and hands what it keeps to the sink of its kind. */
suspend fun replaySeatMapMessage(channel: ManagedChannel) {
    val source = SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel)
    val got = source.lookupSeatMap(lookupSeatMapRequest { this.seatMapId = "primary-replay-seat-map-message" })
    val record = ReplaySeatMapMessageRecord(
        message = got.result.message,
        seatMapId = normalize(got.seatMap.seatMapId),
    )
    val sinks = listOf<ReplaySeatMapMessageSink>(
        ReplaySeatMapMessageContractQuery(ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub(channel)),
        ReplaySeatMapMessageYardApproval(YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel)),
        ReplaySeatMapMessageCheckoutSessionQuery(CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub(channel)),
        ReplaySeatMapMessageRateLimitCalculation(RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(channel)),
    )
    sinks[1].hand(record)
}

/** Where replaySeatMapMessage hands each record. */
interface ReplaySeatMapMessageSink {
    suspend fun hand(rec: ReplaySeatMapMessageRecord)
}

/** What replaySeatMapMessage keeps of each LookupSeatMap response. */
data class ReplaySeatMapMessageRecord(val message: String, val seatMapId: String)

/** Sends records to acme.contracts.v1.ContractQueryService. */
class ReplaySeatMapMessageContractQuery(private val stub: ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub) : ReplaySeatMapMessageSink {
    override suspend fun hand(rec: ReplaySeatMapMessageRecord) {
        val request = summarizeContractsRequest {
            this.details = SummarizeContractsRequestKt.details { this.cron = rec.seatMapId }
        }
        stub.summarizeContracts(request)
    }
}

/** Sends records to acme.yards.v1.YardApprovalService. */
class ReplaySeatMapMessageYardApproval(private val stub: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub) : ReplaySeatMapMessageSink {
    override suspend fun hand(rec: ReplaySeatMapMessageRecord) {
        val request = listPendingYardsRequest {
            this.filter = ListPendingYardsRequestKt.filter { this.region = rec.message; this.query = rec.seatMapId }
        }
        stub.listPendingYards(request)
    }
}

/** Hands records to acme.checkout.v1.CheckoutSessionQueryService. */
class ReplaySeatMapMessageCheckoutSessionQuery(private val stub: CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub) : ReplaySeatMapMessageSink {
    override suspend fun hand(rec: ReplaySeatMapMessageRecord) {
        val request = countCheckoutSessionsRequest {
            this.filter = CountCheckoutSessionsRequestKt.filter { this.region = rec.seatMapId }
        }
        stub.countCheckoutSessions(request)
    }
}

/** Hands records to acme.ratelimiting.v1.RateLimitCalculationService. */
class ReplaySeatMapMessageRateLimitCalculation(private val stub: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub) : ReplaySeatMapMessageSink {
    override suspend fun hand(rec: ReplaySeatMapMessageRecord) {
        stub.quoteRateLimit(quoteRateLimitRequest { this.rateLimitId = rec.seatMapId })
    }
}
