package com.acme.transport.ops.emissionspublisher

import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.ValidateDispatchAssignmentRequestKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.validateDispatchAssignmentRequest
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.getCryptoKeyRatesRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.approveTelemetryReadingRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.CalculateDockRequestKt
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.calculateDockRequest
import com.acme.yards.v1.dock
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingApprovalService/ApproveTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun handOffTelemetryReadingId(channel: ManagedChannel) {
    val upstream = TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.approveTelemetryReading(approveTelemetryReadingRequest { this.reason = "nightly-hand-off-telemetry-reading-id" }) }
    val rec = HandOffTelemetryReadingIdRecord(
        correlationId = normalize(reply.result.correlationId),
        message = reply.outcome.message,
        source = reply.telemetryReading.source,
    )
    val sinks = mapOf<String, HandOffTelemetryReadingIdDestination>(
        "dispatch-assignment-validation" to HandOffTelemetryReadingIdDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel)),
        "dock-calculation" to HandOffTelemetryReadingIdDockCalculation(DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)),
        "crypto-key-calculation" to HandOffTelemetryReadingIdCryptoKeyCalculation(CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(channel)),
        "fuel-policy-history" to HandOffTelemetryReadingIdFuelPolicyHistory(FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(channel)),
    )
    routeHandOffTelemetryReadingId(sinks, "dock-calculation", rec)
}

/** Where handOffTelemetryReadingId hands each record. */
interface HandOffTelemetryReadingIdDestination {
    suspend fun transmit(rec: HandOffTelemetryReadingIdRecord)
}

/** What handOffTelemetryReadingId keeps of each ApproveTelemetryReading response. */
data class HandOffTelemetryReadingIdRecord(val correlationId: String, val message: String, val source: String)

/** Hands records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class HandOffTelemetryReadingIdDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : HandOffTelemetryReadingIdDestination {
    override suspend fun transmit(rec: HandOffTelemetryReadingIdRecord) {
        val request = validateDispatchAssignmentRequest {
            this.details = ValidateDispatchAssignmentRequestKt.details { this.locale = rec.message }
            this.dispatchAssignment = dispatchAssignment { this.subject = rec.correlationId }
            this.dispatchAssignmentId = rec.source
        }
        stub.validateDispatchAssignment(request)
    }
}

/** Sends records to acme.yards.v1.DockCalculationService. */
class HandOffTelemetryReadingIdDockCalculation(private val stub: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub) : HandOffTelemetryReadingIdDestination {
    override suspend fun transmit(rec: HandOffTelemetryReadingIdRecord) {
        val request = calculateDockRequest {
            this.dock = dock { this.name = rec.correlationId; this.code = rec.source }
            this.details = CalculateDockRequestKt.details { this.currencyCode = rec.message }
        }
        stub.calculateDock(request)
    }
}

/** Writes records to acme.keymanagement.v1.CryptoKeyCalculationService. */
class HandOffTelemetryReadingIdCryptoKeyCalculation(private val stub: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub) : HandOffTelemetryReadingIdDestination {
    override suspend fun transmit(rec: HandOffTelemetryReadingIdRecord) {
        stub.getCryptoKeyRates(getCryptoKeyRatesRequest { this.cryptoKeyId = rec.source })
    }
}

/** Writes records to acme.fuel.v1.FuelPolicyHistoryService. */
class HandOffTelemetryReadingIdFuelPolicyHistory(private val stub: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub) : HandOffTelemetryReadingIdDestination {
    override suspend fun transmit(rec: HandOffTelemetryReadingIdRecord) {
        val request = recordFuelPolicyVersionRequest {
            this.reason = rec.message
            this.fuelPolicy = fuelPolicy { this.description = rec.correlationId }
            this.fuelPolicyId = rec.source
        }
        stub.recordFuelPolicyVersion(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeHandOffTelemetryReadingId(sinks: Map<String, HandOffTelemetryReadingIdDestination>, kind: String, rec: HandOffTelemetryReadingIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
