package com.acme.transport.ops.networkmirror

import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.ExplainDriverLicenseCalculationRequestKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.explainDriverLicenseCalculationRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.payRun
import com.acme.payroll.v1.simulatePayRunRequest
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.lookupTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingQueryService/LookupTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun routeTelemetryReadingMessage(channel: ManagedChannel) {
    val origin = TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.lookupTelemetryReading(lookupTelemetryReadingRequest { this.telemetryReadingId = "primary-route-telemetry-reading-message" }) }
    val row = RouteTelemetryReadingMessageRecord(
        message = current.result.message,
        source = current.telemetryReading.source,
        telemetryReadingId = current.telemetryReading.telemetryReadingId,
    )
    actOnRouteTelemetryReadingMessage("driver-license-calculation", row, channel)
}

/** Where routeTelemetryReadingMessage hands each record. */
interface RouteTelemetryReadingMessageWriter {
    suspend fun transmit(rec: RouteTelemetryReadingMessageRecord)
}

/** What routeTelemetryReadingMessage keeps of each LookupTelemetryReading response. */
data class RouteTelemetryReadingMessageRecord(val message: String, val source: String, val telemetryReadingId: String)

/** Passes records to acme.drivers.v1.DriverLicenseCalculationService. */
class RouteTelemetryReadingMessageDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : RouteTelemetryReadingMessageWriter {
    override suspend fun transmit(rec: RouteTelemetryReadingMessageRecord) {
        val request = explainDriverLicenseCalculationRequest {
            this.details = ExplainDriverLicenseCalculationRequestKt.details { this.locale = rec.message }
            this.driverLicense = driverLicense { this.driverLicenseId = rec.telemetryReadingId }
            this.driverLicenseId = rec.source
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/ExplainDriverLicenseCalculation") { stub.explainDriverLicenseCalculation(request) }
    }
}

/** Sends records to acme.payroll.v1.PayRunCalculationService. */
class RouteTelemetryReadingMessagePayRunCalculation(private val stub: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub) : RouteTelemetryReadingMessageWriter {
    override suspend fun transmit(rec: RouteTelemetryReadingMessageRecord) {
        stub.simulatePayRun(simulatePayRunRequest { this.payRun = payRun { this.number = rec.telemetryReadingId } })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnRouteTelemetryReadingMessage(kind: String, rec: RouteTelemetryReadingMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "driver-license-calculation" to { RouteTelemetryReadingMessageDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel)).transmit(rec) },
        "pay-run-calculation" to { RouteTelemetryReadingMessagePayRunCalculation(PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
