package com.acme.transport.platform.clients

import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the pipelines API share the mesh channel. */
val pipelinesClients = module {
    single { PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.pipelines.v1.PipelineCalculationService", it)
    } }
}
