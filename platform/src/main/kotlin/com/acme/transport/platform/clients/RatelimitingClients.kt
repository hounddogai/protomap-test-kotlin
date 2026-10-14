package com.acme.transport.platform.clients

import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.RateLimitQueryServiceGrpcKt
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the ratelimiting API share the mesh channel. */
val ratelimitingClients = module {
    single { RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ratelimiting.v1.RateLimitCalculationService", it)
    } }
    single { RateLimitQueryServiceGrpcKt.RateLimitQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ratelimiting.v1.RateLimitQueryService", it)
    } }
    single { RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ratelimiting.v1.RateLimitService", it)
    } }
}
