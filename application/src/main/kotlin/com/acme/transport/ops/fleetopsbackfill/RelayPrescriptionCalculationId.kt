package com.acme.transport.ops.fleetopsbackfill

import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.RejectDriverLicenseRequestKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.rejectDriverLicenseRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.explainPrescriptionCalculationRequest
import com.acme.telematics.v1.RetryTelemetryReadingImportRequestKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.retryTelemetryReadingImportRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.prescriptions.v1.PrescriptionCalculationService/ExplainPrescriptionCalculation and hands what it keeps to the sink of its kind. */
suspend fun relayPrescriptionCalculationId(channel: ManagedChannel) {
    val origin = PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.explainPrescriptionCalculation(explainPrescriptionCalculationRequest { this.reason = "nightly-relay-prescription-calculation-id" }) }
    val entry = RelayPrescriptionCalculationIdRecord(
        prescriptionId = found.prescription.prescriptionId,
        status = normalize(found.outcome.status),
        message = found.outcome.message,
        referenceId = found.outcome.referenceId,
    )
    deliverRelayPrescriptionCalculationId("driver-license-approval", entry, channel)
}

/** Where relayPrescriptionCalculationId hands each record. */
interface RelayPrescriptionCalculationIdWriter {
    suspend fun forward(rec: RelayPrescriptionCalculationIdRecord)
}

/** What relayPrescriptionCalculationId keeps of each ExplainPrescriptionCalculation response. */
data class RelayPrescriptionCalculationIdRecord(val prescriptionId: String, val status: String, val message: String, val referenceId: String)

/** Passes records to acme.telematics.v1.TelemetryReadingImportService. */
class RelayPrescriptionCalculationIdTelemetryReadingImport(private val stub: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub) : RelayPrescriptionCalculationIdWriter {
    override suspend fun forward(rec: RelayPrescriptionCalculationIdRecord) {
        val request = retryTelemetryReadingImportRequest {
            this.details = RetryTelemetryReadingImportRequestKt.details { this.initiatedBy = rec.message; this.step = rec.status }
            this.telemetryReading = telemetryReading { this.kind = rec.referenceId }
        }
        stub.retryTelemetryReadingImport(request)
    }
}

/** Sends records to acme.drivers.v1.DriverLicenseApprovalService. */
class RelayPrescriptionCalculationIdDriverLicenseApproval(private val stub: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub) : RelayPrescriptionCalculationIdWriter {
    override suspend fun forward(rec: RelayPrescriptionCalculationIdRecord) {
        val request = rejectDriverLicenseRequest {
            this.driverLicense = driverLicense { this.driverLicenseId = rec.status; this.notes = rec.prescriptionId }
            this.details = RejectDriverLicenseRequestKt.details { this.comment = rec.referenceId }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/RejectDriverLicense") { stub.rejectDriverLicense(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayPrescriptionCalculationIdLogOnly : RelayPrescriptionCalculationIdWriter {
    override suspend fun forward(rec: RelayPrescriptionCalculationIdRecord) {
        LoggerFactory.getLogger("RelayPrescriptionCalculationIdRecord").atInfo().addKeyValue("prescriptionId", rec.prescriptionId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayPrescriptionCalculationId(kind: String, rec: RelayPrescriptionCalculationIdRecord, channel: ManagedChannel) {
    var sink: RelayPrescriptionCalculationIdWriter = RelayPrescriptionCalculationIdLogOnly()
    when (kind) {
        "telemetry-reading-import" -> sink = RelayPrescriptionCalculationIdTelemetryReadingImport(TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub(channel))
        "driver-license-approval" -> sink = RelayPrescriptionCalculationIdDriverLicenseApproval(DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
