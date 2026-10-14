package com.acme.transport.ops.lastmilebackfill

import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.summarizeCarePlansRequest
import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.cart.v1.cart
import com.acme.cart.v1.estimateCartRequest
import com.acme.fleet.v1.RetryVehicleGroupWorkflowRequestKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.retryVehicleGroupWorkflowRequest
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.quoteRoutePlanRequest
import com.acme.routing.v1.routePlan
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.careplans.v1.CarePlanQueryService/SummarizeCarePlans and hands what it keeps to the sink of its kind. */
suspend fun propagateCarePlansStatus(channel: ManagedChannel) {
    val source = CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.summarizeCarePlans(summarizeCarePlansRequest { this.carePlanId = "nightly-propagate-care-plans-status" }) }
    val row = PropagateCarePlansStatusRecord(
        status = normalize(reply.result.status),
        downloadUri = reply.downloadUri,
        timezone = reply.period.timezone,
    )
    val sinks = mapOf<String, PropagateCarePlansStatusDestination>(
        "vehicle-group-workflow" to PropagateCarePlansStatusVehicleGroupWorkflow(VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub(channel)),
        "route-plan-calculation" to PropagateCarePlansStatusRoutePlanCalculation(RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(channel)),
        "cart-calculation" to PropagateCarePlansStatusCartCalculation(CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "route-plan-calculation") continue
        sinks[kind]?.transmit(row)
    }
}

/** Where propagateCarePlansStatus hands each record. */
interface PropagateCarePlansStatusDestination {
    suspend fun transmit(rec: PropagateCarePlansStatusRecord)
}

/** What propagateCarePlansStatus keeps of each SummarizeCarePlans response. */
data class PropagateCarePlansStatusRecord(val status: String, val downloadUri: String, val timezone: String)

/** Sends records to acme.fleet.v1.VehicleGroupWorkflowService. */
class PropagateCarePlansStatusVehicleGroupWorkflow(private val stub: VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub) : PropagateCarePlansStatusDestination {
    override suspend fun transmit(rec: PropagateCarePlansStatusRecord) {
        val request = retryVehicleGroupWorkflowRequest {
            this.details = RetryVehicleGroupWorkflowRequestKt.details { this.initiatedBy = rec.timezone }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/RetryVehicleGroupWorkflow") { stub.retryVehicleGroupWorkflow(request) }
    }
}

/** Hands records to acme.routing.v1.RoutePlanCalculationService. */
class PropagateCarePlansStatusRoutePlanCalculation(private val stub: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub) : PropagateCarePlansStatusDestination {
    override suspend fun transmit(rec: PropagateCarePlansStatusRecord) {
        val request = quoteRoutePlanRequest {
            this.routePlan = routePlan { this.number = rec.timezone; this.notes = rec.downloadUri }
        }
        stub.quoteRoutePlan(request)
    }
}

/** Hands records to acme.cart.v1.CartCalculationService. */
class PropagateCarePlansStatusCartCalculation(private val stub: CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub) : PropagateCarePlansStatusDestination {
    override suspend fun transmit(rec: PropagateCarePlansStatusRecord) {
        val request = estimateCartRequest {
            this.cartId = rec.downloadUri
            this.cart = cart { this.notes = rec.status }
        }
        stub.estimateCart(request)
    }
}
