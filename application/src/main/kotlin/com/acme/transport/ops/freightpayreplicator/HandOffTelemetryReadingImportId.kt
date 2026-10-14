package com.acme.transport.ops.freightpayreplicator

import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.lookupBoxOfficeSaleRequest
import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.certificate
import com.acme.certificates.v1.explainCertificateCalculationRequest
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.retryTelemetryReadingImportRequest
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingImportService/RetryTelemetryReadingImport and hands what it keeps to the sink of its kind. */
suspend fun handOffTelemetryReadingImportId(channel: ManagedChannel) {
    val upstream = TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub(channel)
    val found = upstream.retryTelemetryReadingImport(retryTelemetryReadingImportRequest { this.telemetryReadingId = "main-hand-off-telemetry-reading-import-id" })
    val record = HandOffTelemetryReadingImportIdRecord(
        referenceId = found.outcome.referenceId,
        status = found.outcome.status,
        source = found.telemetryReading.source,
    )
    val registry = mutableMapOf<String, HandOffTelemetryReadingImportIdWriter>()
    for (registration in listOf(
        HandOffTelemetryReadingImportIdRegistration("certificate-calculation", HandOffTelemetryReadingImportIdCertificateCalculation(CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub(channel))),
        HandOffTelemetryReadingImportIdRegistration("box-office-sale-query", HandOffTelemetryReadingImportIdBoxOfficeSaleQuery(BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["box-office-sale-query"] ?: return
    sink.write(record)
}

/** Where handOffTelemetryReadingImportId hands each record. */
interface HandOffTelemetryReadingImportIdWriter {
    suspend fun write(rec: HandOffTelemetryReadingImportIdRecord)
}

/** What handOffTelemetryReadingImportId keeps of each RetryTelemetryReadingImport response. */
data class HandOffTelemetryReadingImportIdRecord(val referenceId: String, val status: String, val source: String)

/** Passes records to acme.certificates.v1.CertificateCalculationService. */
class HandOffTelemetryReadingImportIdCertificateCalculation(private val stub: CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub) : HandOffTelemetryReadingImportIdWriter {
    override suspend fun write(rec: HandOffTelemetryReadingImportIdRecord) {
        val request = explainCertificateCalculationRequest {
            this.certificate = certificate { this.number = rec.referenceId; this.notes = rec.source }
            this.reason = rec.status
        }
        stub.explainCertificateCalculation(request)
    }
}

/** Writes records to acme.boxoffice.v1.BoxOfficeSaleQueryService. */
class HandOffTelemetryReadingImportIdBoxOfficeSaleQuery(private val stub: BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub) : HandOffTelemetryReadingImportIdWriter {
    override suspend fun write(rec: HandOffTelemetryReadingImportIdRecord) {
        stub.lookupBoxOfficeSale(lookupBoxOfficeSaleRequest { this.boxOfficeSaleId = rec.referenceId })
    }
}

/** A sink and the kind of record it takes. */
class HandOffTelemetryReadingImportIdRegistration(val kind: String, val sink: HandOffTelemetryReadingImportIdWriter)
