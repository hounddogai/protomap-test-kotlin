package com.acme.transport.platform.clients

import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.FanSessionServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the fanportal API share the mesh channel. */
val fanportalClients = module {
    single { FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fanportal.v1.FanSessionQueryService", it)
    } }
    single { FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fanportal.v1.FanSessionService", it)
    } }
}
