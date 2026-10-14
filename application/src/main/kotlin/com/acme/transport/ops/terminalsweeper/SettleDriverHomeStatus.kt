package com.acme.transport.ops.terminalsweeper

import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.escalateDriverHomeRequest
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.getVehicleGroupRatesRequest
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.SignalRouteWorkflowRequestKt
import com.acme.routing.v1.signalRouteWorkflowRequest
import com.acme.shows.v1.QuoteShowRequestKt
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.quoteShowRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeApprovalService/EscalateDriverHome and hands what it keeps to the sink of its kind. */
suspend fun settleDriverHomeStatus(channel: ManagedChannel) {
    val upstream = DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(channel)
    val found = upstream.escalateDriverHome(escalateDriverHomeRequest { this.reason = "nightly-settle-driver-home-status" })
    val rec = SettleDriverHomeStatusRecord(
        status = normalize(found.outcome.status),
        referenceId = normalize(found.outcome.referenceId),
        subject = found.driverHome.subject,
        correlationId = found.result.correlationId,
    )
    actOnSettleDriverHomeStatus("vehicle-group-calculation", rec, channel)
}

/** Where settleDriverHomeStatus hands each record. */
interface SettleDriverHomeStatusDestination {
    suspend fun transmit(rec: SettleDriverHomeStatusRecord)
}

/** What settleDriverHomeStatus keeps of each EscalateDriverHome response. */
data class SettleDriverHomeStatusRecord(val status: String, val referenceId: String, val subject: String, val correlationId: String)

/** Writes records to acme.fleet.v1.VehicleGroupCalculationService. */
class SettleDriverHomeStatusVehicleGroupCalculation(private val stub: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub) : SettleDriverHomeStatusDestination {
    override suspend fun transmit(rec: SettleDriverHomeStatusRecord) {
        stub.getVehicleGroupRates(getVehicleGroupRatesRequest { this.vehicleGroupId = rec.subject })
    }
}

/** Passes records to acme.shows.v1.ShowCalculationService. */
class SettleDriverHomeStatusShowCalculation(private val stub: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub) : SettleDriverHomeStatusDestination {
    override suspend fun transmit(rec: SettleDriverHomeStatusRecord) {
        val request = quoteShowRequest {
            this.details = QuoteShowRequestKt.details { this.currencyCode = rec.correlationId }
        }
        stub.quoteShow(request)
    }
}

/** Hands records to acme.routing.v1.RouteWorkflowService. */
class SettleDriverHomeStatusRouteWorkflow(private val stub: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub) : SettleDriverHomeStatusDestination {
    override suspend fun transmit(rec: SettleDriverHomeStatusRecord) {
        val request = signalRouteWorkflowRequest {
            this.details = SignalRouteWorkflowRequestKt.details { this.step = rec.correlationId }
            this.reason = rec.referenceId
        }
        stub.signalRouteWorkflow(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSettleDriverHomeStatus(kind: String, rec: SettleDriverHomeStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "vehicle-group-calculation" to { SettleDriverHomeStatusVehicleGroupCalculation(VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(channel)).transmit(rec) },
        "show-calculation" to { SettleDriverHomeStatusShowCalculation(ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel)).transmit(rec) },
        "route-workflow" to { SettleDriverHomeStatusRouteWorkflow(RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
