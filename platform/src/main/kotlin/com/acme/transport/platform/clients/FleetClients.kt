package com.acme.transport.platform.clients

import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the fleet API share the mesh channel. */
val fleetClients = module {
    single { InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.InspectionApprovalService", it)
    } }
    single { InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.InspectionCalculationService", it)
    } }
    single { InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.InspectionLifecycleService", it)
    } }
    single { InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.InspectionValidationService", it)
    } }
    single { VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleApprovalService", it)
    } }
    single { VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleGroupApprovalService", it)
    } }
    single { VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleGroupCalculationService", it)
    } }
    single { VehicleGroupQueryServiceGrpcKt.VehicleGroupQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleGroupQueryService", it)
    } }
    single { VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleGroupValidationService", it)
    } }
    single { VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleGroupWorkflowService", it)
    } }
    single { VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleQueryService", it)
    } }
    single { VehicleServiceGrpcKt.VehicleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleService", it)
    } }
    single { VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleValidationService", it)
    } }
    single { VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.fleet.v1.VehicleWorkflowService", it)
    } }
}
