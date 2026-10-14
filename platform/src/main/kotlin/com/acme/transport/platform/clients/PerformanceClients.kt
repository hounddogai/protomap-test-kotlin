package com.acme.transport.platform.clients

import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.ReviewServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the performance API share the mesh channel. */
val performanceClients = module {
    single { ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.performance.v1.ReviewQueryService", it)
    } }
    single { ReviewServiceGrpcKt.ReviewServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.performance.v1.ReviewService", it)
    } }
}
