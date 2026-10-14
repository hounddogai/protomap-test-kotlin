package com.acme.transport.platform.clients

import com.acme.suppliers.v1.SupplierServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the suppliers API share the mesh channel. */
val suppliersClients = module {
    single { SupplierServiceGrpcKt.SupplierServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.suppliers.v1.SupplierService", it)
    } }
}
