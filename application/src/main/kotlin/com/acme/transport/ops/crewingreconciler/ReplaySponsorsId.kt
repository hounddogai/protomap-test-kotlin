package com.acme.transport.ops.crewingreconciler

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.getAccessPolicyRequest
import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.escalateHouseholdRequest
import com.acme.customers.v1.household
import com.acme.orders.v1.OrderCalculationServiceGrpcKt
import com.acme.orders.v1.explainOrderCalculationRequest
import com.acme.orders.v1.order
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.activateRoutePlanRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.browseSponsorsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.sponsorships.v1.SponsorQueryService/BrowseSponsors and hands what it keeps to the sink of its kind. */
suspend fun replaySponsorsId(channel: ManagedChannel) {
    val reader = SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)
    val found = reader.browseSponsors(browseSponsorsRequest { this.orderBy = "current-replay-sponsors-id" })
    val entry = ReplaySponsorsIdRecord(
        correlationId = found.result.correlationId,
        status = found.result.status,
        nextPageToken = normalize(found.pageInfo.nextPageToken),
        message = found.result.message,
    )
    deliverReplaySponsorsId("order-calculation", entry, channel)
}

/** Where replaySponsorsId hands each record. */
interface ReplaySponsorsIdWriter {
    suspend fun forward(rec: ReplaySponsorsIdRecord)
}

/** What replaySponsorsId keeps of each BrowseSponsors response. */
data class ReplaySponsorsIdRecord(val correlationId: String, val status: String, val nextPageToken: String, val message: String)

/** Writes records to acme.authorization.v1.AccessPolicyService. */
class ReplaySponsorsIdAccessPolicy(private val stub: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub) : ReplaySponsorsIdWriter {
    override suspend fun forward(rec: ReplaySponsorsIdRecord) {
        stub.getAccessPolicy(getAccessPolicyRequest { this.accessPolicyId = rec.nextPageToken })
    }
}

/** Hands records to acme.orders.v1.OrderCalculationService. */
class ReplaySponsorsIdOrderCalculation(private val stub: OrderCalculationServiceGrpcKt.OrderCalculationServiceCoroutineStub) : ReplaySponsorsIdWriter {
    override suspend fun forward(rec: ReplaySponsorsIdRecord) {
        RpcBoundary.run("acme.orders.v1.OrderCalculationService/ExplainOrderCalculation") { stub.explainOrderCalculation(explainOrderCalculationRequest { this.order = order { this.orderId = rec.message } }) }
    }
}

/** Writes records to acme.customers.v1.HouseholdApprovalService. */
class ReplaySponsorsIdHouseholdApproval(private val stub: HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub) : ReplaySponsorsIdWriter {
    override suspend fun forward(rec: ReplaySponsorsIdRecord) {
        val request = escalateHouseholdRequest {
            this.household = household { this.name = rec.message; this.parentId = rec.nextPageToken }
        }
        stub.escalateHousehold(request)
    }
}

/** Passes records to acme.routing.v1.RoutePlanLifecycleService. */
class ReplaySponsorsIdRoutePlanLifecycle(private val stub: RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub) : ReplaySponsorsIdWriter {
    override suspend fun forward(rec: ReplaySponsorsIdRecord) {
        val request = activateRoutePlanRequest {
            this.routePlanId = rec.status
            this.reason = rec.correlationId
        }
        stub.activateRoutePlan(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplaySponsorsIdLogOnly : ReplaySponsorsIdWriter {
    override suspend fun forward(rec: ReplaySponsorsIdRecord) {
        LoggerFactory.getLogger("ReplaySponsorsIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverReplaySponsorsId(kind: String, rec: ReplaySponsorsIdRecord, channel: ManagedChannel) {
    var sink: ReplaySponsorsIdWriter = ReplaySponsorsIdLogOnly()
    when (kind) {
        "access-policy" -> sink = ReplaySponsorsIdAccessPolicy(AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub(channel))
        "order-calculation" -> sink = ReplaySponsorsIdOrderCalculation(OrderCalculationServiceGrpcKt.OrderCalculationServiceCoroutineStub(channel))
        "household-approval" -> sink = ReplaySponsorsIdHouseholdApproval(HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub(channel))
        "route-plan-lifecycle" -> sink = ReplaySponsorsIdRoutePlanLifecycle(RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
