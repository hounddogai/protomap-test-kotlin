package com.acme.transport.platform.clients

import com.acme.ledger.v1.LedgerAccountQueryServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the ledger API share the mesh channel. */
val ledgerClients = module {
    single { LedgerAccountQueryServiceGrpcKt.LedgerAccountQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ledger.v1.LedgerAccountQueryService", it)
    } }
    single { LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ledger.v1.LedgerAccountService", it)
    } }
    single { LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ledger.v1.LedgerAccountValidationService", it)
    } }
}
