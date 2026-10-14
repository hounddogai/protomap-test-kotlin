package com.acme.transport.routing

import com.acme.transport.routing.adapters.RouteController
import com.acme.transport.routing.adapters.RoutePlanApprovalController
import com.acme.transport.routing.adapters.RoutePlanValidationController
import com.acme.transport.routing.adapters.StopApprovalController
import com.acme.transport.routing.adapters.StopValidationController
import com.acme.transport.routing.api.RouteCalculationGrpcService
import com.acme.transport.routing.api.RoutePlanLifecycleGrpcService
import com.acme.transport.routing.api.RouteQueryGrpcService
import com.acme.transport.routing.api.RouteWorkflowGrpcService
import com.acme.transport.routing.api.StopQueryGrpcService
import com.acme.transport.routing.grpc.RouteApprovalEndpoint
import com.acme.transport.routing.grpc.RoutePlanCalculationEndpoint
import com.acme.transport.routing.grpc.RoutePlanWorkflowEndpoint
import com.acme.transport.routing.grpc.RouteValidationEndpoint
import com.acme.transport.routing.grpc.StopCalculationEndpoint
import com.acme.transport.routing.grpc.StopWorkflowEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the routing team. */
val routingModule = module {
    single { RoutingRepository(get()) }
    single { RouteApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RouteCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RoutePlanApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RoutePlanCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RoutePlanLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RoutePlanValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RoutePlanWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RouteQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RouteController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RouteValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { RouteWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StopApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StopCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StopQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StopValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StopWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun routingServices(koin: Koin): List<BindableService> = listOf(
    koin.get<RouteApprovalEndpoint>(),
    koin.get<RouteCalculationGrpcService>(),
    koin.get<RoutePlanApprovalController>(),
    koin.get<RoutePlanCalculationEndpoint>(),
    koin.get<RoutePlanLifecycleGrpcService>(),
    koin.get<RoutePlanValidationController>(),
    koin.get<RoutePlanWorkflowEndpoint>(),
    koin.get<RouteQueryGrpcService>(),
    koin.get<RouteController>(),
    koin.get<RouteValidationEndpoint>(),
    koin.get<RouteWorkflowGrpcService>(),
    koin.get<StopApprovalController>(),
    koin.get<StopCalculationEndpoint>(),
    koin.get<StopQueryGrpcService>(),
    koin.get<StopValidationController>(),
    koin.get<StopWorkflowEndpoint>(),
)
