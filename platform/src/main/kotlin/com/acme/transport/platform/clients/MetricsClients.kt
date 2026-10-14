package com.acme.transport.platform.clients

import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.MetricSeriesValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the metrics API share the mesh channel. */
val metricsClients = module {
    single { MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.metrics.v1.MetricSeriesQueryService", it)
    } }
    single { MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.metrics.v1.MetricSeriesService", it)
    } }
    single { MetricSeriesValidationServiceGrpcKt.MetricSeriesValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.metrics.v1.MetricSeriesValidationService", it)
    } }
}
