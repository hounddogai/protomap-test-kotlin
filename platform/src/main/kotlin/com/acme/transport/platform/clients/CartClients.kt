package com.acme.transport.platform.clients

import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the cart API share the mesh channel. */
val cartClients = module {
    single { CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.cart.v1.CartCalculationService", it)
    } }
}
