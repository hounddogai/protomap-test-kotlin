package com.acme.transport.platform.clients

import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the shipping API share the mesh channel. */
val shippingClients = module {
    single { ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.shipping.v1.ShipmentCalculationService", it)
    } }
}
