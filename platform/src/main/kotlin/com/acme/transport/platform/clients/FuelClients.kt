package com.acme.transport.platform.clients

import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.FuelCardSyncServiceGrpcKt
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionExportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionLifecycleServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the fuel API share the mesh channel. */
val fuelClients = module {
    single { FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardApprovalService", it)
    } }
    single { FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardCalculationService", it)
    } }
    single { FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardExportService", it)
    } }
    single { FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardImportService", it)
    } }
    single { FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardLifecycleService", it)
    } }
    single { FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardQueryService", it)
    } }
    single { FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardReportingService", it)
    } }
    single { FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardService", it)
    } }
    single { FuelCardSyncServiceGrpcKt.FuelCardSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardSyncService", it)
    } }
    single { FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardValidationService", it)
    } }
    single { FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelCardWorkflowService", it)
    } }
    single { FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyApprovalService", it)
    } }
    single { FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyCalculationService", it)
    } }
    single { FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyExportService", it)
    } }
    single { FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyHistoryService", it)
    } }
    single { FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyImportService", it)
    } }
    single { FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyLifecycleService", it)
    } }
    single { FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyReportingService", it)
    } }
    single { FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicySyncService", it)
    } }
    single { FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyValidationService", it)
    } }
    single { FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelPolicyWorkflowService", it)
    } }
    single { FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionApprovalService", it)
    } }
    single { FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionCalculationService", it)
    } }
    single { FuelTransactionExportServiceGrpcKt.FuelTransactionExportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionExportService", it)
    } }
    single { FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionImportService", it)
    } }
    single { FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionLifecycleService", it)
    } }
    single { FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionQueryService", it)
    } }
    single { FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionReportingService", it)
    } }
    single { FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionSyncService", it)
    } }
    single { FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionValidationService", it)
    } }
    single { FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fuel.v1.FuelTransactionWorkflowService", it)
    } }
}
