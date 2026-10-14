package com.acme.transport.platform.clients

import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the modelregistry API share the mesh channel. */
val modelregistryClients = module {
    single { RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.modelregistry.v1.RegisteredModelQueryService", it)
    } }
}
