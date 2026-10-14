package com.acme.transport.platform.clients

import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.RouteServiceGrpcKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the routing API share the mesh channel. */
val routingClients = module {
    single { RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteApprovalService", it)
    } }
    single { RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteCalculationService", it)
    } }
    single { RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RoutePlanApprovalService", it)
    } }
    single { RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RoutePlanCalculationService", it)
    } }
    single { RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RoutePlanLifecycleService", it)
    } }
    single { RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RoutePlanValidationService", it)
    } }
    single { RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteQueryService", it)
    } }
    single { RouteServiceGrpcKt.RouteServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteService", it)
    } }
    single { RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteValidationService", it)
    } }
    single { RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.RouteWorkflowService", it)
    } }
    single { StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.StopApprovalService", it)
    } }
    single { StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.StopCalculationService", it)
    } }
    single { StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.StopQueryService", it)
    } }
    single { StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.StopValidationService", it)
    } }
    single { StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.routing.v1.StopWorkflowService", it)
    } }
}
