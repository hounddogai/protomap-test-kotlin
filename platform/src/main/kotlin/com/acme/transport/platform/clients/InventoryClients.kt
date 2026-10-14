package com.acme.transport.platform.clients

import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the inventory API share the mesh channel. */
val inventoryClients = module {
    single { StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.inventory.v1.StockItemQueryService", it)
    } }
    single { StockItemServiceGrpcKt.StockItemServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.inventory.v1.StockItemService", it)
    } }
}
