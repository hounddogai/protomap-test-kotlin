package com.acme.transport.ops.crewingrelay

import com.acme.ledger.v1.BrowseLedgerAccountsRequestKt
import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.browseLedgerAccountsRequest
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.advanceRoutePlanWorkflowRequest
import com.acme.venues.v1.SuggestVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.suggestVenuesRequest
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RoutePlanWorkflowService/AdvanceRoutePlanWorkflow and hands what it keeps to the sink of its kind. */
suspend fun settleRoutePlanWorkflowMessage(channel: ManagedChannel) {
    val upstream = RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel)
    val current = upstream.advanceRoutePlanWorkflow(advanceRoutePlanWorkflowRequest { this.routePlanId = "primary-settle-route-plan-workflow-message" })
    val row = SettleRoutePlanWorkflowMessageRecord(
        message = current.outcome.message,
        number = current.routePlan.number,
    )
    val sinks = mapOf<String, SettleRoutePlanWorkflowMessageDestination>(
        "venue-query" to SettleRoutePlanWorkflowMessageVenueQuery(VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(channel)),
        "ledger-account-query" to SettleRoutePlanWorkflowMessageLedgerAccountQuery(LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("SETTLE_ROUTE_PLAN_WORKFLOW_MESSAGE_TARGET")] ?: return
    sink.forward(row)
}

/** Where settleRoutePlanWorkflowMessage hands each record. */
interface SettleRoutePlanWorkflowMessageDestination {
    suspend fun forward(rec: SettleRoutePlanWorkflowMessageRecord)
}

/** What settleRoutePlanWorkflowMessage keeps of each AdvanceRoutePlanWorkflow response. */
data class SettleRoutePlanWorkflowMessageRecord(val message: String, val number: String)

/** Hands records to acme.venues.v1.VenueQueryService. */
class SettleRoutePlanWorkflowMessageVenueQuery(private val stub: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub) : SettleRoutePlanWorkflowMessageDestination {
    override suspend fun forward(rec: SettleRoutePlanWorkflowMessageRecord) {
        val request = suggestVenuesRequest {
            this.orderBy = rec.number
            this.filter = SuggestVenuesRequestKt.filter { this.query = rec.message }
        }
        stub.suggestVenues(request)
    }
}

/** Passes records to acme.ledger.v1.LedgerAccountQueryService. */
class SettleRoutePlanWorkflowMessageLedgerAccountQuery(private val stub: LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub) : SettleRoutePlanWorkflowMessageDestination {
    override suspend fun forward(rec: SettleRoutePlanWorkflowMessageRecord) {
        val request = browseLedgerAccountsRequest {
            this.filter = BrowseLedgerAccountsRequestKt.filter { this.query = rec.number }
            this.orderBy = rec.message
        }
        stub.browseLedgerAccounts(request)
    }
}
