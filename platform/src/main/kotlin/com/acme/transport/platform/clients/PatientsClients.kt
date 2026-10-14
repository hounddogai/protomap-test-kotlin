package com.acme.transport.platform.clients

import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.PatientQueryServiceGrpcKt
import com.acme.patients.v1.PatientServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the patients API share the mesh channel. */
val patientsClients = module {
    single { PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.patients.v1.PatientCalculationService", it)
    } }
    single { PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.patients.v1.PatientQueryService", it)
    } }
    single { PatientServiceGrpcKt.PatientServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.patients.v1.PatientService", it)
    } }
}
