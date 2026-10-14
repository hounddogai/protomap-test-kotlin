package com.acme.transport.platform.clients

import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the search API share the mesh channel. */
val searchClients = module {
    single { SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.search.v1.SearchIndexQueryService", it)
    } }
    single { SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.search.v1.SearchIndexService", it)
    } }
    single { SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.search.v1.SearchIndexValidationService", it)
    } }
}
