package com.acme.transport.platform.clients

import com.acme.risk.v1.RiskRegisterQueryServiceGrpcKt
import com.acme.risk.v1.RiskRegisterServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the risk API share the mesh channel. */
val riskClients = module {
    single { RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.risk.v1.RiskRegisterQueryService", it)
    } }
    single { RiskRegisterServiceGrpcKt.RiskRegisterServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.risk.v1.RiskRegisterService", it)
    } }
}
