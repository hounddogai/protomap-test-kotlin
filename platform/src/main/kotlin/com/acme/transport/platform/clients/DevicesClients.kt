package com.acme.transport.platform.clients

import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the devices API share the mesh channel. */
val devicesClients = module {
    single { ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.devices.v1.ManagedDeviceQueryService", it)
    } }
}
