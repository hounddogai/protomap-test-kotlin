package com.acme.transport.ops.terminalrelay

import com.acme.fleet.v1.CheckInspectionEligibilityRequestKt
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.checkInspectionEligibilityRequest
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.peekTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.warehousing.v1.WarehouseServiceGrpcKt
import com.acme.warehousing.v1.cloneWarehouseRequest
import com.acme.warehousing.v1.warehouse
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingQueryService/PeekTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun replayTelemetryReadingStatus(channel: ManagedChannel) {
    val origin = TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.peekTelemetryReading(peekTelemetryReadingRequest { this.telemetryReadingId = "live-replay-telemetry-reading-status" }) }
    val rec = ReplayTelemetryReadingStatusRecord(
        status = found.result.status,
        message = found.result.message,
    )
    val sinks = listOf<ReplayTelemetryReadingStatusDestination>(
        ReplayTelemetryReadingStatusInspectionValidation(InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub(channel)),
        ReplayTelemetryReadingStatusWarehouse(WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub(channel)),
    )
    sinks[0].transmit(rec)
}

/** Where replayTelemetryReadingStatus hands each record. */
interface ReplayTelemetryReadingStatusDestination {
    suspend fun transmit(rec: ReplayTelemetryReadingStatusRecord)
}

/** What replayTelemetryReadingStatus keeps of each PeekTelemetryReading response. */
data class ReplayTelemetryReadingStatusRecord(val status: String, val message: String)

/** Writes records to acme.fleet.v1.InspectionValidationService. */
class ReplayTelemetryReadingStatusInspectionValidation(private val stub: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub) : ReplayTelemetryReadingStatusDestination {
    override suspend fun transmit(rec: ReplayTelemetryReadingStatusRecord) {
        val request = checkInspectionEligibilityRequest {
            this.details = CheckInspectionEligibilityRequestKt.details { this.locale = rec.status }
        }
        stub.checkInspectionEligibility(request)
    }
}

/** Writes records to acme.warehousing.v1.WarehouseService. */
class ReplayTelemetryReadingStatusWarehouse(private val stub: WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub) : ReplayTelemetryReadingStatusDestination {
    override suspend fun transmit(rec: ReplayTelemetryReadingStatusRecord) {
        stub.cloneWarehouse(cloneWarehouseRequest { this.warehouse = warehouse { this.code = rec.message } })
    }
}
