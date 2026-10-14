package com.acme.transport.platform.clients

import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the learning API share the mesh channel. */
val learningClients = module {
    single { CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.learning.v1.CourseQueryService", it)
    } }
}
