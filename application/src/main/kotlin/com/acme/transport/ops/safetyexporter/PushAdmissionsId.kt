package com.acme.transport.ops.safetyexporter

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.searchAdmissionsRequest
import com.acme.audit.v1.AuditEventQueryServiceGrpcKt
import com.acme.audit.v1.SuggestAuditEventsRequestKt
import com.acme.audit.v1.suggestAuditEventsRequest
import com.acme.common.v1.pageRequest
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.quoteZoneRequest
import com.acme.dispatch.v1.zone
import com.acme.transport.kit.text.normalize
import com.acme.venues.v1.SummarizeVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.summarizeVenuesRequest
import com.acme.yards.v1.SimulateYardRequestKt
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.simulateYardRequest
import io.grpc.ManagedChannel

/** Reads acme.admissions.v1.AdmissionService/SearchAdmissions and hands what it keeps to the sink of its kind. */
suspend fun pushAdmissionsId(channel: ManagedChannel) {
    val upstream = AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub(channel)
    val current = upstream.searchAdmissions(searchAdmissionsRequest { this.orderBy = "default-push-admissions-id" })
    val entry = PushAdmissionsIdRecord(
        correlationId = normalize(current.result.correlationId),
        nextPageToken = current.pageInfo.nextPageToken,
        status = current.result.status,
    )
    actOnPushAdmissionsId("venue-query", entry, channel)
}

/** Where pushAdmissionsId hands each record. */
interface PushAdmissionsIdTarget {
    suspend fun write(rec: PushAdmissionsIdRecord)
}

/** What pushAdmissionsId keeps of each SearchAdmissions response. */
data class PushAdmissionsIdRecord(val correlationId: String, val nextPageToken: String, val status: String)

/** Hands records to acme.dispatch.v1.ZoneCalculationService. */
class PushAdmissionsIdZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : PushAdmissionsIdTarget {
    override suspend fun write(rec: PushAdmissionsIdRecord) {
        val request = quoteZoneRequest {
            this.zone = zone { this.name = rec.status; this.parentId = rec.nextPageToken }
        }
        stub.quoteZone(request)
    }
}

/** Writes records to acme.yards.v1.YardCalculationService. */
class PushAdmissionsIdYardCalculation(private val stub: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub) : PushAdmissionsIdTarget {
    override suspend fun write(rec: PushAdmissionsIdRecord) {
        val request = simulateYardRequest {
            this.reason = rec.status
            this.yardId = rec.correlationId
            this.details = SimulateYardRequestKt.details { this.currencyCode = rec.nextPageToken }
        }
        stub.simulateYard(request)
    }
}

/** Passes records to acme.audit.v1.AuditEventQueryService. */
class PushAdmissionsIdAuditEventQuery(private val stub: AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub) : PushAdmissionsIdTarget {
    override suspend fun write(rec: PushAdmissionsIdRecord) {
        val request = suggestAuditEventsRequest {
            this.filter = SuggestAuditEventsRequestKt.filter { this.ownerId = rec.nextPageToken }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.suggestAuditEvents(request)
    }
}

/** Writes records to acme.venues.v1.VenueQueryService. */
class PushAdmissionsIdVenueQuery(private val stub: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub) : PushAdmissionsIdTarget {
    override suspend fun write(rec: PushAdmissionsIdRecord) {
        val request = summarizeVenuesRequest {
            this.details = SummarizeVenuesRequestKt.details { this.cron = rec.status }
            this.venueId = rec.correlationId
            this.reason = rec.nextPageToken
        }
        stub.summarizeVenues(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPushAdmissionsId(kind: String, rec: PushAdmissionsIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "zone-calculation" to { PushAdmissionsIdZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel)).write(rec) },
        "yard-calculation" to { PushAdmissionsIdYardCalculation(YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub(channel)).write(rec) },
        "audit-event-query" to { PushAdmissionsIdAuditEventQuery(AuditEventQueryServiceGrpcKt.AuditEventQueryServiceCoroutineStub(channel)).write(rec) },
        "venue-query" to { PushAdmissionsIdVenueQuery(VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
