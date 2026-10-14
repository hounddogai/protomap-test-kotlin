package com.acme.transport.platform.clients

import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeServiceGrpcKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the driverapp API share the mesh channel. */
val driverappClients = module {
    single { DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeApprovalService", it)
    } }
    single { DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeIntegrationService", it)
    } }
    single { DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeNotificationService", it)
    } }
    single { DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeQueryService", it)
    } }
    single { DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeReportingService", it)
    } }
    single { DriverHomeServiceGrpcKt.DriverHomeServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeService", it)
    } }
    single { DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverHomeWorkflowService", it)
    } }
    single { DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxApprovalService", it)
    } }
    single { DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxIntegrationService", it)
    } }
    single { DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxNotificationService", it)
    } }
    single { DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxQueryService", it)
    } }
    single { DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxReportingService", it)
    } }
    single { DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxService", it)
    } }
    single { DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.driverapp.v1.DriverInboxWorkflowService", it)
    } }
}
