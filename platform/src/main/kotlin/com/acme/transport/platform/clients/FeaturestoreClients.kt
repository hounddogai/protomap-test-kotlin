package com.acme.transport.platform.clients

import com.acme.featurestore.v1.FeatureViewQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the featurestore API share the mesh channel. */
val featurestoreClients = module {
    single { FeatureViewQueryServiceGrpcKt.FeatureViewQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.featurestore.v1.FeatureViewQueryService", it)
    } }
}
