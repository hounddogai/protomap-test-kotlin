package com.acme.transport.platform.clients

import com.acme.audiences.v1.AudienceQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the audiences API share the mesh channel. */
val audiencesClients = module {
    single { AudienceQueryServiceGrpcKt.AudienceQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.audiences.v1.AudienceQueryService", it)
    } }
}
