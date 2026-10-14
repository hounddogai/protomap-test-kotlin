package com.acme.transport.platform.clients

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the admissions API share the mesh channel. */
val admissionsClients = module {
    single { AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.admissions.v1.AdmissionCalculationService", it)
    } }
    single { AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.admissions.v1.AdmissionService", it)
    } }
}
