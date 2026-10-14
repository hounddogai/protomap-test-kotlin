package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the treasury API share the mesh channel. */
val treasuryClients = module {
    single { CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.treasury.v1.CashPositionCalculationService", it)
    } }
}
