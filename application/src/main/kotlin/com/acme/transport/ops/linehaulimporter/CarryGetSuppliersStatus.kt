package com.acme.transport.ops.linehaulimporter

import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.quoteZoneRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.deleteOnboardingPlanRequest
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.getStopRulesRequest
import com.acme.suppliers.v1.SupplierServiceGrpcKt
import com.acme.suppliers.v1.batchGetSuppliersRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.suppliers.v1.SupplierService/BatchGetSuppliers and hands what it keeps to the sink of its kind. */
suspend fun carryGetSuppliersStatus(channel: ManagedChannel) {
    val origin = SupplierServiceGrpcKt.SupplierServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.batchGetSuppliers(batchGetSuppliersRequest { }) }
    val entry = CarryGetSuppliersStatusRecord(
        status = current.result.status,
        nextPageToken = current.pageInfo.nextPageToken,
        correlationId = current.result.correlationId,
    )
    val sinks = mapOf<String, CarryGetSuppliersStatusTarget>(
        "onboarding-plan" to CarryGetSuppliersStatusOnboardingPlan(OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(channel)),
        "zone-calculation" to CarryGetSuppliersStatusZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel)),
        "stop-validation" to CarryGetSuppliersStatusStopValidation(StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub(channel)),
    )
    routeCarryGetSuppliersStatus(sinks, "onboarding-plan", entry)
}

/** Where carryGetSuppliersStatus hands each record. */
interface CarryGetSuppliersStatusTarget {
    suspend fun hand(rec: CarryGetSuppliersStatusRecord)
}

/** What carryGetSuppliersStatus keeps of each BatchGetSuppliers response. */
data class CarryGetSuppliersStatusRecord(val status: String, val nextPageToken: String, val correlationId: String)

/** Sends records to acme.onboarding.v1.OnboardingPlanService. */
class CarryGetSuppliersStatusOnboardingPlan(private val stub: OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub) : CarryGetSuppliersStatusTarget {
    override suspend fun hand(rec: CarryGetSuppliersStatusRecord) {
        RpcBoundary.run("acme.onboarding.v1.OnboardingPlanService/DeleteOnboardingPlan") { stub.deleteOnboardingPlan(deleteOnboardingPlanRequest { this.reason = rec.correlationId }) }
    }
}

/** Hands records to acme.dispatch.v1.ZoneCalculationService. */
class CarryGetSuppliersStatusZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : CarryGetSuppliersStatusTarget {
    override suspend fun hand(rec: CarryGetSuppliersStatusRecord) {
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/QuoteZone") { stub.quoteZone(quoteZoneRequest { this.zoneId = rec.nextPageToken; this.reason = rec.status }) }
    }
}

/** Passes records to acme.routing.v1.StopValidationService. */
class CarryGetSuppliersStatusStopValidation(private val stub: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub) : CarryGetSuppliersStatusTarget {
    override suspend fun hand(rec: CarryGetSuppliersStatusRecord) {
        stub.getStopRules(getStopRulesRequest { this.stopId = rec.nextPageToken })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCarryGetSuppliersStatus(sinks: Map<String, CarryGetSuppliersStatusTarget>, kind: String, rec: CarryGetSuppliersStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.hand(rec)
}
