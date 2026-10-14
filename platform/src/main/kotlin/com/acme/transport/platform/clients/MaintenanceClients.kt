package com.acme.transport.platform.clients

import com.acme.maintenance.v1.PartApprovalServiceGrpcKt
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.PartLifecycleServiceGrpcKt
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the maintenance API share the mesh channel. */
val maintenanceClients = module {
    single { PartApprovalServiceGrpcKt.PartApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartApprovalService", it)
    } }
    single { PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartCalculationService", it)
    } }
    single { PartLifecycleServiceGrpcKt.PartLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartLifecycleService", it)
    } }
    single { PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartReportingService", it)
    } }
    single { PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartValidationService", it)
    } }
    single { PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.PartWorkflowService", it)
    } }
    single { ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.ServiceScheduleApprovalService", it)
    } }
    single { ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.ServiceScheduleCalculationService", it)
    } }
    single { ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.ServiceScheduleQueryService", it)
    } }
    single { ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.ServiceScheduleValidationService", it)
    } }
    single { ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.ServiceScheduleWorkflowService", it)
    } }
    single { WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderApprovalService", it)
    } }
    single { WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderCalculationService", it)
    } }
    single { WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderLifecycleService", it)
    } }
    single { WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderQueryService", it)
    } }
    single { WorkOrderServiceGrpcKt.WorkOrderServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderService", it)
    } }
    single { WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderValidationService", it)
    } }
    single { WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.maintenance.v1.WorkOrderWorkflowService", it)
    } }
}
