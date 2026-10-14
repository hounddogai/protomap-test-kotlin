package com.acme.transport.platform.clients

import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the insurance API share the mesh channel. */
val insuranceClients = module {
    single { PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.insurance.v1.PolicyCalculationService", it)
    } }
}
