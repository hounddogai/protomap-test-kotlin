package com.acme.transport.platform.clients

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the appointments API share the mesh channel. */
val appointmentsClients = module {
    single { AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.appointments.v1.AppointmentQueryService", it)
    } }
    single { AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.appointments.v1.AppointmentService", it)
    } }
}
