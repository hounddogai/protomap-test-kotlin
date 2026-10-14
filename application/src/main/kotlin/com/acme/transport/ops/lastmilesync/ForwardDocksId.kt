package com.acme.transport.ops.lastmilesync

import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.ExplainDispatchAssignmentDecisionRequestKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.explainDispatchAssignmentDecisionRequest
import com.acme.edgegateway.v1.ListRouteBindingsRequestKt
import com.acme.edgegateway.v1.RouteBindingServiceGrpcKt
import com.acme.edgegateway.v1.listRouteBindingsRequest
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.lookupServiceScheduleRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.findDocksRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockQueryService/FindDocks and hands what it keeps to the sink of its kind. */
suspend fun forwardDocksId(channel: ManagedChannel) {
    val source = DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.findDocks(findDocksRequest { this.orderBy = "default-forward-docks-id" }) }
    val row = ForwardDocksIdRecord(
        correlationId = normalize(got.result.correlationId),
        status = normalize(got.result.status),
        message = got.result.message,
    )
    actOnForwardDocksId("schedule-query", row, channel)
}

/** Where forwardDocksId hands each record. */
interface ForwardDocksIdDestination {
    suspend fun relay(rec: ForwardDocksIdRecord)
}

/** What forwardDocksId keeps of each FindDocks response. */
data class ForwardDocksIdRecord(val correlationId: String, val status: String, val message: String)

/** Passes records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class ForwardDocksIdDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : ForwardDocksIdDestination {
    override suspend fun relay(rec: ForwardDocksIdRecord) {
        val request = explainDispatchAssignmentDecisionRequest {
            this.dispatchAssignmentId = rec.correlationId
            this.dispatchAssignment = dispatchAssignment { this.subject = rec.status }
            this.details = ExplainDispatchAssignmentDecisionRequestKt.details { this.locale = rec.message }
        }
        stub.explainDispatchAssignmentDecision(request)
    }
}

/** Hands records to acme.maintenance.v1.ServiceScheduleQueryService. */
class ForwardDocksIdServiceScheduleQuery(private val stub: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub) : ForwardDocksIdDestination {
    override suspend fun relay(rec: ForwardDocksIdRecord) {
        stub.lookupServiceSchedule(lookupServiceScheduleRequest { this.serviceScheduleId = rec.correlationId })
    }
}

/** Sends records to acme.edgegateway.v1.RouteBindingService. */
class ForwardDocksIdRouteBinding(private val stub: RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub) : ForwardDocksIdDestination {
    override suspend fun relay(rec: ForwardDocksIdRecord) {
        val request = listRouteBindingsRequest {
            this.filter = ListRouteBindingsRequestKt.filter { this.query = rec.message; this.ownerId = rec.correlationId }
        }
        RpcBoundary.run("acme.edgegateway.v1.RouteBindingService/ListRouteBindings") { stub.listRouteBindings(request) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnForwardDocksId(kind: String, rec: ForwardDocksIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "dispatch-assignment-validation" to { ForwardDocksIdDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel)).relay(rec) },
        "schedule-query" to { ForwardDocksIdServiceScheduleQuery(ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub(channel)).relay(rec) },
        "route-binding" to { ForwardDocksIdRouteBinding(RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
