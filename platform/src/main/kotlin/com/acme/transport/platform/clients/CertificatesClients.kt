package com.acme.transport.platform.clients

import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the certificates API share the mesh channel. */
val certificatesClients = module {
    single { CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.certificates.v1.CertificateCalculationService", it)
    } }
    single { CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.certificates.v1.CertificateQueryService", it)
    } }
    single { CertificateServiceGrpcKt.CertificateServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.certificates.v1.CertificateService", it)
    } }
}
