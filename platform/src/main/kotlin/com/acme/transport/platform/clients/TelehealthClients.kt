package com.acme.transport.platform.clients

import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the telehealth API share the mesh channel. */
val telehealthClients = module {
    single { VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telehealth.v1.VideoVisitCalculationService", it)
    } }
    single { VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telehealth.v1.VideoVisitQueryService", it)
    } }
}
