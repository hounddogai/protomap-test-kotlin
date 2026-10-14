package com.acme.transport.telematics

import com.acme.transport.telematics.adapters.GeoFenceImportController
import com.acme.transport.telematics.adapters.GeoFenceReportingController
import com.acme.transport.telematics.adapters.GeoFenceWorkflowController
import com.acme.transport.telematics.adapters.TelemetryReadingImportController
import com.acme.transport.telematics.adapters.TelemetryReadingReportingController
import com.acme.transport.telematics.adapters.TelemetryReadingValidationController
import com.acme.transport.telematics.adapters.TripLogCalculationController
import com.acme.transport.telematics.adapters.TripLogLifecycleController
import com.acme.transport.telematics.adapters.TripLogValidationController
import com.acme.transport.telematics.api.GeoFenceCalculationGrpcService
import com.acme.transport.telematics.api.GeoFenceQueryGrpcService
import com.acme.transport.telematics.api.GeoFenceValidationGrpcService
import com.acme.transport.telematics.api.TelemetryReadingCalculationGrpcService
import com.acme.transport.telematics.api.TelemetryReadingQueryGrpcService
import com.acme.transport.telematics.api.TelemetryReadingSyncGrpcService
import com.acme.transport.telematics.api.TripLogApprovalGrpcService
import com.acme.transport.telematics.api.TripLogImportGrpcService
import com.acme.transport.telematics.api.TripLogSyncGrpcService
import com.acme.transport.telematics.grpc.GeoFenceApprovalEndpoint
import com.acme.transport.telematics.grpc.GeoFenceLifecycleEndpoint
import com.acme.transport.telematics.grpc.GeoFenceSyncEndpoint
import com.acme.transport.telematics.grpc.TelemetryReadingApprovalEndpoint
import com.acme.transport.telematics.grpc.TelemetryReadingEndpoint
import com.acme.transport.telematics.grpc.TelemetryReadingLifecycleEndpoint
import com.acme.transport.telematics.grpc.TelemetryReadingWorkflowEndpoint
import com.acme.transport.telematics.grpc.TripLogExportEndpoint
import com.acme.transport.telematics.grpc.TripLogReportingEndpoint
import com.acme.transport.telematics.grpc.TripLogWorkflowEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the telematics team. */
val telematicsModule = module {
    single { TelematicsRepository(get()) }
    single { GeoFenceApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceImportController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceLifecycleEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceSyncEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GeoFenceWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingImportController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingLifecycleEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingSyncGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TelemetryReadingWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogApprovalGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogCalculationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogExportEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogImportGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogLifecycleController(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogSyncGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { TripLogWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun telematicsServices(koin: Koin): List<BindableService> = listOf(
    koin.get<GeoFenceApprovalEndpoint>(),
    koin.get<GeoFenceCalculationGrpcService>(),
    koin.get<GeoFenceImportController>(),
    koin.get<GeoFenceLifecycleEndpoint>(),
    koin.get<GeoFenceQueryGrpcService>(),
    koin.get<GeoFenceReportingController>(),
    koin.get<GeoFenceSyncEndpoint>(),
    koin.get<GeoFenceValidationGrpcService>(),
    koin.get<GeoFenceWorkflowController>(),
    koin.get<TelemetryReadingApprovalEndpoint>(),
    koin.get<TelemetryReadingCalculationGrpcService>(),
    koin.get<TelemetryReadingImportController>(),
    koin.get<TelemetryReadingLifecycleEndpoint>(),
    koin.get<TelemetryReadingQueryGrpcService>(),
    koin.get<TelemetryReadingReportingController>(),
    koin.get<TelemetryReadingEndpoint>(),
    koin.get<TelemetryReadingSyncGrpcService>(),
    koin.get<TelemetryReadingValidationController>(),
    koin.get<TelemetryReadingWorkflowEndpoint>(),
    koin.get<TripLogApprovalGrpcService>(),
    koin.get<TripLogCalculationController>(),
    koin.get<TripLogExportEndpoint>(),
    koin.get<TripLogImportGrpcService>(),
    koin.get<TripLogLifecycleController>(),
    koin.get<TripLogReportingEndpoint>(),
    koin.get<TripLogSyncGrpcService>(),
    koin.get<TripLogValidationController>(),
    koin.get<TripLogWorkflowEndpoint>(),
)
