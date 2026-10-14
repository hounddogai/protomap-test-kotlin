package com.acme.transport.ops.lastmilesync

import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.ledgerAccount
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.cloneTelemetryReadingRequest
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingService/CloneTelemetryReading and hands what it keeps to acme.ledger.v1.LedgerAccountValidationService. */
suspend fun syncTelemetryReadingSource(channel: ManagedChannel) {
    val source = TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)
    val current = source.cloneTelemetryReading(cloneTelemetryReadingRequest { this.requestId = "current-sync-telemetry-reading-source" })
    val rec = SyncTelemetryReadingSourceRecord(
        source = current.telemetryReading.source,
        kind = current.telemetryReading.kind,
        message = current.result.message,
        status = current.result.status,
    )
    SyncTelemetryReadingSourceLedgerAccountValidation(LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(channel)).relay(rec)
}

/** What syncTelemetryReadingSource keeps of each CloneTelemetryReading response. */
data class SyncTelemetryReadingSourceRecord(val source: String, val kind: String, val message: String, val status: String)

/** Hands records to acme.ledger.v1.LedgerAccountValidationService. */
class SyncTelemetryReadingSourceLedgerAccountValidation(private val stub: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub) {
    suspend fun relay(rec: SyncTelemetryReadingSourceRecord) {
        val request = previewLedgerAccountRequest {
            this.ledgerAccount = ledgerAccount { this.currencyCode = rec.source }
            this.ledgerAccountId = rec.kind
        }
        stub.previewLedgerAccount(request)
    }
}
