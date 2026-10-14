package com.acme.transport.platform.clients

import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the secrets API share the mesh channel. */
val secretsClients = module {
    single { SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.secrets.v1.SecretQueryService", it)
    } }
    single { SecretServiceGrpcKt.SecretServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.secrets.v1.SecretService", it)
    } }
}
