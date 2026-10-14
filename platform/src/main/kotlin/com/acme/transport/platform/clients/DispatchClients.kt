package com.acme.transport.platform.clients

import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.ZoneApprovalServiceGrpcKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the dispatch API share the mesh channel. */
val dispatchClients = module {
    single { DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchAssignmentApprovalService", it)
    } }
    single { DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchAssignmentCalculationService", it)
    } }
    single { DispatchAssignmentQueryServiceGrpcKt.DispatchAssignmentQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchAssignmentQueryService", it)
    } }
    single { DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchAssignmentValidationService", it)
    } }
    single { DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchAssignmentWorkflowService", it)
    } }
    single { DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchOrderApprovalService", it)
    } }
    single { DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchOrderQueryService", it)
    } }
    single { DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchOrderService", it)
    } }
    single { DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchOrderValidationService", it)
    } }
    single { DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.DispatchOrderWorkflowService", it)
    } }
    single { ZoneApprovalServiceGrpcKt.ZoneApprovalServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.ZoneApprovalService", it)
    } }
    single { ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.ZoneCalculationService", it)
    } }
    single { ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.ZoneValidationService", it)
    } }
    single { ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.dispatch.v1.ZoneWorkflowService", it)
    } }
}
