package com.acme.transport.platform.clients

import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.ConsentRecordCalculationServiceGrpcKt
import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.ConsentRecordWorkflowServiceGrpcKt
import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerLifecycleServiceGrpcKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentImportServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentReportingServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.HouseholdWorkflowServiceGrpcKt
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.PreferenceImportServiceGrpcKt
import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the customers API share the mesh channel. */
val customersClients = module {
    single { ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordApprovalService", it)
    } }
    single { ConsentRecordCalculationServiceGrpcKt.ConsentRecordCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordCalculationService", it)
    } }
    single { ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordReportingService", it)
    } }
    single { ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordSyncService", it)
    } }
    single { ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordValidationService", it)
    } }
    single { ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.ConsentRecordWorkflowService", it)
    } }
    single { CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerApprovalService", it)
    } }
    single { CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerCalculationService", it)
    } }
    single { CustomerLifecycleServiceGrpcKt.CustomerLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerLifecycleService", it)
    } }
    single { CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerQueryService", it)
    } }
    single { CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentApprovalService", it)
    } }
    single { CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentCalculationService", it)
    } }
    single { CustomerSegmentImportServiceGrpcKt.CustomerSegmentImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentImportService", it)
    } }
    single { CustomerSegmentReportingServiceGrpcKt.CustomerSegmentReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentReportingService", it)
    } }
    single { CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentSyncService", it)
    } }
    single { CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerSegmentValidationService", it)
    } }
    single { CustomerServiceGrpcKt.CustomerServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerService", it)
    } }
    single { CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerValidationService", it)
    } }
    single { CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.CustomerWorkflowService", it)
    } }
    single { HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdApprovalService", it)
    } }
    single { HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdCalculationService", it)
    } }
    single { HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdQueryService", it)
    } }
    single { HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdReportingService", it)
    } }
    single { HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdValidationService", it)
    } }
    single { HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.HouseholdWorkflowService", it)
    } }
    single { PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.PreferenceCalculationService", it)
    } }
    single { PreferenceImportServiceGrpcKt.PreferenceImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.PreferenceImportService", it)
    } }
    single { PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.PreferenceReportingService", it)
    } }
    single { PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.PreferenceSyncService", it)
    } }
    single { PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.customers.v1.PreferenceValidationService", it)
    } }
}
