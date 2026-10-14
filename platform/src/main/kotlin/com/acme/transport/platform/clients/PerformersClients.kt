package com.acme.transport.platform.clients

import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the performers API share the mesh channel. */
val performersClients = module {
    single { PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.performers.v1.PerformerQueryService", it)
    } }
    single { PerformerServiceGrpcKt.PerformerServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.performers.v1.PerformerService", it)
    } }
}
