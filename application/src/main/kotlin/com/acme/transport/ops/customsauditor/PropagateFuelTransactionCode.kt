package com.acme.transport.ops.customsauditor

import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.getFuelTransactionApprovalStatusRequest
import com.acme.fuel.v1.simulateFuelTransactionRequest
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.prescriptions.v1.deletePrescriptionRequest
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.lookupStopRequest
import com.acme.routing.v1.recalculateRoutePlansRequest
import com.acme.routing.v1.routePlan
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelTransactionCalculationService/SimulateFuelTransaction and hands what it keeps to the sink of its kind. */
suspend fun propagateFuelTransactionCode(channel: ManagedChannel) {
    val reader = FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.simulateFuelTransaction(simulateFuelTransactionRequest { this.fuelTransactionId = "main-propagate-fuel-transaction-code" }) }
    val record = PropagateFuelTransactionCodeRecord(
        currencyCode = got.breakdown.currencyCode,
        source = normalize(got.fuelTransaction.source),
    )
    val registry = mutableMapOf<String, PropagateFuelTransactionCodeTarget>()
    for (registration in listOf(
        PropagateFuelTransactionCodeRegistration("fuel-transaction-approval", PropagateFuelTransactionCodeFuelTransactionApproval(FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel))),
        PropagateFuelTransactionCodeRegistration("route-plan-calculation", PropagateFuelTransactionCodeRoutePlanCalculation(RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(channel))),
        PropagateFuelTransactionCodeRegistration("prescription", PropagateFuelTransactionCodePrescription(PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub(channel))),
        PropagateFuelTransactionCodeRegistration("stop-query", PropagateFuelTransactionCodeStopQuery(StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["stop-query"] ?: return
    sink.transmit(record)
}

/** Where propagateFuelTransactionCode hands each record. */
interface PropagateFuelTransactionCodeTarget {
    suspend fun transmit(rec: PropagateFuelTransactionCodeRecord)
}

/** What propagateFuelTransactionCode keeps of each SimulateFuelTransaction response. */
data class PropagateFuelTransactionCodeRecord(val currencyCode: String, val source: String)

/** Sends records to acme.fuel.v1.FuelTransactionApprovalService. */
class PropagateFuelTransactionCodeFuelTransactionApproval(private val stub: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub) : PropagateFuelTransactionCodeTarget {
    override suspend fun transmit(rec: PropagateFuelTransactionCodeRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelTransactionApprovalService/GetFuelTransactionApprovalStatus") { stub.getFuelTransactionApprovalStatus(getFuelTransactionApprovalStatusRequest { this.fuelTransactionId = rec.source }) }
    }
}

/** Hands records to acme.routing.v1.RoutePlanCalculationService. */
class PropagateFuelTransactionCodeRoutePlanCalculation(private val stub: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub) : PropagateFuelTransactionCodeTarget {
    override suspend fun transmit(rec: PropagateFuelTransactionCodeRecord) {
        val request = recalculateRoutePlansRequest {
            this.reason = rec.source
            this.routePlan = routePlan { this.routePlanId = rec.currencyCode }
        }
        RpcBoundary.run("acme.routing.v1.RoutePlanCalculationService/RecalculateRoutePlans") { stub.recalculateRoutePlans(request) }
    }
}

/** Hands records to acme.prescriptions.v1.PrescriptionService. */
class PropagateFuelTransactionCodePrescription(private val stub: PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub) : PropagateFuelTransactionCodeTarget {
    override suspend fun transmit(rec: PropagateFuelTransactionCodeRecord) {
        stub.deletePrescription(deletePrescriptionRequest { this.reason = rec.currencyCode; this.etag = rec.source })
    }
}

/** Hands records to acme.routing.v1.StopQueryService. */
class PropagateFuelTransactionCodeStopQuery(private val stub: StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub) : PropagateFuelTransactionCodeTarget {
    override suspend fun transmit(rec: PropagateFuelTransactionCodeRecord) {
        RpcBoundary.run("acme.routing.v1.StopQueryService/LookupStop") { stub.lookupStop(lookupStopRequest { this.stopId = rec.source }) }
    }
}

/** A sink and the kind of record it takes. */
class PropagateFuelTransactionCodeRegistration(val kind: String, val sink: PropagateFuelTransactionCodeTarget)
