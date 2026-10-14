package com.acme.transport.platform.clients

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.FixedAssetQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the assets API share the mesh channel. */
val assetsClients = module {
    single { FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.assets.v1.FixedAssetCalculationService", it)
    } }
    single { FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.assets.v1.FixedAssetQueryService", it)
    } }
}
