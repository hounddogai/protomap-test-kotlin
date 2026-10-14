package com.acme.transport.platform.clients

import com.acme.billing.v1.BillingAccountServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the billing API share the mesh channel. */
val billingClients = module {
    single { BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.billing.v1.BillingAccountService", it)
    } }
}
