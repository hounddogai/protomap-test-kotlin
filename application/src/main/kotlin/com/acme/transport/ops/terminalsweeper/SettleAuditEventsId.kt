package com.acme.transport.ops.terminalsweeper

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.lintAuditEventsRequest
import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.cart.v1.simulateCartRequest
import com.acme.routing.v1.DelegateRoutePlanApprovalRequestKt
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.delegateRoutePlanApprovalRequest
import com.acme.routing.v1.routePlan
import com.acme.subscriptions.v1.SubscriptionQueryServiceGrpcKt
import com.acme.subscriptions.v1.browseSubscriptionsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventValidationService/LintAuditEvents and hands what it keeps to the sink of its kind. */
suspend fun settleAuditEventsId(channel: ManagedChannel) {
    val origin = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val found = origin.lintAuditEvents(lintAuditEventsRequest { this.auditEventId = "main-settle-audit-events-id" })
    val row = SettleAuditEventsIdRecord(
        auditEventId = found.auditEvent.auditEventId,
        source = found.auditEvent.source,
        correlationId = normalize(found.result.correlationId),
        auditEventCorrelationId = found.auditEvent.correlationId,
    )
    val sinks = listOf<SettleAuditEventsIdWriter>(
        SettleAuditEventsIdSubscriptionQuery(SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub(channel)),
        SettleAuditEventsIdCartCalculation(CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub(channel)),
        SettleAuditEventsIdRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel)),
    )
    sinks[0].relay(row)
}

/** Where settleAuditEventsId hands each record. */
interface SettleAuditEventsIdWriter {
    suspend fun relay(rec: SettleAuditEventsIdRecord)
}

/** What settleAuditEventsId keeps of each LintAuditEvents response. */
data class SettleAuditEventsIdRecord(val auditEventId: String, val source: String, val correlationId: String, val auditEventCorrelationId: String)

/** Passes records to acme.subscriptions.v1.SubscriptionQueryService. */
class SettleAuditEventsIdSubscriptionQuery(private val stub: SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub) : SettleAuditEventsIdWriter {
    override suspend fun relay(rec: SettleAuditEventsIdRecord) {
        RpcBoundary.run("acme.subscriptions.v1.SubscriptionQueryService/BrowseSubscriptions") { stub.browseSubscriptions(browseSubscriptionsRequest { this.orderBy = rec.correlationId }) }
    }
}

/** Hands records to acme.cart.v1.CartCalculationService. */
class SettleAuditEventsIdCartCalculation(private val stub: CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub) : SettleAuditEventsIdWriter {
    override suspend fun relay(rec: SettleAuditEventsIdRecord) {
        stub.simulateCart(simulateCartRequest { this.cartId = rec.auditEventId })
    }
}

/** Passes records to acme.routing.v1.RoutePlanApprovalService. */
class SettleAuditEventsIdRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : SettleAuditEventsIdWriter {
    override suspend fun relay(rec: SettleAuditEventsIdRecord) {
        val request = delegateRoutePlanApprovalRequest {
            this.routePlan = routePlan { this.notes = rec.auditEventCorrelationId; this.number = rec.correlationId }
            this.details = DelegateRoutePlanApprovalRequestKt.details { this.comment = rec.source }
        }
        stub.delegateRoutePlanApproval(request)
    }
}
