package com.acme.transport.fleet

import com.acme.transport.fleet.adapters.InspectionLifecycleController
import com.acme.transport.fleet.adapters.VehicleApprovalController
import com.acme.transport.fleet.adapters.VehicleGroupQueryController
import com.acme.transport.fleet.adapters.VehicleQueryController
import com.acme.transport.fleet.adapters.VehicleWorkflowController
import com.acme.transport.fleet.api.InspectionCalculationGrpcService
import com.acme.transport.fleet.api.InspectionWorkflowGrpcService
import com.acme.transport.fleet.api.VehicleGroupCalculationGrpcService
import com.acme.transport.fleet.api.VehicleGroupWorkflowGrpcService
import com.acme.transport.fleet.api.VehicleValidationGrpcService
import com.acme.transport.fleet.grpc.InspectionApprovalEndpoint
import com.acme.transport.fleet.grpc.InspectionValidationEndpoint
import com.acme.transport.fleet.grpc.VehicleEndpoint
import com.acme.transport.fleet.grpc.VehicleGroupApprovalEndpoint
import com.acme.transport.fleet.grpc.VehicleGroupValidationEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the fleet team. */
val fleetModule = module {
    single { FleetRepository(get()) }
    single { InspectionApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { InspectionCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { InspectionLifecycleController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { InspectionValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { InspectionWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleGroupApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleGroupCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleGroupQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleGroupValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleGroupWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { VehicleValidationGrpcService(get(), get(), get(), get(), get(), get()) }
    single { VehicleWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun fleetServices(koin: Koin): List<BindableService> = listOf(
    koin.get<InspectionApprovalEndpoint>(),
    koin.get<InspectionCalculationGrpcService>(),
    koin.get<InspectionLifecycleController>(),
    koin.get<InspectionValidationEndpoint>(),
    koin.get<InspectionWorkflowGrpcService>(),
    koin.get<VehicleApprovalController>(),
    koin.get<VehicleGroupApprovalEndpoint>(),
    koin.get<VehicleGroupCalculationGrpcService>(),
    koin.get<VehicleGroupQueryController>(),
    koin.get<VehicleGroupValidationEndpoint>(),
    koin.get<VehicleGroupWorkflowGrpcService>(),
    koin.get<VehicleQueryController>(),
    koin.get<VehicleEndpoint>(),
    koin.get<VehicleValidationGrpcService>(),
    koin.get<VehicleWorkflowController>(),
)
