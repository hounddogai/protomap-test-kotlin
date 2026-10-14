package com.acme.transport.platform.clients

import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the seating API share the mesh channel. */
val seatingClients = module {
    single { SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.seating.v1.SeatMapCalculationService", it)
    } }
    single { SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.seating.v1.SeatMapQueryService", it)
    } }
    single { SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.seating.v1.SeatMapService", it)
    } }
}
