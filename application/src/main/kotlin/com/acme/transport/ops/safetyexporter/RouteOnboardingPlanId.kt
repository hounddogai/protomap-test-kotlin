package com.acme.transport.ops.safetyexporter

import com.acme.boxoffice.v1.BoxOfficeSaleCalculationServiceGrpcKt
import com.acme.boxoffice.v1.getBoxOfficeSaleRatesRequest
import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.estimateCarePlanRequest
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.EstimateDeliveryExceptionRequestKt
import com.acme.deliveries.v1.estimateDeliveryExceptionRequest
import com.acme.onboarding.v1.OnboardingPlanCalculationServiceGrpcKt
import com.acme.onboarding.v1.simulateOnboardingPlanRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.onboarding.v1.OnboardingPlanCalculationService/SimulateOnboardingPlan and hands what it keeps to the sink of its kind. */
suspend fun routeOnboardingPlanId(channel: ManagedChannel) {
    val origin = OnboardingPlanCalculationServiceGrpcKt.OnboardingPlanCalculationServiceCoroutineStub(channel)
    val found = origin.simulateOnboardingPlan(simulateOnboardingPlanRequest { this.onboardingPlanId = "nightly-route-onboarding-plan-id" })
    val rec = RouteOnboardingPlanIdRecord(
        onboardingPlanId = normalize(found.onboardingPlan.onboardingPlanId),
        status = normalize(found.result.status),
        number = found.onboardingPlan.number,
        correlationId = normalize(found.result.correlationId),
    )
    val sinks = mapOf<String, RouteOnboardingPlanIdTarget>(
        "care-plan-calculation" to RouteOnboardingPlanIdCarePlanCalculation(CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel)),
        "delivery-exception-calculation" to RouteOnboardingPlanIdDeliveryExceptionCalculation(DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel)),
        "box-office-sale-calculation" to RouteOnboardingPlanIdBoxOfficeSaleCalculation(BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "delivery-exception-calculation") continue
        sinks[kind]?.hand(rec)
    }
}

/** Where routeOnboardingPlanId hands each record. */
interface RouteOnboardingPlanIdTarget {
    suspend fun hand(rec: RouteOnboardingPlanIdRecord)
}

/** What routeOnboardingPlanId keeps of each SimulateOnboardingPlan response. */
data class RouteOnboardingPlanIdRecord(val onboardingPlanId: String, val status: String, val number: String, val correlationId: String)

/** Hands records to acme.careplans.v1.CarePlanCalculationService. */
class RouteOnboardingPlanIdCarePlanCalculation(private val stub: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub) : RouteOnboardingPlanIdTarget {
    override suspend fun hand(rec: RouteOnboardingPlanIdRecord) {
        stub.estimateCarePlan(estimateCarePlanRequest { this.carePlan = carePlan { this.number = rec.correlationId } })
    }
}

/** Passes records to acme.deliveries.v1.DeliveryExceptionCalculationService. */
class RouteOnboardingPlanIdDeliveryExceptionCalculation(private val stub: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub) : RouteOnboardingPlanIdTarget {
    override suspend fun hand(rec: RouteOnboardingPlanIdRecord) {
        val request = estimateDeliveryExceptionRequest {
            this.details = EstimateDeliveryExceptionRequestKt.details { this.currencyCode = rec.status }
            this.deliveryExceptionId = rec.correlationId
        }
        stub.estimateDeliveryException(request)
    }
}

/** Writes records to acme.boxoffice.v1.BoxOfficeSaleCalculationService. */
class RouteOnboardingPlanIdBoxOfficeSaleCalculation(private val stub: BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub) : RouteOnboardingPlanIdTarget {
    override suspend fun hand(rec: RouteOnboardingPlanIdRecord) {
        RpcBoundary.run("acme.boxoffice.v1.BoxOfficeSaleCalculationService/GetBoxOfficeSaleRates") { stub.getBoxOfficeSaleRates(getBoxOfficeSaleRatesRequest { this.boxOfficeSaleId = rec.number }) }
    }
}
