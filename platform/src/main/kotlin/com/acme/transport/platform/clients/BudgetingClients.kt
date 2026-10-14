package com.acme.transport.platform.clients

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the budgeting API share the mesh channel. */
val budgetingClients = module {
    single { BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.budgeting.v1.BudgetCalculationService", it)
    } }
    single { BudgetServiceGrpcKt.BudgetServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.budgeting.v1.BudgetService", it)
    } }
}
