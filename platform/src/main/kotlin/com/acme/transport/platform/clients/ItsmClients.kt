package com.acme.transport.platform.clients

import com.acme.itsm.v1.IncidentServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the itsm API share the mesh channel. */
val itsmClients = module {
    single { IncidentServiceGrpcKt.IncidentServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.itsm.v1.IncidentService", it)
    } }
}
