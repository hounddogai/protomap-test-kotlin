package com.acme.transport.platform.clients

import com.acme.transport.platform.ClientRegistry
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the yards API share the mesh channel. */
val yardsClients = module {
    single { DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockApprovalService", it)
    } }
    single { DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockCalculationService", it)
    } }
    single { DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockLifecycleService", it)
    } }
    single { DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockQueryService", it)
    } }
    single { DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockValidationService", it)
    } }
    single { DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.DockWorkflowService", it)
    } }
    single { YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardApprovalService", it)
    } }
    single { YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardCalculationService", it)
    } }
    single { YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveApprovalService", it)
    } }
    single { YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveCalculationService", it)
    } }
    single { YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveLifecycleService", it)
    } }
    single { YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveReportingService", it)
    } }
    single { YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveValidationService", it)
    } }
    single { YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardMoveWorkflowService", it)
    } }
    single { YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardQueryService", it)
    } }
    single { YardServiceGrpcKt.YardServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardService", it)
    } }
    single { YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardValidationService", it)
    } }
    single { YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.yards.v1.YardWorkflowService", it)
    } }
}
