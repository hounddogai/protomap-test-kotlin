package com.acme.transport.platform.clients

import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the featureflags API share the mesh channel. */
val featureflagsClients = module {
    single { FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.featureflags.v1.FeatureFlagQueryService", it)
    } }
    single { FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.featureflags.v1.FeatureFlagService", it)
    } }
    single { FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.featureflags.v1.FeatureFlagValidationService", it)
    } }
}
