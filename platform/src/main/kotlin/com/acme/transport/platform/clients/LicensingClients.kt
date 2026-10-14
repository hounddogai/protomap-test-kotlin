package com.acme.transport.platform.clients

import com.acme.licensing.v1.SoftwareLicenseServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the licensing API share the mesh channel. */
val licensingClients = module {
    single { SoftwareLicenseServiceGrpcKt.SoftwareLicenseServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.licensing.v1.SoftwareLicenseService", it)
    } }
}
