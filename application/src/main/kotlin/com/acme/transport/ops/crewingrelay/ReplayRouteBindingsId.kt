package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.DeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.getDeliveryRulesRequest
import com.acme.edgegateway.v1.RouteBindingServiceGrpcKt
import com.acme.edgegateway.v1.listRouteBindingsRequest
import com.acme.fleet.v1.ListVehicleGroupWorkflowsRequestKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.listVehicleGroupWorkflowsRequest
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.SummarizeCoursesRequestKt
import com.acme.learning.v1.summarizeCoursesRequest
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.SuggestRoutesRequestKt
import com.acme.routing.v1.suggestRoutesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.edgegateway.v1.RouteBindingService/ListRouteBindings and hands what it keeps to the sink of its kind. */
suspend fun replayRouteBindingsId(channel: ManagedChannel) {
    val source = RouteBindingServiceGrpcKt.RouteBindingServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.listRouteBindings(listRouteBindingsRequest { this.orderBy = "current-replay-route-bindings-id" }) }
    val rec = ReplayRouteBindingsIdRecord(
        correlationId = current.result.correlationId,
        nextPageToken = normalize(current.pageInfo.nextPageToken),
        status = current.result.status,
        message = current.result.message,
    )
    val registry = mutableMapOf<String, ReplayRouteBindingsIdTarget>()
    for (registration in listOf(
        ReplayRouteBindingsIdRegistration("route-query", ReplayRouteBindingsIdRouteQuery(RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub(channel))),
        ReplayRouteBindingsIdRegistration("vehicle-group-workflow", ReplayRouteBindingsIdVehicleGroupWorkflow(VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub(channel))),
        ReplayRouteBindingsIdRegistration("course-query", ReplayRouteBindingsIdCourseQuery(CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(channel))),
        ReplayRouteBindingsIdRegistration("delivery-validation", ReplayRouteBindingsIdDeliveryValidation(DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["route-query"] ?: return
    sink.forward(rec)
}

/** Where replayRouteBindingsId hands each record. */
interface ReplayRouteBindingsIdTarget {
    suspend fun forward(rec: ReplayRouteBindingsIdRecord)
}

/** What replayRouteBindingsId keeps of each ListRouteBindings response. */
data class ReplayRouteBindingsIdRecord(val correlationId: String, val nextPageToken: String, val status: String, val message: String)

/** Sends records to acme.routing.v1.RouteQueryService. */
class ReplayRouteBindingsIdRouteQuery(private val stub: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub) : ReplayRouteBindingsIdTarget {
    override suspend fun forward(rec: ReplayRouteBindingsIdRecord) {
        val request = suggestRoutesRequest {
            this.filter = SuggestRoutesRequestKt.filter { this.query = rec.correlationId }
        }
        stub.suggestRoutes(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupWorkflowService. */
class ReplayRouteBindingsIdVehicleGroupWorkflow(private val stub: VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub) : ReplayRouteBindingsIdTarget {
    override suspend fun forward(rec: ReplayRouteBindingsIdRecord) {
        val request = listVehicleGroupWorkflowsRequest {
            this.filter = ListVehicleGroupWorkflowsRequestKt.filter { this.region = rec.nextPageToken }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.listVehicleGroupWorkflows(request)
    }
}

/** Hands records to acme.learning.v1.CourseQueryService. */
class ReplayRouteBindingsIdCourseQuery(private val stub: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub) : ReplayRouteBindingsIdTarget {
    override suspend fun forward(rec: ReplayRouteBindingsIdRecord) {
        val request = summarizeCoursesRequest {
            this.details = SummarizeCoursesRequestKt.details { this.format = rec.message }
        }
        stub.summarizeCourses(request)
    }
}

/** Sends records to acme.deliveries.v1.DeliveryValidationService. */
class ReplayRouteBindingsIdDeliveryValidation(private val stub: DeliveryValidationServiceGrpcKt.DeliveryValidationServiceCoroutineStub) : ReplayRouteBindingsIdTarget {
    override suspend fun forward(rec: ReplayRouteBindingsIdRecord) {
        RpcBoundary.run("acme.deliveries.v1.DeliveryValidationService/GetDeliveryRules") { stub.getDeliveryRules(getDeliveryRulesRequest { this.deliveryId = rec.correlationId }) }
    }
}

/** A sink and the kind of record it takes. */
class ReplayRouteBindingsIdRegistration(val kind: String, val sink: ReplayRouteBindingsIdTarget)
