package com.acme.transport.ops.linehaulimporter

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.fixedAsset
import com.acme.assets.v1.quoteFixedAssetRequest
import com.acme.customers.v1.FindHouseholdsRequestKt
import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.findHouseholdsRequest
import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.recalculatePatientsRequest
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.listTelemetryReadingConflictsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.TelemetryReadingSyncService/ListTelemetryReadingConflicts and hands what it keeps to the sink of its kind. */
suspend fun mirrorTelemetryReadingConflictsMessage(channel: ManagedChannel) {
    val origin = TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(channel)
    val found = origin.listTelemetryReadingConflicts(listTelemetryReadingConflictsRequest { this.orderBy = "default-mirror-telemetry-reading-conflicts-message" })
    val rec = MirrorTelemetryReadingConflictsMessageRecord(
        message = found.result.message,
        nextPageToken = found.pageInfo.nextPageToken,
    )
    deliverMirrorTelemetryReadingConflictsMessage("fixed-asset-calculation", rec, channel)
}

/** Where mirrorTelemetryReadingConflictsMessage hands each record. */
interface MirrorTelemetryReadingConflictsMessageTarget {
    suspend fun transmit(rec: MirrorTelemetryReadingConflictsMessageRecord)
}

/** What mirrorTelemetryReadingConflictsMessage keeps of each ListTelemetryReadingConflicts response. */
data class MirrorTelemetryReadingConflictsMessageRecord(val message: String, val nextPageToken: String)

/** Sends records to acme.assets.v1.FixedAssetCalculationService. */
class MirrorTelemetryReadingConflictsMessageFixedAssetCalculation(private val stub: FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub) : MirrorTelemetryReadingConflictsMessageTarget {
    override suspend fun transmit(rec: MirrorTelemetryReadingConflictsMessageRecord) {
        val request = quoteFixedAssetRequest {
            this.fixedAsset = fixedAsset { this.fixedAssetId = rec.message; this.name = rec.nextPageToken }
        }
        stub.quoteFixedAsset(request)
    }
}

/** Sends records to acme.patients.v1.PatientCalculationService. */
class MirrorTelemetryReadingConflictsMessagePatientCalculation(private val stub: PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub) : MirrorTelemetryReadingConflictsMessageTarget {
    override suspend fun transmit(rec: MirrorTelemetryReadingConflictsMessageRecord) {
        stub.recalculatePatients(recalculatePatientsRequest { this.patientId = rec.nextPageToken })
    }
}

/** Hands records to acme.customers.v1.HouseholdQueryService. */
class MirrorTelemetryReadingConflictsMessageHouseholdQuery(private val stub: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub) : MirrorTelemetryReadingConflictsMessageTarget {
    override suspend fun transmit(rec: MirrorTelemetryReadingConflictsMessageRecord) {
        val request = findHouseholdsRequest {
            this.filter = FindHouseholdsRequestKt.filter { this.region = rec.message }
        }
        stub.findHouseholds(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorTelemetryReadingConflictsMessageLogOnly : MirrorTelemetryReadingConflictsMessageTarget {
    override suspend fun transmit(rec: MirrorTelemetryReadingConflictsMessageRecord) {
        LoggerFactory.getLogger("MirrorTelemetryReadingConflictsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorTelemetryReadingConflictsMessage(kind: String, rec: MirrorTelemetryReadingConflictsMessageRecord, channel: ManagedChannel) {
    var sink: MirrorTelemetryReadingConflictsMessageTarget = MirrorTelemetryReadingConflictsMessageLogOnly()
    when (kind) {
        "fixed-asset-calculation" -> sink = MirrorTelemetryReadingConflictsMessageFixedAssetCalculation(FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub(channel))
        "patient-calculation" -> sink = MirrorTelemetryReadingConflictsMessagePatientCalculation(PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub(channel))
        "household-query" -> sink = MirrorTelemetryReadingConflictsMessageHouseholdQuery(HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
