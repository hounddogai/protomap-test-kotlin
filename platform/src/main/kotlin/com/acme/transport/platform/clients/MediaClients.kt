package com.acme.transport.platform.clients

import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the media API share the mesh channel. */
val mediaClients = module {
    single { MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.media.v1.MediaAssetCalculationService", it)
    } }
    single { MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.media.v1.MediaAssetQueryService", it)
    } }
}
