package com.acme.transport.platform.clients

import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the contracts API share the mesh channel. */
val contractsClients = module {
    single { ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.contracts.v1.ContractQueryService", it)
    } }
    single { ContractServiceGrpcKt.ContractServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.contracts.v1.ContractService", it)
    } }
}
