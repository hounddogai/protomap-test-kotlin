package com.acme.transport.platform.clients

import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the notifications API share the mesh channel. */
val notificationsClients = module {
    single { NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.notifications.v1.NotificationQueryService", it)
    } }
    single { NotificationServiceGrpcKt.NotificationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.notifications.v1.NotificationService", it)
    } }
    single { NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.notifications.v1.NotificationValidationService", it)
    } }
}
