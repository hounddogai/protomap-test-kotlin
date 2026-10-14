package com.acme.transport.ops.terminalrelay

import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.lookupAuditEventRequest
import com.acme.fuel.v1.CancelFuelPolicyImportRequestKt
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.cancelFuelPolicyImportRequest
import com.acme.telematics.v1.EscalateTripLogRequestKt
import com.acme.telematics.v1.MapTripLogImportColumnsRequestKt
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.escalateTripLogRequest
import com.acme.telematics.v1.mapTripLogImportColumnsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.cloneVenueRequest
import io.grpc.ManagedChannel

/** Reads acme.venues.v1.VenueService/CloneVenue and hands what it keeps to the sink of its kind. */
suspend fun syncVenueMessage(channel: ManagedChannel) {
    val upstream = VenueServiceGrpcKt.VenueServiceCoroutineStub(channel)
    val current = upstream.cloneVenue(cloneVenueRequest { this.requestId = "current-sync-venue-message" })
    val entry = SyncVenueMessageRecord(
        message = normalize(current.result.message),
        status = current.result.status,
        venueId = current.venue.venueId,
        correlationId = current.result.correlationId,
    )
    actOnSyncVenueMessage("trip-log-approval", entry, channel)
}

/** Where syncVenueMessage hands each record. */
interface SyncVenueMessageTarget {
    suspend fun write(rec: SyncVenueMessageRecord)
}

/** What syncVenueMessage keeps of each CloneVenue response. */
data class SyncVenueMessageRecord(val message: String, val status: String, val venueId: String, val correlationId: String)

/** Hands records to acme.telematics.v1.TripLogImportService. */
class SyncVenueMessageTripLogImport(private val stub: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub) : SyncVenueMessageTarget {
    override suspend fun write(rec: SyncVenueMessageRecord) {
        val request = mapTripLogImportColumnsRequest {
            this.details = MapTripLogImportColumnsRequestKt.details { this.locale = rec.status }
            this.reason = rec.message
            this.tripLog = tripLog { this.tripLogId = rec.venueId }
        }
        stub.mapTripLogImportColumns(request)
    }
}

/** Writes records to acme.audit.v1.AuditEventQueryService. */
class SyncVenueMessageAuditEventQuery(private val stub: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub) : SyncVenueMessageTarget {
    override suspend fun write(rec: SyncVenueMessageRecord) {
        RpcBoundary.run("acme.audit.v1.AuditEventQueryService/LookupAuditEvent") { stub.lookupAuditEvent(lookupAuditEventRequest { this.auditEventId = rec.message }) }
    }
}

/** Hands records to acme.fuel.v1.FuelPolicyImportService. */
class SyncVenueMessageFuelPolicyImport(private val stub: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub) : SyncVenueMessageTarget {
    override suspend fun write(rec: SyncVenueMessageRecord) {
        val request = cancelFuelPolicyImportRequest {
            this.fuelPolicyId = rec.message
            this.details = CancelFuelPolicyImportRequestKt.details { this.reasonCode = rec.venueId }
            this.reason = rec.correlationId
        }
        stub.cancelFuelPolicyImport(request)
    }
}

/** Writes records to acme.telematics.v1.TripLogApprovalService. */
class SyncVenueMessageTripLogApproval(private val stub: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub) : SyncVenueMessageTarget {
    override suspend fun write(rec: SyncVenueMessageRecord) {
        val request = escalateTripLogRequest {
            this.tripLog = tripLog { this.number = rec.correlationId; this.tripLogId = rec.message }
            this.details = EscalateTripLogRequestKt.details { this.approverEmail = rec.venueId }
        }
        stub.escalateTripLog(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSyncVenueMessage(kind: String, rec: SyncVenueMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "trip-log-import" to { SyncVenueMessageTripLogImport(TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub(channel)).write(rec) },
        "audit-event-query" to { SyncVenueMessageAuditEventQuery(AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub(channel)).write(rec) },
        "fuel-policy-import" to { SyncVenueMessageFuelPolicyImport(FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub(channel)).write(rec) },
        "trip-log-approval" to { SyncVenueMessageTripLogApproval(TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
