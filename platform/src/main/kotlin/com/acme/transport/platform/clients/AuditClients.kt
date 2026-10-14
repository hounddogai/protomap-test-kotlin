package com.acme.transport.platform.clients

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the audit API share the mesh channel. */
val auditClients = module {
    single { AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.audit.v1.AuditEventQueryService", it)
    } }
    single { AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.audit.v1.AuditEventService", it)
    } }
    single { AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.audit.v1.AuditEventValidationService", it)
    } }
}
