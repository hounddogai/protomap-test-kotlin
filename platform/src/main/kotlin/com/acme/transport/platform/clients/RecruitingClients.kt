package com.acme.transport.platform.clients

import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the recruiting API share the mesh channel. */
val recruitingClients = module {
    single { JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.recruiting.v1.JobOpeningService", it)
    } }
}
