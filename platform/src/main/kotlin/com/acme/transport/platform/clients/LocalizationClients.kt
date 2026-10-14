package com.acme.transport.platform.clients

import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the localization API share the mesh channel. */
val localizationClients = module {
    single { TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.localization.v1.TranslationKeyCalculationService", it)
    } }
}
