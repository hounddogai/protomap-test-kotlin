package com.acme.transport.platform.clients

import com.acme.compliance.v1.ComplianceCaseCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the compliance API share the mesh channel. */
val complianceClients = module {
    single { ComplianceCaseCalculationServiceGrpcKt.ComplianceCaseCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.compliance.v1.ComplianceCaseCalculationService", it)
    } }
}
