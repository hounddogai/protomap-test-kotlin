package com.acme.transport.drivers

import com.acme.transport.drivers.adapters.DriverController
import com.acme.transport.drivers.adapters.DriverLicenseApprovalController
import com.acme.transport.drivers.adapters.DriverLicenseQueryController
import com.acme.transport.drivers.adapters.DriverLicenseWorkflowController
import com.acme.transport.drivers.adapters.DutyStatusApprovalController
import com.acme.transport.drivers.adapters.DutyStatusReportingController
import com.acme.transport.drivers.api.DriverCalculationGrpcService
import com.acme.transport.drivers.api.DriverLicenseLifecycleGrpcService
import com.acme.transport.drivers.api.DriverLicenseValidationGrpcService
import com.acme.transport.drivers.api.DriverQueryGrpcService
import com.acme.transport.drivers.api.DriverWorkflowGrpcService
import com.acme.transport.drivers.api.DutyStatusLifecycleGrpcService
import com.acme.transport.drivers.api.DutyStatusWorkflowGrpcService
import com.acme.transport.drivers.grpc.DriverApprovalEndpoint
import com.acme.transport.drivers.grpc.DriverLicenseCalculationEndpoint
import com.acme.transport.drivers.grpc.DriverLicenseReportingEndpoint
import com.acme.transport.drivers.grpc.DriverLifecycleEndpoint
import com.acme.transport.drivers.grpc.DriverValidationEndpoint
import com.acme.transport.drivers.grpc.DutyStatusCalculationEndpoint
import com.acme.transport.drivers.grpc.DutyStatusValidationEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the drivers team. */
val driversModule = module {
    single { DriversRepository(get()) }
    single { DriverApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLicenseWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverLifecycleEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DutyStatusWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun driversServices(koin: Koin): List<BindableService> = listOf(
    koin.get<DriverApprovalEndpoint>(),
    koin.get<DriverCalculationGrpcService>(),
    koin.get<DriverLicenseApprovalController>(),
    koin.get<DriverLicenseCalculationEndpoint>(),
    koin.get<DriverLicenseLifecycleGrpcService>(),
    koin.get<DriverLicenseQueryController>(),
    koin.get<DriverLicenseReportingEndpoint>(),
    koin.get<DriverLicenseValidationGrpcService>(),
    koin.get<DriverLicenseWorkflowController>(),
    koin.get<DriverLifecycleEndpoint>(),
    koin.get<DriverQueryGrpcService>(),
    koin.get<DriverController>(),
    koin.get<DriverValidationEndpoint>(),
    koin.get<DriverWorkflowGrpcService>(),
    koin.get<DutyStatusApprovalController>(),
    koin.get<DutyStatusCalculationEndpoint>(),
    koin.get<DutyStatusLifecycleGrpcService>(),
    koin.get<DutyStatusReportingController>(),
    koin.get<DutyStatusValidationEndpoint>(),
    koin.get<DutyStatusWorkflowGrpcService>(),
)
