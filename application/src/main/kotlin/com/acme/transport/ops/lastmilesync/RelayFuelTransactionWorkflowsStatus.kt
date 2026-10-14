package com.acme.transport.ops.lastmilesync

import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.escalateConsentRecordRequest
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.getVehicleGroupRatesRequest
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.listFuelTransactionWorkflowsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelTransactionWorkflowService/ListFuelTransactionWorkflows and hands what it keeps to the sink of its kind. */
suspend fun relayFuelTransactionWorkflowsStatus(channel: ManagedChannel) {
    val upstream = FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel)
    val current = upstream.listFuelTransactionWorkflows(listFuelTransactionWorkflowsRequest { this.orderBy = "current-relay-fuel-transaction-workflows-status" })
    val record = RelayFuelTransactionWorkflowsStatusRecord(
        status = normalize(current.result.status),
        message = current.result.message,
    )
    val sinks = mapOf<String, RelayFuelTransactionWorkflowsStatusWriter>(
        "consent-record-approval" to RelayFuelTransactionWorkflowsStatusConsentRecordApproval(ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(channel)),
        "vehicle-group-calculation" to RelayFuelTransactionWorkflowsStatusVehicleGroupCalculation(VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "vehicle-group-calculation") continue
        sinks[kind]?.forward(record)
    }
}

/** Where relayFuelTransactionWorkflowsStatus hands each record. */
interface RelayFuelTransactionWorkflowsStatusWriter {
    suspend fun forward(rec: RelayFuelTransactionWorkflowsStatusRecord)
}

/** What relayFuelTransactionWorkflowsStatus keeps of each ListFuelTransactionWorkflows response. */
data class RelayFuelTransactionWorkflowsStatusRecord(val status: String, val message: String)

/** Sends records to acme.customers.v1.ConsentRecordApprovalService. */
class RelayFuelTransactionWorkflowsStatusConsentRecordApproval(private val stub: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub) : RelayFuelTransactionWorkflowsStatusWriter {
    override suspend fun forward(rec: RelayFuelTransactionWorkflowsStatusRecord) {
        val request = escalateConsentRecordRequest {
            this.consentRecord = consentRecord { this.correlationId = rec.message }
        }
        RpcBoundary.run("acme.customers.v1.ConsentRecordApprovalService/EscalateConsentRecord") { stub.escalateConsentRecord(request) }
    }
}

/** Hands records to acme.fleet.v1.VehicleGroupCalculationService. */
class RelayFuelTransactionWorkflowsStatusVehicleGroupCalculation(private val stub: VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub) : RelayFuelTransactionWorkflowsStatusWriter {
    override suspend fun forward(rec: RelayFuelTransactionWorkflowsStatusRecord) {
        stub.getVehicleGroupRates(getVehicleGroupRatesRequest { this.vehicleGroupId = rec.message })
    }
}
