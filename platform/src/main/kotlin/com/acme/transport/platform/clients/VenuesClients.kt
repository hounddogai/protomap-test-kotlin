package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the venues API share the mesh channel. */
val venuesClients = module {
    single { VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.venues.v1.VenueQueryService", it)
    } }
    single { VenueServiceGrpcKt.VenueServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.venues.v1.VenueService", it)
    } }
    single { VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.venues.v1.VenueValidationService", it)
    } }
}
