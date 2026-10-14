package com.acme.transport.platform.clients

import com.acme.checkout.v1.CheckoutSessionQueryServiceGrpcKt
import com.acme.checkout.v1.CheckoutSessionServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the checkout API share the mesh channel. */
val checkoutClients = module {
    single { CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.checkout.v1.CheckoutSessionQueryService", it)
    } }
    single { CheckoutSessionServiceGrpcKt.CheckoutSessionServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.checkout.v1.CheckoutSessionService", it)
    } }
}
