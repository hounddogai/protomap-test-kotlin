package com.acme.transport.platform.clients

import com.acme.boxoffice.v1.BoxOfficeSaleCalculationServiceGrpcKt
import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.BoxOfficeSaleServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the boxoffice API share the mesh channel. */
val boxofficeClients = module {
    single { BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.boxoffice.v1.BoxOfficeSaleCalculationService", it)
    } }
    single { BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.boxoffice.v1.BoxOfficeSaleQueryService", it)
    } }
    single { BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.boxoffice.v1.BoxOfficeSaleService", it)
    } }
}
