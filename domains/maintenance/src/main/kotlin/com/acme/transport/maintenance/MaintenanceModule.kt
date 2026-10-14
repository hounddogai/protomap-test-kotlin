package com.acme.transport.maintenance

import com.acme.transport.maintenance.adapters.PartLifecycleController
import com.acme.transport.maintenance.adapters.PartWorkflowController
import com.acme.transport.maintenance.adapters.ServiceScheduleLifecycleController
import com.acme.transport.maintenance.adapters.ServiceScheduleWorkflowController
import com.acme.transport.maintenance.adapters.WorkOrderLifecycleController
import com.acme.transport.maintenance.adapters.WorkOrderValidationController
import com.acme.transport.maintenance.api.PartCalculationGrpcService
import com.acme.transport.maintenance.api.PartValidationGrpcService
import com.acme.transport.maintenance.api.ServiceScheduleCalculationGrpcService
import com.acme.transport.maintenance.api.ServiceScheduleValidationGrpcService
import com.acme.transport.maintenance.api.WorkOrderCalculationGrpcService
import com.acme.transport.maintenance.api.WorkOrderGrpcService
import com.acme.transport.maintenance.grpc.PartApprovalEndpoint
import com.acme.transport.maintenance.grpc.PartReportingEndpoint
import com.acme.transport.maintenance.grpc.ServiceScheduleApprovalEndpoint
import com.acme.transport.maintenance.grpc.ServiceScheduleQueryEndpoint
import com.acme.transport.maintenance.grpc.WorkOrderApprovalEndpoint
import com.acme.transport.maintenance.grpc.WorkOrderQueryEndpoint
import com.acme.transport.maintenance.grpc.WorkOrderWorkflowEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the maintenance team. */
val maintenanceModule = module {
    single { MaintenanceRepository(get()) }
    single { PartApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PartCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PartLifecycleController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PartReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PartValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PartWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleLifecycleController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleQueryEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ServiceScheduleWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderLifecycleController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderQueryEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WorkOrderWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun maintenanceServices(koin: Koin): List<BindableService> = listOf(
    koin.get<PartApprovalEndpoint>(),
    koin.get<PartCalculationGrpcService>(),
    koin.get<PartLifecycleController>(),
    koin.get<PartReportingEndpoint>(),
    koin.get<PartValidationGrpcService>(),
    koin.get<PartWorkflowController>(),
    koin.get<ServiceScheduleApprovalEndpoint>(),
    koin.get<ServiceScheduleCalculationGrpcService>(),
    koin.get<ServiceScheduleLifecycleController>(),
    koin.get<ServiceScheduleQueryEndpoint>(),
    koin.get<ServiceScheduleValidationGrpcService>(),
    koin.get<ServiceScheduleWorkflowController>(),
    koin.get<WorkOrderApprovalEndpoint>(),
    koin.get<WorkOrderCalculationGrpcService>(),
    koin.get<WorkOrderLifecycleController>(),
    koin.get<WorkOrderQueryEndpoint>(),
    koin.get<WorkOrderGrpcService>(),
    koin.get<WorkOrderValidationController>(),
    koin.get<WorkOrderWorkflowEndpoint>(),
)
