package com.acme.transport.platform.clients

import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceImportServiceGrpcKt
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingLifecycleServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.TripLogWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the telematics API share the mesh channel. */
val telematicsClients = module {
    single { GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceApprovalService", it)
    } }
    single { GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceCalculationService", it)
    } }
    single { GeoFenceImportServiceGrpcKt.GeoFenceImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceImportService", it)
    } }
    single { GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceLifecycleService", it)
    } }
    single { GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceQueryService", it)
    } }
    single { GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceReportingService", it)
    } }
    single { GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceSyncService", it)
    } }
    single { GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceValidationService", it)
    } }
    single { GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.GeoFenceWorkflowService", it)
    } }
    single { TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingApprovalService", it)
    } }
    single { TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingCalculationService", it)
    } }
    single { TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingImportService", it)
    } }
    single { TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingLifecycleService", it)
    } }
    single { TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingQueryService", it)
    } }
    single { TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingReportingService", it)
    } }
    single { TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingService", it)
    } }
    single { TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingSyncService", it)
    } }
    single { TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingValidationService", it)
    } }
    single { TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TelemetryReadingWorkflowService", it)
    } }
    single { TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogApprovalService", it)
    } }
    single { TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogCalculationService", it)
    } }
    single { TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogExportService", it)
    } }
    single { TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogImportService", it)
    } }
    single { TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogLifecycleService", it)
    } }
    single { TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogReportingService", it)
    } }
    single { TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogSyncService", it)
    } }
    single { TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogValidationService", it)
    } }
    single { TripLogWorkflowServiceGrpcKt.TripLogWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.telematics.v1.TripLogWorkflowService", it)
    } }
}
