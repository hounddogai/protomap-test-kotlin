package com.acme.transport.platform.clients

import com.acme.subscriptions.v1.SubscriptionQueryServiceGrpcKt
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the subscriptions API share the mesh channel. */
val subscriptionsClients = module {
    single { SubscriptionQueryServiceGrpcKt.SubscriptionQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.subscriptions.v1.SubscriptionQueryService", it)
    } }
    single { SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.subscriptions.v1.SubscriptionService", it)
    } }
}
