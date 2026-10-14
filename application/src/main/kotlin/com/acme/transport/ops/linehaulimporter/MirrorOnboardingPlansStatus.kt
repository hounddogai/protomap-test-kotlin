package com.acme.transport.ops.linehaulimporter

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.getAdmissionRatesRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.getInspectionLifecycleRequest
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.onboarding.v1.searchOnboardingPlansRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.rateLimit
import com.acme.ratelimiting.v1.updateRateLimitRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.onboarding.v1.OnboardingPlanService/SearchOnboardingPlans and hands what it keeps to the sink of its kind. */
suspend fun mirrorOnboardingPlansStatus(channel: ManagedChannel) {
    val origin = OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(channel)
    val current = origin.searchOnboardingPlans(searchOnboardingPlansRequest { this.orderBy = "current-mirror-onboarding-plans-status" })
    val record = MirrorOnboardingPlansStatusRecord(
        status = current.result.status,
        correlationId = current.result.correlationId,
        message = current.result.message,
    )
    val sinks = mapOf<String, MirrorOnboardingPlansStatusWriter>(
        "inspection-lifecycle" to MirrorOnboardingPlansStatusInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)),
        "admission-calculation" to MirrorOnboardingPlansStatusAdmissionCalculation(AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(channel)),
        "rate-limit" to MirrorOnboardingPlansStatusRateLimit(RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub(channel)),
    )
    routeMirrorOnboardingPlansStatus(sinks, "inspection-lifecycle", record)
}

/** Where mirrorOnboardingPlansStatus hands each record. */
interface MirrorOnboardingPlansStatusWriter {
    suspend fun transmit(rec: MirrorOnboardingPlansStatusRecord)
}

/** What mirrorOnboardingPlansStatus keeps of each SearchOnboardingPlans response. */
data class MirrorOnboardingPlansStatusRecord(val status: String, val correlationId: String, val message: String)

/** Hands records to acme.fleet.v1.InspectionLifecycleService. */
class MirrorOnboardingPlansStatusInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : MirrorOnboardingPlansStatusWriter {
    override suspend fun transmit(rec: MirrorOnboardingPlansStatusRecord) {
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/GetInspectionLifecycle") { stub.getInspectionLifecycle(getInspectionLifecycleRequest { this.inspectionId = rec.correlationId }) }
    }
}

/** Sends records to acme.admissions.v1.AdmissionCalculationService. */
class MirrorOnboardingPlansStatusAdmissionCalculation(private val stub: AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub) : MirrorOnboardingPlansStatusWriter {
    override suspend fun transmit(rec: MirrorOnboardingPlansStatusRecord) {
        RpcBoundary.run("acme.admissions.v1.AdmissionCalculationService/GetAdmissionRates") { stub.getAdmissionRates(getAdmissionRatesRequest { this.admissionId = rec.status }) }
    }
}

/** Writes records to acme.ratelimiting.v1.RateLimitService. */
class MirrorOnboardingPlansStatusRateLimit(private val stub: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub) : MirrorOnboardingPlansStatusWriter {
    override suspend fun transmit(rec: MirrorOnboardingPlansStatusRecord) {
        val request = updateRateLimitRequest {
            this.rateLimit = rateLimit { this.description = rec.correlationId }
        }
        RpcBoundary.run("acme.ratelimiting.v1.RateLimitService/UpdateRateLimit") { stub.updateRateLimit(request) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeMirrorOnboardingPlansStatus(sinks: Map<String, MirrorOnboardingPlansStatusWriter>, kind: String, rec: MirrorOnboardingPlansStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
