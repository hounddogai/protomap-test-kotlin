package com.acme.transport.platform.clients

import com.acme.orders.v1.OrderCalculationServiceGrpcKt
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the orders API share the mesh channel. */
val ordersClients = module {
    single { OrderCalculationServiceGrpcKt.OrderCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.orders.v1.OrderCalculationService", it)
    } }
    single { OrderServiceGrpcKt.OrderServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.orders.v1.OrderService", it)
    } }
}
