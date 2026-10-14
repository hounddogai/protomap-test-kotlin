package com.acme.transport.platform.clients

import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the broadcasts API share the mesh channel. */
val broadcastsClients = module {
    single { BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.broadcasts.v1.BroadcastQueryService", it)
    } }
    single { BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.broadcasts.v1.BroadcastService", it)
    } }
}
