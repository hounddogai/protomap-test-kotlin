package com.acme.transport.platform.clients

import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.ShowQueryServiceGrpcKt
import com.acme.shows.v1.ShowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the shows API share the mesh channel. */
val showsClients = module {
    single { ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.shows.v1.ShowCalculationService", it)
    } }
    single { ShowQueryServiceGrpcKt.ShowQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.shows.v1.ShowQueryService", it)
    } }
    single { ShowServiceGrpcKt.ShowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.shows.v1.ShowService", it)
    } }
}
