package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.TripServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the travel API share the mesh channel. */
val travelClients = module {
    single { TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.travel.v1.TripCalculationService", it)
    } }
    single { TripServiceGrpcKt.TripServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.travel.v1.TripService", it)
    } }
}
