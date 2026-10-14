package com.acme.transport.yards

import com.acme.transport.yards.adapters.DockLifecycleController
import com.acme.transport.yards.adapters.DockWorkflowController
import com.acme.transport.yards.adapters.YardMoveApprovalController
import com.acme.transport.yards.adapters.YardMoveReportingController
import com.acme.transport.yards.adapters.YardQueryController
import com.acme.transport.yards.adapters.YardWorkflowController
import com.acme.transport.yards.api.DockCalculationGrpcService
import com.acme.transport.yards.api.DockValidationGrpcService
import com.acme.transport.yards.api.YardCalculationGrpcService
import com.acme.transport.yards.api.YardMoveLifecycleGrpcService
import com.acme.transport.yards.api.YardMoveWorkflowGrpcService
import com.acme.transport.yards.api.YardValidationGrpcService
import com.acme.transport.yards.grpc.DockApprovalEndpoint
import com.acme.transport.yards.grpc.DockQueryEndpoint
import com.acme.transport.yards.grpc.YardApprovalEndpoint
import com.acme.transport.yards.grpc.YardEndpoint
import com.acme.transport.yards.grpc.YardMoveCalculationEndpoint
import com.acme.transport.yards.grpc.YardMoveValidationEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the yards team. */
val yardsModule = module {
    single { YardsRepository(get()) }
    single { DockApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DockCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DockLifecycleController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DockQueryEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DockValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DockWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardMoveWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardQueryController(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { YardWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun yardsServices(koin: Koin): List<BindableService> = listOf(
    koin.get<DockApprovalEndpoint>(),
    koin.get<DockCalculationGrpcService>(),
    koin.get<DockLifecycleController>(),
    koin.get<DockQueryEndpoint>(),
    koin.get<DockValidationGrpcService>(),
    koin.get<DockWorkflowController>(),
    koin.get<YardApprovalEndpoint>(),
    koin.get<YardCalculationGrpcService>(),
    koin.get<YardMoveApprovalController>(),
    koin.get<YardMoveCalculationEndpoint>(),
    koin.get<YardMoveLifecycleGrpcService>(),
    koin.get<YardMoveReportingController>(),
    koin.get<YardMoveValidationEndpoint>(),
    koin.get<YardMoveWorkflowGrpcService>(),
    koin.get<YardQueryController>(),
    koin.get<YardEndpoint>(),
    koin.get<YardValidationGrpcService>(),
    koin.get<YardWorkflowController>(),
)
