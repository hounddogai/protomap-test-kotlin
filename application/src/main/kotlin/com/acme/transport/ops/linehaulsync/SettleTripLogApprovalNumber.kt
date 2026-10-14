package com.acme.transport.ops.linehaulsync

import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.browseBroadcastsRequest
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.SummarizeDeliveriesRequestKt
import com.acme.deliveries.v1.summarizeDeliveriesRequest
import com.acme.lending.v1.BrowseLoanApplicationsRequestKt
import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.lending.v1.browseLoanApplicationsRequest
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.commentTripLogApprovalRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.quoteYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogApprovalService/CommentTripLogApproval and hands what it keeps to the sink of its kind. */
suspend fun settleTripLogApprovalNumber(channel: ManagedChannel) {
    val source = TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel)
    val reply = source.commentTripLogApproval(commentTripLogApprovalRequest { this.reason = "live-settle-trip-log-approval-number" })
    val entry = SettleTripLogApprovalNumberRecord(
        number = normalize(reply.tripLog.number),
        correlationId = normalize(reply.result.correlationId),
        referenceId = reply.outcome.referenceId,
        message = reply.outcome.message,
    )
    val sinks = mapOf<String, SettleTripLogApprovalNumberTarget>(
        "yard-move-calculation" to SettleTripLogApprovalNumberYardMoveCalculation(YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub(channel)),
        "broadcast-query" to SettleTripLogApprovalNumberBroadcastQuery(BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(channel)),
        "loan-application-query" to SettleTripLogApprovalNumberLoanApplicationQuery(LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub(channel)),
        "delivery-query" to SettleTripLogApprovalNumberDeliveryQuery(DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("SETTLE_TRIP_LOG_APPROVAL_NUMBER_TARGET")] ?: return
    sink.hand(entry)
}

/** Where settleTripLogApprovalNumber hands each record. */
interface SettleTripLogApprovalNumberTarget {
    suspend fun hand(rec: SettleTripLogApprovalNumberRecord)
}

/** What settleTripLogApprovalNumber keeps of each CommentTripLogApproval response. */
data class SettleTripLogApprovalNumberRecord(val number: String, val correlationId: String, val referenceId: String, val message: String)

/** Writes records to acme.yards.v1.YardMoveCalculationService. */
class SettleTripLogApprovalNumberYardMoveCalculation(private val stub: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub) : SettleTripLogApprovalNumberTarget {
    override suspend fun hand(rec: SettleTripLogApprovalNumberRecord) {
        val request = quoteYardMoveRequest {
            this.reason = rec.number
            this.yardMoveId = rec.message
            this.yardMove = yardMove { this.yardMoveId = rec.correlationId }
        }
        stub.quoteYardMove(request)
    }
}

/** Sends records to acme.broadcasts.v1.BroadcastQueryService. */
class SettleTripLogApprovalNumberBroadcastQuery(private val stub: BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub) : SettleTripLogApprovalNumberTarget {
    override suspend fun hand(rec: SettleTripLogApprovalNumberRecord) {
        stub.browseBroadcasts(browseBroadcastsRequest { this.orderBy = rec.number })
    }
}

/** Writes records to acme.lending.v1.LoanApplicationQueryService. */
class SettleTripLogApprovalNumberLoanApplicationQuery(private val stub: LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub) : SettleTripLogApprovalNumberTarget {
    override suspend fun hand(rec: SettleTripLogApprovalNumberRecord) {
        val request = browseLoanApplicationsRequest {
            this.filter = BrowseLoanApplicationsRequestKt.filter { this.ownerId = rec.referenceId }
            this.orderBy = rec.correlationId
        }
        RpcBoundary.run("acme.lending.v1.LoanApplicationQueryService/BrowseLoanApplications") { stub.browseLoanApplications(request) }
    }
}

/** Passes records to acme.deliveries.v1.DeliveryQueryService. */
class SettleTripLogApprovalNumberDeliveryQuery(private val stub: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub) : SettleTripLogApprovalNumberTarget {
    override suspend fun hand(rec: SettleTripLogApprovalNumberRecord) {
        val request = summarizeDeliveriesRequest {
            this.details = SummarizeDeliveriesRequestKt.details { this.format = rec.correlationId }
        }
        stub.summarizeDeliveries(request)
    }
}
