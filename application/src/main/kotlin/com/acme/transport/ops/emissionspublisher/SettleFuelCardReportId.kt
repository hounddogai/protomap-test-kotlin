package com.acme.transport.ops.emissionspublisher

import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.PreviewFuelTransactionRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.previewFuelTransactionRequest
import com.acme.fuel.v1.shareFuelCardReportRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.getTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelCardReportingService/ShareFuelCardReport and hands what it keeps to the sink of its kind. */
suspend fun settleFuelCardReportId(channel: ManagedChannel) {
    val upstream = FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.shareFuelCardReport(shareFuelCardReportRequest { this.reason = "current-settle-fuel-card-report-id" }) }
    val entry = SettleFuelCardReportIdRecord(
        referenceId = reply.outcome.referenceId,
        message = reply.outcome.message,
        resultMessage = reply.result.message,
    )
    deliverSettleFuelCardReportId("fuel-transaction-validation", entry, channel)
}

/** Where settleFuelCardReportId hands each record. */
interface SettleFuelCardReportIdWriter {
    suspend fun hand(rec: SettleFuelCardReportIdRecord)
}

/** What settleFuelCardReportId keeps of each ShareFuelCardReport response. */
data class SettleFuelCardReportIdRecord(val referenceId: String, val message: String, val resultMessage: String)

/** Writes records to acme.telematics.v1.TelemetryReadingService. */
class SettleFuelCardReportIdTelemetryReading(private val stub: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub) : SettleFuelCardReportIdWriter {
    override suspend fun hand(rec: SettleFuelCardReportIdRecord) {
        stub.getTelemetryReading(getTelemetryReadingRequest { this.telemetryReadingId = rec.resultMessage })
    }
}

/** Sends records to acme.fuel.v1.FuelTransactionValidationService. */
class SettleFuelCardReportIdFuelTransactionValidation(private val stub: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub) : SettleFuelCardReportIdWriter {
    override suspend fun hand(rec: SettleFuelCardReportIdRecord) {
        val request = previewFuelTransactionRequest {
            this.fuelTransaction = fuelTransaction { this.correlationId = rec.message; this.kind = rec.resultMessage }
            this.details = PreviewFuelTransactionRequestKt.details { this.locale = rec.referenceId }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/PreviewFuelTransaction") { stub.previewFuelTransaction(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleFuelCardReportIdLogOnly : SettleFuelCardReportIdWriter {
    override suspend fun hand(rec: SettleFuelCardReportIdRecord) {
        LoggerFactory.getLogger("SettleFuelCardReportIdRecord").atInfo().addKeyValue("referenceId", rec.referenceId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSettleFuelCardReportId(kind: String, rec: SettleFuelCardReportIdRecord, channel: ManagedChannel) {
    var sink: SettleFuelCardReportIdWriter = SettleFuelCardReportIdLogOnly()
    when (kind) {
        "telemetry-reading" -> sink = SettleFuelCardReportIdTelemetryReading(TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel))
        "fuel-transaction-validation" -> sink = SettleFuelCardReportIdFuelTransactionValidation(FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
