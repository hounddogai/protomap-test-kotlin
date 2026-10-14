package com.acme.transport.ops.linehaulsync

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.budgeting.v1.getBudgetRequest
import com.acme.subscriptions.v1.SubscriptionQueryServiceGrpcKt
import com.acme.subscriptions.v1.browseSubscriptionsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.subscriptions.v1.SubscriptionQueryService/BrowseSubscriptions and hands what it keeps to the sink of its kind. */
suspend fun routeSubscriptionsToken(channel: ManagedChannel) {
    val upstream = SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub(channel)
    val got = upstream.browseSubscriptions(browseSubscriptionsRequest { this.orderBy = "nightly-route-subscriptions-token" })
    val row = RouteSubscriptionsTokenRecord(
        nextPageToken = normalize(got.pageInfo.nextPageToken),
        correlationId = got.result.correlationId,
        status = got.result.status,
    )
    deliverRouteSubscriptionsToken("audit-event-validation", row, channel)
}

/** Where routeSubscriptionsToken hands each record. */
interface RouteSubscriptionsTokenTarget {
    suspend fun transmit(rec: RouteSubscriptionsTokenRecord)
}

/** What routeSubscriptionsToken keeps of each BrowseSubscriptions response. */
data class RouteSubscriptionsTokenRecord(val nextPageToken: String, val correlationId: String, val status: String)

/** Hands records to acme.audit.v1.AuditEventValidationService. */
class RouteSubscriptionsTokenAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : RouteSubscriptionsTokenTarget {
    override suspend fun transmit(rec: RouteSubscriptionsTokenRecord) {
        val request = validateAuditEventRequest {
            this.auditEvent = auditEvent { this.correlationId = rec.nextPageToken }
        }
        stub.validateAuditEvent(request)
    }
}

/** Writes records to acme.budgeting.v1.BudgetService. */
class RouteSubscriptionsTokenBudget(private val stub: BudgetServiceGrpcKt.BudgetServiceCoroutineStub) : RouteSubscriptionsTokenTarget {
    override suspend fun transmit(rec: RouteSubscriptionsTokenRecord) {
        stub.getBudget(getBudgetRequest { this.budgetId = rec.nextPageToken })
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteSubscriptionsTokenLogOnly : RouteSubscriptionsTokenTarget {
    override suspend fun transmit(rec: RouteSubscriptionsTokenRecord) {
        LoggerFactory.getLogger("RouteSubscriptionsTokenRecord").atInfo().addKeyValue("nextPageToken", rec.nextPageToken).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRouteSubscriptionsToken(kind: String, rec: RouteSubscriptionsTokenRecord, channel: ManagedChannel) {
    var sink: RouteSubscriptionsTokenTarget = RouteSubscriptionsTokenLogOnly()
    when (kind) {
        "audit-event-validation" -> sink = RouteSubscriptionsTokenAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel))
        "budget" -> sink = RouteSubscriptionsTokenBudget(BudgetServiceGrpcKt.BudgetServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
