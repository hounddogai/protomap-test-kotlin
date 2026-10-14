package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.warehousing.v1.WarehouseServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the warehousing API share the mesh channel. */
val warehousingClients = module {
    single { WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.warehousing.v1.WarehouseService", it)
    } }
}
