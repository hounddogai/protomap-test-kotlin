package com.acme.transport.platform.clients

import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the chat API share the mesh channel. */
val chatClients = module {
    single { ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.chat.v1.ChatRoomCalculationService", it)
    } }
}
