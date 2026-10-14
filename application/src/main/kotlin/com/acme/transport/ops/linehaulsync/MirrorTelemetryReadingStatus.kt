package com.acme.transport.ops.linehaulsync

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.simulateCarePlanRequest
import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.rejectConsentRecordRequest
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.validateLedgerAccountRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.createTelemetryReadingRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingService/CreateTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun mirrorTelemetryReadingStatus(channel: ManagedChannel) {
    val reader = TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)
    val found = reader.createTelemetryReading(createTelemetryReadingRequest { this.requestId = "main-mirror-telemetry-reading-status" })
    val row = MirrorTelemetryReadingStatusRecord(
        status = normalize(found.result.status),
        message = found.result.message,
        kind = found.telemetryReading.kind,
        source = found.telemetryReading.source,
    )
    val registry = mutableMapOf<String, MirrorTelemetryReadingStatusWriter>()
    for (registration in listOf(
        MirrorTelemetryReadingStatusRegistration("ledger-account-validation", MirrorTelemetryReadingStatusLedgerAccountValidation(LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(channel))),
        MirrorTelemetryReadingStatusRegistration("care-plan-calculation", MirrorTelemetryReadingStatusCarePlanCalculation(CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel))),
        MirrorTelemetryReadingStatusRegistration("consent-record-approval", MirrorTelemetryReadingStatusConsentRecordApproval(ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["ledger-account-validation"] ?: return
    sink.write(row)
}

/** Where mirrorTelemetryReadingStatus hands each record. */
interface MirrorTelemetryReadingStatusWriter {
    suspend fun write(rec: MirrorTelemetryReadingStatusRecord)
}

/** What mirrorTelemetryReadingStatus keeps of each CreateTelemetryReading response. */
data class MirrorTelemetryReadingStatusRecord(val status: String, val message: String, val kind: String, val source: String)

/** Sends records to acme.ledger.v1.LedgerAccountValidationService. */
class MirrorTelemetryReadingStatusLedgerAccountValidation(private val stub: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub) : MirrorTelemetryReadingStatusWriter {
    override suspend fun write(rec: MirrorTelemetryReadingStatusRecord) {
        val request = validateLedgerAccountRequest {
            this.reason = rec.source
            this.ledgerAccountId = rec.status
        }
        stub.validateLedgerAccount(request)
    }
}

/** Writes records to acme.careplans.v1.CarePlanCalculationService. */
class MirrorTelemetryReadingStatusCarePlanCalculation(private val stub: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub) : MirrorTelemetryReadingStatusWriter {
    override suspend fun write(rec: MirrorTelemetryReadingStatusRecord) {
        stub.simulateCarePlan(simulateCarePlanRequest { this.reason = rec.source })
    }
}

/** Hands records to acme.customers.v1.ConsentRecordApprovalService. */
class MirrorTelemetryReadingStatusConsentRecordApproval(private val stub: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub) : MirrorTelemetryReadingStatusWriter {
    override suspend fun write(rec: MirrorTelemetryReadingStatusRecord) {
        RpcBoundary.run("acme.customers.v1.ConsentRecordApprovalService/RejectConsentRecord") { stub.rejectConsentRecord(rejectConsentRecordRequest { this.reason = rec.kind }) }
    }
}

/** A sink and the kind of record it takes. */
class MirrorTelemetryReadingStatusRegistration(val kind: String, val sink: MirrorTelemetryReadingStatusWriter)
