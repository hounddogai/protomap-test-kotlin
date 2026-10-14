package com.acme.transport.ops.freightpaysweeper

import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.FindCustomersRequestKt
import com.acme.customers.v1.findCustomersRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.batchGetOnboardingPlansRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.estimateYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel

/** Reads acme.onboarding.v1.OnboardingPlanService/BatchGetOnboardingPlans and hands what it keeps to the sink of its kind. */
suspend fun mirrorGetOnboardingPlansToken(channel: ManagedChannel) {
    val source = OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.batchGetOnboardingPlans(batchGetOnboardingPlansRequest { }) }
    val record = MirrorGetOnboardingPlansTokenRecord(
        nextPageToken = current.pageInfo.nextPageToken,
        message = current.result.message,
        status = normalize(current.result.status),
        correlationId = current.result.correlationId,
    )
    actOnMirrorGetOnboardingPlansToken("yard-move-calculation", record, channel)
}

/** Where mirrorGetOnboardingPlansToken hands each record. */
interface MirrorGetOnboardingPlansTokenDestination {
    suspend fun forward(rec: MirrorGetOnboardingPlansTokenRecord)
}

/** What mirrorGetOnboardingPlansToken keeps of each BatchGetOnboardingPlans response. */
data class MirrorGetOnboardingPlansTokenRecord(val nextPageToken: String, val message: String, val status: String, val correlationId: String)

/** Sends records to acme.yards.v1.YardMoveCalculationService. */
class MirrorGetOnboardingPlansTokenYardMoveCalculation(private val stub: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub) : MirrorGetOnboardingPlansTokenDestination {
    override suspend fun forward(rec: MirrorGetOnboardingPlansTokenRecord) {
        val request = estimateYardMoveRequest {
            this.yardMoveId = rec.status
            this.reason = rec.correlationId
            this.yardMove = yardMove { this.yardMoveId = rec.nextPageToken }
        }
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/EstimateYardMove") { stub.estimateYardMove(request) }
    }
}

/** Writes records to acme.customers.v1.CustomerQueryService. */
class MirrorGetOnboardingPlansTokenCustomerQuery(private val stub: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub) : MirrorGetOnboardingPlansTokenDestination {
    override suspend fun forward(rec: MirrorGetOnboardingPlansTokenRecord) {
        val request = findCustomersRequest {
            this.filter = FindCustomersRequestKt.filter { this.query = rec.nextPageToken }
            this.orderBy = rec.status
        }
        stub.findCustomers(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnMirrorGetOnboardingPlansToken(kind: String, rec: MirrorGetOnboardingPlansTokenRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "yard-move-calculation" to { MirrorGetOnboardingPlansTokenYardMoveCalculation(YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub(channel)).forward(rec) },
        "customer-query" to { MirrorGetOnboardingPlansTokenCustomerQuery(CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
