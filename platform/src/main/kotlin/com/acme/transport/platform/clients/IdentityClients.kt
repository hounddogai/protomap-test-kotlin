package com.acme.transport.platform.clients

import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the identity API share the mesh channel. */
val identityClients = module {
    single { UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.identity.v1.UserQueryService", it)
    } }
    single { UserServiceGrpcKt.UserServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.identity.v1.UserService", it)
    } }
    single { UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.identity.v1.UserValidationService", it)
    } }
}
