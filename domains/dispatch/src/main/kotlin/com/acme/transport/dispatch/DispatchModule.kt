package com.acme.transport.dispatch

import com.acme.transport.dispatch.adapters.DispatchAssignmentQueryController
import com.acme.transport.dispatch.adapters.DispatchOrderApprovalController
import com.acme.transport.dispatch.adapters.DispatchOrderValidationController
import com.acme.transport.dispatch.adapters.ZoneCalculationController
import com.acme.transport.dispatch.api.DispatchAssignmentCalculationGrpcService
import com.acme.transport.dispatch.api.DispatchAssignmentWorkflowGrpcService
import com.acme.transport.dispatch.api.DispatchOrderGrpcService
import com.acme.transport.dispatch.api.ZoneApprovalGrpcService
import com.acme.transport.dispatch.api.ZoneWorkflowGrpcService
import com.acme.transport.dispatch.grpc.DispatchAssignmentApprovalEndpoint
import com.acme.transport.dispatch.grpc.DispatchAssignmentValidationEndpoint
import com.acme.transport.dispatch.grpc.DispatchOrderQueryEndpoint
import com.acme.transport.dispatch.grpc.DispatchOrderWorkflowEndpoint
import com.acme.transport.dispatch.grpc.ZoneValidationEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the dispatch team. */
val dispatchModule = module {
    single { DispatchRepository(get()) }
    single { DispatchAssignmentApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchAssignmentCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchAssignmentQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchAssignmentValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchAssignmentWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchOrderApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchOrderQueryEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchOrderGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchOrderValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DispatchOrderWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ZoneApprovalGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ZoneCalculationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ZoneValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ZoneWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun dispatchServices(koin: Koin): List<BindableService> = listOf(
    koin.get<DispatchAssignmentApprovalEndpoint>(),
    koin.get<DispatchAssignmentCalculationGrpcService>(),
    koin.get<DispatchAssignmentQueryController>(),
    koin.get<DispatchAssignmentValidationEndpoint>(),
    koin.get<DispatchAssignmentWorkflowGrpcService>(),
    koin.get<DispatchOrderApprovalController>(),
    koin.get<DispatchOrderQueryEndpoint>(),
    koin.get<DispatchOrderGrpcService>(),
    koin.get<DispatchOrderValidationController>(),
    koin.get<DispatchOrderWorkflowEndpoint>(),
    koin.get<ZoneApprovalGrpcService>(),
    koin.get<ZoneCalculationController>(),
    koin.get<ZoneValidationEndpoint>(),
    koin.get<ZoneWorkflowGrpcService>(),
)
