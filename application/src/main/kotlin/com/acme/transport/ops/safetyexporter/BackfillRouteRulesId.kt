package com.acme.transport.ops.safetyexporter

import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.quoteFuelPolicyRequest
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.getRouteRulesRequest
import com.acme.shipping.v1.ExplainShipmentCalculationRequestKt
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.explainShipmentCalculationRequest
import com.acme.shipping.v1.shipment
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RouteValidationService/GetRouteRules and hands what it keeps to the sink of its kind. */
suspend fun backfillRouteRulesId(channel: ManagedChannel) {
    val source = RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.getRouteRules(getRouteRulesRequest { this.routeId = "current-backfill-route-rules-id" }) }
    val row = BackfillRouteRulesIdRecord(
        correlationId = reply.result.correlationId,
        status = reply.result.status,
        number = normalize(reply.route.number),
    )
    val sinks = listOf<BackfillRouteRulesIdSink>(
        BackfillRouteRulesIdFuelPolicyCalculation(FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel)),
        BackfillRouteRulesIdShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel)),
    )
    sinks[0].forward(row)
}

/** Where backfillRouteRulesId hands each record. */
interface BackfillRouteRulesIdSink {
    suspend fun forward(rec: BackfillRouteRulesIdRecord)
}

/** What backfillRouteRulesId keeps of each GetRouteRules response. */
data class BackfillRouteRulesIdRecord(val correlationId: String, val status: String, val number: String)

/** Passes records to acme.fuel.v1.FuelPolicyCalculationService. */
class BackfillRouteRulesIdFuelPolicyCalculation(private val stub: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub) : BackfillRouteRulesIdSink {
    override suspend fun forward(rec: BackfillRouteRulesIdRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelPolicyCalculationService/QuoteFuelPolicy") { stub.quoteFuelPolicy(quoteFuelPolicyRequest { this.fuelPolicy = fuelPolicy { this.name = rec.correlationId } }) }
    }
}

/** Passes records to acme.shipping.v1.ShipmentCalculationService. */
class BackfillRouteRulesIdShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : BackfillRouteRulesIdSink {
    override suspend fun forward(rec: BackfillRouteRulesIdRecord) {
        val request = explainShipmentCalculationRequest {
            this.shipment = shipment { this.notes = rec.correlationId; this.number = rec.number }
            this.details = ExplainShipmentCalculationRequestKt.details { this.locale = rec.status }
        }
        stub.explainShipmentCalculation(request)
    }
}
