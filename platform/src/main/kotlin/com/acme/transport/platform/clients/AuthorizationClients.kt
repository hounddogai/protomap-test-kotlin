package com.acme.transport.platform.clients

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the authorization API share the mesh channel. */
val authorizationClients = module {
    single { AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.authorization.v1.AccessPolicyQueryService", it)
    } }
    single { AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.authorization.v1.AccessPolicyService", it)
    } }
    single { AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.authorization.v1.AccessPolicyValidationService", it)
    } }
}
