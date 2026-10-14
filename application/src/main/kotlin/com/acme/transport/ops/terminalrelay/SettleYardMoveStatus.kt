package com.acme.transport.ops.terminalrelay

import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.SummarizeBroadcastsRequestKt
import com.acme.broadcasts.v1.summarizeBroadcastsRequest
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.commentCustomerSegmentApprovalRequest
import com.acme.customers.v1.customerSegment
import com.acme.sponsorships.v1.ExplainSponsorCalculationRequestKt
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.explainSponsorCalculationRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.expireYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardMoveLifecycleService/ExpireYardMove and hands what it keeps to the sink of its kind. */
suspend fun settleYardMoveStatus(channel: ManagedChannel) {
    val origin = YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(channel)
    val found = origin.expireYardMove(expireYardMoveRequest { this.reason = "nightly-settle-yard-move-status" })
    val record = SettleYardMoveStatusRecord(
        status = found.outcome.status,
        referenceId = normalize(found.outcome.referenceId),
        message = found.outcome.message,
        correlationId = found.result.correlationId,
    )
    for (sink in listOf<SettleYardMoveStatusTarget>(
        SettleYardMoveStatusCustomerSegmentApproval(CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel)),
        SettleYardMoveStatusSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel)),
        SettleYardMoveStatusBroadcastQuery(BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(channel)),
    )) {
        sink.transmit(record)
    }
}

/** Where settleYardMoveStatus hands each record. */
interface SettleYardMoveStatusTarget {
    suspend fun transmit(rec: SettleYardMoveStatusRecord)
}

/** What settleYardMoveStatus keeps of each ExpireYardMove response. */
data class SettleYardMoveStatusRecord(val status: String, val referenceId: String, val message: String, val correlationId: String)

/** Sends records to acme.customers.v1.CustomerSegmentApprovalService. */
class SettleYardMoveStatusCustomerSegmentApproval(private val stub: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub) : SettleYardMoveStatusTarget {
    override suspend fun transmit(rec: SettleYardMoveStatusRecord) {
        val request = commentCustomerSegmentApprovalRequest {
            this.customerSegment = customerSegment { this.description = rec.message; this.name = rec.correlationId }
        }
        stub.commentCustomerSegmentApproval(request)
    }
}

/** Hands records to acme.sponsorships.v1.SponsorCalculationService. */
class SettleYardMoveStatusSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : SettleYardMoveStatusTarget {
    override suspend fun transmit(rec: SettleYardMoveStatusRecord) {
        val request = explainSponsorCalculationRequest {
            this.reason = rec.referenceId
            this.sponsorId = rec.status
            this.details = ExplainSponsorCalculationRequestKt.details { this.locale = rec.correlationId }
        }
        RpcBoundary.run("acme.sponsorships.v1.SponsorCalculationService/ExplainSponsorCalculation") { stub.explainSponsorCalculation(request) }
    }
}

/** Hands records to acme.broadcasts.v1.BroadcastQueryService. */
class SettleYardMoveStatusBroadcastQuery(private val stub: BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub) : SettleYardMoveStatusTarget {
    override suspend fun transmit(rec: SettleYardMoveStatusRecord) {
        val request = summarizeBroadcastsRequest {
            this.broadcastId = rec.correlationId
            this.details = SummarizeBroadcastsRequestKt.details { this.format = rec.message }
            this.reason = rec.status
        }
        stub.summarizeBroadcasts(request)
    }
}
