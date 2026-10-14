package com.acme.transport.platform.clients

import com.acme.onboarding.v1.OnboardingPlanCalculationServiceGrpcKt
import com.acme.onboarding.v1.OnboardingPlanServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the onboarding API share the mesh channel. */
val onboardingClients = module {
    single { OnboardingPlanCalculationServiceGrpcKt.OnboardingPlanCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.onboarding.v1.OnboardingPlanCalculationService", it)
    } }
    single { OnboardingPlanServiceGrpcKt.OnboardingPlanServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.onboarding.v1.OnboardingPlanService", it)
    } }
}
