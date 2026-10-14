package com.acme.transport.platform.clients

import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the sponsorships API share the mesh channel. */
val sponsorshipsClients = module {
    single { SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.sponsorships.v1.SponsorCalculationService", it)
    } }
    single { SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.sponsorships.v1.SponsorQueryService", it)
    } }
    single { SponsorServiceGrpcKt.SponsorServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.sponsorships.v1.SponsorService", it)
    } }
}
