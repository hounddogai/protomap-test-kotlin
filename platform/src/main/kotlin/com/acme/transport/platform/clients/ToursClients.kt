package com.acme.transport.platform.clients

import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the tours API share the mesh channel. */
val toursClients = module {
    single { TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.tours.v1.TourQueryService", it)
    } }
    single { TourServiceGrpcKt.TourServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.tours.v1.TourService", it)
    } }
}
