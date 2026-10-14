package com.acme.transport.platform.clients

import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the payroll API share the mesh channel. */
val payrollClients = module {
    single { PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.payroll.v1.PayRunCalculationService", it)
    } }
}
