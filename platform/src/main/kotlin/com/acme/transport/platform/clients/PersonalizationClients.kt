package com.acme.transport.platform.clients

import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the personalization API share the mesh channel. */
val personalizationClients = module {
    single { ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.personalization.v1.ProfileCalculationService", it)
    } }
}
