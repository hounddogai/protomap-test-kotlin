package com.acme.transport.platform.clients

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.ProductQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the catalog API share the mesh channel. */
val catalogClients = module {
    single { ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.catalog.v1.ProductCalculationService", it)
    } }
    single { ProductQueryServiceGrpcKt.ProductQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.catalog.v1.ProductQueryService", it)
    } }
}
