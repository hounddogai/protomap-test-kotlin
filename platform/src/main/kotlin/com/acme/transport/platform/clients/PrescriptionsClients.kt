package com.acme.transport.platform.clients

import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.PrescriptionQueryServiceGrpcKt
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the prescriptions API share the mesh channel. */
val prescriptionsClients = module {
    single { PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.prescriptions.v1.PrescriptionCalculationService", it)
    } }
    single { PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.prescriptions.v1.PrescriptionQueryService", it)
    } }
    single { PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.prescriptions.v1.PrescriptionService", it)
    } }
}
