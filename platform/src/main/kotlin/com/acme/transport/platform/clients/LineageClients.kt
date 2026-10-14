package com.acme.transport.platform.clients

import com.acme.lineage.v1.LineageNodeCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the lineage API share the mesh channel. */
val lineageClients = module {
    single { LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.lineage.v1.LineageNodeCalculationService", it)
    } }
}
