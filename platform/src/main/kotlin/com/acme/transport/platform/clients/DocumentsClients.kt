package com.acme.transport.platform.clients

import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the documents API share the mesh channel. */
val documentsClients = module {
    single { DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.documents.v1.DocumentQueryService", it)
    } }
    single { DocumentServiceGrpcKt.DocumentServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.documents.v1.DocumentService", it)
    } }
    single { DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.documents.v1.DocumentValidationService", it)
    } }
}
