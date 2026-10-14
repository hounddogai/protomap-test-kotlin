package com.acme.transport.ops.emissionspublisher

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.quoteCarePlanRequest
import com.acme.fuel.v1.CancelFuelCardWorkflowRequestKt
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.cancelFuelCardWorkflowRequest
import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.personalization.v1.simulateProfileRequest
import com.acme.shows.v1.ShowQueryServiceGrpcKt
import com.acme.shows.v1.lookupShowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.careplans.v1.CarePlanCalculationService/QuoteCarePlan and hands what it keeps to the sink of its kind. */
suspend fun pushCarePlanMessage(channel: ManagedChannel) {
    val upstream = CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.quoteCarePlan(quoteCarePlanRequest { this.carePlanId = "current-push-care-plan-message" }) }
    val row = PushCarePlanMessageRecord(
        message = reply.result.message,
        correlationId = normalize(reply.result.correlationId),
    )
    dispatchPushCarePlanMessage("show-query", row, channel)
}

/** Where pushCarePlanMessage hands each record. */
interface PushCarePlanMessageDestination {
    suspend fun write(rec: PushCarePlanMessageRecord)
}

/** What pushCarePlanMessage keeps of each QuoteCarePlan response. */
data class PushCarePlanMessageRecord(val message: String, val correlationId: String)

/** Sends records to acme.shows.v1.ShowQueryService. */
class PushCarePlanMessageShowQuery(private val stub: ShowQueryServiceGrpcKt.ShowQueryServiceCoroutineStub) : PushCarePlanMessageDestination {
    override suspend fun write(rec: PushCarePlanMessageRecord) {
        RpcBoundary.run("acme.shows.v1.ShowQueryService/LookupShow") { stub.lookupShow(lookupShowRequest { this.showId = rec.message }) }
    }
}

/** Sends records to acme.fuel.v1.FuelCardWorkflowService. */
class PushCarePlanMessageFuelCardWorkflow(private val stub: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub) : PushCarePlanMessageDestination {
    override suspend fun write(rec: PushCarePlanMessageRecord) {
        val request = cancelFuelCardWorkflowRequest {
            this.details = CancelFuelCardWorkflowRequestKt.details { this.initiatedBy = rec.correlationId }
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/CancelFuelCardWorkflow") { stub.cancelFuelCardWorkflow(request) }
    }
}

/** Passes records to acme.personalization.v1.ProfileCalculationService. */
class PushCarePlanMessageProfileCalculation(private val stub: ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub) : PushCarePlanMessageDestination {
    override suspend fun write(rec: PushCarePlanMessageRecord) {
        stub.simulateProfile(simulateProfileRequest { this.reason = rec.message })
    }
}

/** Only logs, for kinds no service takes yet. */
class PushCarePlanMessageLogOnly : PushCarePlanMessageDestination {
    override suspend fun write(rec: PushCarePlanMessageRecord) {
        LoggerFactory.getLogger("PushCarePlanMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPushCarePlanMessage(kind: String, rec: PushCarePlanMessageRecord, channel: ManagedChannel) {
    var write: suspend (PushCarePlanMessageRecord) -> Unit = PushCarePlanMessageLogOnly()::write
    when (kind) {
        "show-query" -> write = PushCarePlanMessageShowQuery(ShowQueryServiceGrpcKt.ShowQueryServiceCoroutineStub(channel))::write
        "fuel-card-workflow" -> write = PushCarePlanMessageFuelCardWorkflow(FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(channel))::write
        "profile-calculation" -> write = PushCarePlanMessageProfileCalculation(ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
