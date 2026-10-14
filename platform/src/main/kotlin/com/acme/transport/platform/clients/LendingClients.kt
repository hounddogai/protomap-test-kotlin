package com.acme.transport.platform.clients

import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the lending API share the mesh channel. */
val lendingClients = module {
    single { LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.lending.v1.LoanApplicationQueryService", it)
    } }
}
