package com.acme.transport.platform.clients

import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.ReturnRequestServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the returns API share the mesh channel. */
val returnsClients = module {
    single { ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.returns.v1.ReturnRequestCalculationService", it)
    } }
    single { ReturnRequestServiceGrpcKt.ReturnRequestServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.returns.v1.ReturnRequestService", it)
    } }
}
