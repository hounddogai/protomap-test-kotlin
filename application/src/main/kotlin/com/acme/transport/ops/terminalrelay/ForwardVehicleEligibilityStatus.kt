package com.acme.transport.ops.terminalrelay

import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.checkVehicleEligibilityRequest
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.ValidateFuelCardImportRequestKt
import com.acme.fuel.v1.validateFuelCardImportRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.SummarizeYardsRequestKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.summarizeYardsRequest
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleValidationService/CheckVehicleEligibility and hands what it keeps to the sink of its kind. */
suspend fun forwardVehicleEligibilityStatus(channel: ManagedChannel) {
    val reader = VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub(channel)
    val reply = reader.checkVehicleEligibility(checkVehicleEligibilityRequest { this.vehicleId = "default-forward-vehicle-eligibility-status" })
    val record = ForwardVehicleEligibilityStatusRecord(
        status = reply.result.status,
        outcomeStatus = reply.outcome.status,
        name = reply.vehicle.name,
        message = reply.outcome.message,
    )
    val sinks = mapOf<String, ForwardVehicleEligibilityStatusDestination>(
        "yard-query" to ForwardVehicleEligibilityStatusYardQuery(YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(channel)),
        "fuel-card-import" to ForwardVehicleEligibilityStatusFuelCardImport(FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "fuel-card-import") continue
        sinks[kind]?.deliver(record)
    }
}

/** Where forwardVehicleEligibilityStatus hands each record. */
interface ForwardVehicleEligibilityStatusDestination {
    suspend fun deliver(rec: ForwardVehicleEligibilityStatusRecord)
}

/** What forwardVehicleEligibilityStatus keeps of each CheckVehicleEligibility response. */
data class ForwardVehicleEligibilityStatusRecord(val status: String, val outcomeStatus: String, val name: String, val message: String)

/** Passes records to acme.yards.v1.YardQueryService. */
class ForwardVehicleEligibilityStatusYardQuery(private val stub: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub) : ForwardVehicleEligibilityStatusDestination {
    override suspend fun deliver(rec: ForwardVehicleEligibilityStatusRecord) {
        val request = summarizeYardsRequest {
            this.reason = rec.name
            this.details = SummarizeYardsRequestKt.details { this.cron = rec.outcomeStatus }
        }
        RpcBoundary.run("acme.yards.v1.YardQueryService/SummarizeYards") { stub.summarizeYards(request) }
    }
}

/** Sends records to acme.fuel.v1.FuelCardImportService. */
class ForwardVehicleEligibilityStatusFuelCardImport(private val stub: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub) : ForwardVehicleEligibilityStatusDestination {
    override suspend fun deliver(rec: ForwardVehicleEligibilityStatusRecord) {
        val request = validateFuelCardImportRequest {
            this.reason = rec.outcomeStatus
            this.details = ValidateFuelCardImportRequestKt.details { this.locale = rec.message }
        }
        stub.validateFuelCardImport(request)
    }
}
