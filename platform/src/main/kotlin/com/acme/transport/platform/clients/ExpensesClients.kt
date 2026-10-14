package com.acme.transport.platform.clients

import com.acme.expenses.v1.ExpenseReportCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the expenses API share the mesh channel. */
val expensesClients = module {
    single { ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.expenses.v1.ExpenseReportCalculationService", it)
    } }
}
