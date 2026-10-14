package com.acme.transport.platform.clients

import com.acme.community.v1.ForumServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the community API share the mesh channel. */
val communityClients = module {
    single { ForumServiceGrpcKt.ForumServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.community.v1.ForumService", it)
    } }
}
