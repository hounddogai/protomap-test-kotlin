package com.acme.transport.ops.freightpaysweeper

import com.acme.devices.v1.CountManagedDevicesRequestKt
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.countManagedDevicesRequest
import com.acme.fleet.v1.CancelVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.cancelVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.fuel.v1.FuelCardValidationServiceGrpcKt
import com.acme.fuel.v1.NormalizeFuelCardRequestKt
import com.acme.fuel.v1.normalizeFuelCardRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.deleteTelemetryReadingRequest
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingService/DeleteTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun settleTelemetryReadingMessage(channel: ManagedChannel) {
    val upstream = TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)
    val found = upstream.deleteTelemetryReading(deleteTelemetryReadingRequest { this.telemetryReadingId = "main-settle-telemetry-reading-message" })
    val record = SettleTelemetryReadingMessageRecord(
        message = found.result.message,
        correlationId = found.result.correlationId,
        status = found.result.status,
    )
    actOnSettleTelemetryReadingMessage("managed-device-query", record, channel)
}

/** Where settleTelemetryReadingMessage hands each record. */
interface SettleTelemetryReadingMessageDestination {
    suspend fun forward(rec: SettleTelemetryReadingMessageRecord)
}

/** What settleTelemetryReadingMessage keeps of each DeleteTelemetryReading response. */
data class SettleTelemetryReadingMessageRecord(val message: String, val correlationId: String, val status: String)

/** Writes records to acme.devices.v1.ManagedDeviceQueryService. */
class SettleTelemetryReadingMessageManagedDeviceQuery(private val stub: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub) : SettleTelemetryReadingMessageDestination {
    override suspend fun forward(rec: SettleTelemetryReadingMessageRecord) {
        val request = countManagedDevicesRequest {
            this.orderBy = rec.correlationId
            this.filter = CountManagedDevicesRequestKt.filter { this.region = rec.status; this.query = rec.message }
        }
        stub.countManagedDevices(request)
    }
}

/** Passes records to acme.fleet.v1.VehicleWorkflowService. */
class SettleTelemetryReadingMessageVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : SettleTelemetryReadingMessageDestination {
    override suspend fun forward(rec: SettleTelemetryReadingMessageRecord) {
        val request = cancelVehicleWorkflowRequest {
            this.vehicle = vehicle { this.code = rec.correlationId }
            this.details = CancelVehicleWorkflowRequestKt.details { this.step = rec.message }
            this.reason = rec.status
        }
        stub.cancelVehicleWorkflow(request)
    }
}

/** Writes records to acme.fuel.v1.FuelCardValidationService. */
class SettleTelemetryReadingMessageFuelCardValidation(private val stub: FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub) : SettleTelemetryReadingMessageDestination {
    override suspend fun forward(rec: SettleTelemetryReadingMessageRecord) {
        val request = normalizeFuelCardRequest {
            this.details = NormalizeFuelCardRequestKt.details { this.locale = rec.correlationId }
            this.reason = rec.status
        }
        stub.normalizeFuelCard(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSettleTelemetryReadingMessage(kind: String, rec: SettleTelemetryReadingMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "managed-device-query" to { SettleTelemetryReadingMessageManagedDeviceQuery(ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(channel)).forward(rec) },
        "vehicle-workflow" to { SettleTelemetryReadingMessageVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)).forward(rec) },
        "fuel-card-validation" to { SettleTelemetryReadingMessageFuelCardValidation(FuelCardValidationServiceGrpcKt.FuelCardValidationServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
