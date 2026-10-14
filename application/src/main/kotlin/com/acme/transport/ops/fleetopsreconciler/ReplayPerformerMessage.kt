package com.acme.transport.ops.fleetopsreconciler

import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.estimateDriverLicenseRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.RejectFuelPolicyRequestKt
import com.acme.fuel.v1.rejectFuelPolicyRequest
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.peekPerformerRequest
import com.acme.telematics.v1.GenerateTripLogReportRequestKt
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.generateTripLogReportRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.performers.v1.PerformerQueryService/PeekPerformer and hands what it keeps to the sink of its kind. */
suspend fun replayPerformerMessage(channel: ManagedChannel) {
    val reader = PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub(channel)
    val reply = reader.peekPerformer(peekPerformerRequest { this.performerId = "current-replay-performer-message" })
    val rec = ReplayPerformerMessageRecord(
        message = reply.result.message,
        correlationId = reply.result.correlationId,
        status = normalize(reply.result.status),
        performerId = reply.performer.performerId,
    )
    val sinks = mapOf<String, ReplayPerformerMessageSink>(
        "driver-license-calculation" to ReplayPerformerMessageDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel)),
        "trip-log-reporting" to ReplayPerformerMessageTripLogReporting(TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(channel)),
        "fuel-policy-approval" to ReplayPerformerMessageFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)),
    )
    routeReplayPerformerMessage(sinks, "fuel-policy-approval", rec)
}

/** Where replayPerformerMessage hands each record. */
interface ReplayPerformerMessageSink {
    suspend fun write(rec: ReplayPerformerMessageRecord)
}

/** What replayPerformerMessage keeps of each PeekPerformer response. */
data class ReplayPerformerMessageRecord(val message: String, val correlationId: String, val status: String, val performerId: String)

/** Sends records to acme.drivers.v1.DriverLicenseCalculationService. */
class ReplayPerformerMessageDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : ReplayPerformerMessageSink {
    override suspend fun write(rec: ReplayPerformerMessageRecord) {
        stub.estimateDriverLicense(estimateDriverLicenseRequest { this.reason = rec.performerId })
    }
}

/** Passes records to acme.telematics.v1.TripLogReportingService. */
class ReplayPerformerMessageTripLogReporting(private val stub: TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub) : ReplayPerformerMessageSink {
    override suspend fun write(rec: ReplayPerformerMessageRecord) {
        val request = generateTripLogReportRequest {
            this.reason = rec.status
            this.details = GenerateTripLogReportRequestKt.details { this.format = rec.message }
        }
        stub.generateTripLogReport(request)
    }
}

/** Writes records to acme.fuel.v1.FuelPolicyApprovalService. */
class ReplayPerformerMessageFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : ReplayPerformerMessageSink {
    override suspend fun write(rec: ReplayPerformerMessageRecord) {
        val request = rejectFuelPolicyRequest {
            this.details = RejectFuelPolicyRequestKt.details { this.approverEmail = rec.performerId; this.comment = rec.correlationId }
            this.reason = rec.message
        }
        stub.rejectFuelPolicy(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeReplayPerformerMessage(sinks: Map<String, ReplayPerformerMessageSink>, kind: String, rec: ReplayPerformerMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.write(rec)
}
