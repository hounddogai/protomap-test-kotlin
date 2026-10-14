package com.acme.transport.platform.clients

import com.acme.edgegateway.v1.RouteBindingQueryServiceGrpcKt
import com.acme.edgegateway.v1.RouteBindingServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the edgegateway API share the mesh channel. */
val edgegatewayClients = module {
    single { RouteBindingQueryServiceGrpcKt.RouteBindingQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.edgegateway.v1.RouteBindingQueryService", it)
    } }
    single { RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.edgegateway.v1.RouteBindingService", it)
    } }
}
