package com.acme.transport.platform.clients

import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the patientapp API share the mesh channel. */
val patientappClients = module {
    single { PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.patientapp.v1.PatientHomeQueryService", it)
    } }
}
