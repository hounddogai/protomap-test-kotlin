package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the workplaces API share the mesh channel. */
val workplacesClients = module {
    single { BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.workplaces.v1.BuildingQueryService", it)
    } }
}
