package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the webhooks API share the mesh channel. */
val webhooksClients = module {
    single { WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.webhooks.v1.WebhookEndpointService", it)
    } }
}
