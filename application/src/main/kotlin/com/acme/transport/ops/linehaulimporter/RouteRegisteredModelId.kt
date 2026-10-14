package com.acme.transport.ops.linehaulimporter

import com.acme.driverapp.v1.AdvanceDriverHomeWorkflowRequestKt
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.advanceDriverHomeWorkflowRequest
import com.acme.driverapp.v1.driverHome
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.SummarizeFuelCardsRequestKt
import com.acme.fuel.v1.summarizeFuelCardsRequest
import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.modelregistry.v1.lookupRegisteredModelRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.modelregistry.v1.RegisteredModelQueryService/LookupRegisteredModel and hands what it keeps to the sink of its kind. */
suspend fun routeRegisteredModelId(channel: ManagedChannel) {
    val origin = RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub(channel)
    val found = origin.lookupRegisteredModel(lookupRegisteredModelRequest { this.registeredModelId = "current-route-registered-model-id" })
    val rec = RouteRegisteredModelIdRecord(
        correlationId = normalize(found.result.correlationId),
        name = found.registeredModel.name,
        code = found.registeredModel.code,
        status = normalize(found.result.status),
    )
    val sinks = mapOf<String, RouteRegisteredModelIdDestination>(
        "driver-home-workflow" to RouteRegisteredModelIdDriverHomeWorkflow(DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(channel)),
        "fuel-card-query" to RouteRegisteredModelIdFuelCardQuery(FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub(channel)),
    )
    routeRouteRegisteredModelId(sinks, "fuel-card-query", rec)
}

/** Where routeRegisteredModelId hands each record. */
interface RouteRegisteredModelIdDestination {
    suspend fun transmit(rec: RouteRegisteredModelIdRecord)
}

/** What routeRegisteredModelId keeps of each LookupRegisteredModel response. */
data class RouteRegisteredModelIdRecord(val correlationId: String, val name: String, val code: String, val status: String)

/** Writes records to acme.driverapp.v1.DriverHomeWorkflowService. */
class RouteRegisteredModelIdDriverHomeWorkflow(private val stub: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub) : RouteRegisteredModelIdDestination {
    override suspend fun transmit(rec: RouteRegisteredModelIdRecord) {
        val request = advanceDriverHomeWorkflowRequest {
            this.driverHome = driverHome { this.subject = rec.name }
            this.reason = rec.correlationId
            this.details = AdvanceDriverHomeWorkflowRequestKt.details { this.initiatedBy = rec.status }
        }
        stub.advanceDriverHomeWorkflow(request)
    }
}

/** Passes records to acme.fuel.v1.FuelCardQueryService. */
class RouteRegisteredModelIdFuelCardQuery(private val stub: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub) : RouteRegisteredModelIdDestination {
    override suspend fun transmit(rec: RouteRegisteredModelIdRecord) {
        val request = summarizeFuelCardsRequest {
            this.details = SummarizeFuelCardsRequestKt.details { this.format = rec.correlationId }
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardQueryService/SummarizeFuelCards") { stub.summarizeFuelCards(request) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeRouteRegisteredModelId(sinks: Map<String, RouteRegisteredModelIdDestination>, kind: String, rec: RouteRegisteredModelIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
