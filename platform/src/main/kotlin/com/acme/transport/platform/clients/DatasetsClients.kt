package com.acme.transport.platform.clients

import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the datasets API share the mesh channel. */
val datasetsClients = module {
    single { DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.datasets.v1.DatasetQueryService", it)
    } }
    single { DatasetServiceGrpcKt.DatasetServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.datasets.v1.DatasetService", it)
    } }
}
