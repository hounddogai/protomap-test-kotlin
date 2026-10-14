package com.acme.transport.platform.clients

import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseQueryServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.DutyStatusApprovalServiceGrpcKt
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the drivers API share the mesh channel. */
val driversClients = module {
    single { DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverApprovalService", it)
    } }
    single { DriverCalculationServiceGrpcKt.DriverCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverCalculationService", it)
    } }
    single { DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseApprovalService", it)
    } }
    single { DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseCalculationService", it)
    } }
    single { DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseLifecycleService", it)
    } }
    single { DriverLicenseQueryServiceGrpcKt.DriverLicenseQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseQueryService", it)
    } }
    single { DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseReportingService", it)
    } }
    single { DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseValidationService", it)
    } }
    single { DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLicenseWorkflowService", it)
    } }
    single { DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverLifecycleService", it)
    } }
    single { DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverQueryService", it)
    } }
    single { DriverServiceGrpcKt.DriverServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverService", it)
    } }
    single { DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverValidationService", it)
    } }
    single { DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DriverWorkflowService", it)
    } }
    single { DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DutyStatusApprovalService", it)
    } }
    single { DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DutyStatusCalculationService", it)
    } }
    single { DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DutyStatusLifecycleService", it)
    } }
    single { DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DutyStatusReportingService", it)
    } }
    single { DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.drivers.v1.DutyStatusValidationService", it)
    } }
}
