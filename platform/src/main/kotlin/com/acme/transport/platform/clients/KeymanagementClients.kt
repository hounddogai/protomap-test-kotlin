package com.acme.transport.platform.clients

import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the keymanagement API share the mesh channel. */
val keymanagementClients = module {
    single { CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.keymanagement.v1.CryptoKeyCalculationService", it)
    } }
    single { CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.keymanagement.v1.CryptoKeyQueryService", it)
    } }
    single { CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.keymanagement.v1.CryptoKeyService", it)
    } }
}
