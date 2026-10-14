package com.acme.transport.platform.clients

import com.acme.ticketing.v1.TicketQueryServiceGrpcKt
import com.acme.ticketing.v1.TicketServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the ticketing API share the mesh channel. */
val ticketingClients = module {
    single { TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ticketing.v1.TicketQueryService", it)
    } }
    single { TicketServiceGrpcKt.TicketServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.ticketing.v1.TicketService", it)
    } }
}
