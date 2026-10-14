package com.acme.transport.platform.clients

import com.acme.configuration.v1.ConfigSetCalculationServiceGrpcKt
import com.acme.configuration.v1.ConfigSetQueryServiceGrpcKt
import com.acme.configuration.v1.ConfigSetServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the configuration API share the mesh channel. */
val configurationClients = module {
    single { ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.configuration.v1.ConfigSetCalculationService", it)
    } }
    single { ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.configuration.v1.ConfigSetQueryService", it)
    } }
    single { ConfigSetServiceGrpcKt.ConfigSetServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.configuration.v1.ConfigSetService", it)
    } }
}
