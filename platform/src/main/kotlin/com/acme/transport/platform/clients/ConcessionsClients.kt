package com.acme.transport.platform.clients

import com.acme.concessions.v1.ConcessionStandCalculationServiceGrpcKt
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the concessions API share the mesh channel. */
val concessionsClients = module {
    single { ConcessionStandCalculationServiceGrpcKt.ConcessionStandCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.concessions.v1.ConcessionStandCalculationService", it)
    } }
    single { ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.concessions.v1.ConcessionStandQueryService", it)
    } }
    single { ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.concessions.v1.ConcessionStandService", it)
    } }
}
