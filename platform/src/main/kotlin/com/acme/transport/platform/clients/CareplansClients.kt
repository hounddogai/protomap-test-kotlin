package com.acme.transport.platform.clients

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.CarePlanServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the careplans API share the mesh channel. */
val careplansClients = module {
    single { CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.careplans.v1.CarePlanCalculationService", it)
    } }
    single { CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.careplans.v1.CarePlanQueryService", it)
    } }
    single { CarePlanServiceGrpcKt.CarePlanServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.careplans.v1.CarePlanService", it)
    } }
}
