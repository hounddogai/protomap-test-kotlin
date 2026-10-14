package com.acme.transport.platform.clients

import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the corporateevents API share the mesh channel. */
val corporateeventsClients = module {
    single { CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.corporateevents.v1.CorporateEventCalculationService", it)
    } }
    single { CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.corporateevents.v1.CorporateEventService", it)
    } }
}
