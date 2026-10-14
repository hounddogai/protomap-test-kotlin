package com.acme.transport.ops.customsauditor

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.previewSearchIndexRequest
import com.acme.search.v1.searchIndex
import com.acme.telematics.v1.CalculateGeoFenceRequestKt
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.calculateGeoFenceRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.syncGeoFencesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceSyncService/SyncGeoFences and hands what it keeps to the sink of its kind. */
suspend fun syncGeoFencesDescription(channel: ManagedChannel) {
    val reader = GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.syncGeoFences(syncGeoFencesRequest { this.reason = "default-sync-geo-fences-description" }) }
    val entry = SyncGeoFencesDescriptionRecord(
        description = normalize(reply.geoFence.description),
        geoFenceId = reply.geoFence.geoFenceId,
        message = reply.outcome.message,
        status = normalize(reply.result.status),
    )
    val sinks = mapOf<String, SyncGeoFencesDescriptionWriter>(
        "geo-fence-calculation" to SyncGeoFencesDescriptionGeoFenceCalculation(GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(channel)),
        "audit-event-validation" to SyncGeoFencesDescriptionAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        "search-index-validation" to SyncGeoFencesDescriptionSearchIndexValidation(SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "search-index-validation") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where syncGeoFencesDescription hands each record. */
interface SyncGeoFencesDescriptionWriter {
    suspend fun deliver(rec: SyncGeoFencesDescriptionRecord)
}

/** What syncGeoFencesDescription keeps of each SyncGeoFences response. */
data class SyncGeoFencesDescriptionRecord(val description: String, val geoFenceId: String, val message: String, val status: String)

/** Writes records to acme.telematics.v1.GeoFenceCalculationService. */
class SyncGeoFencesDescriptionGeoFenceCalculation(private val stub: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub) : SyncGeoFencesDescriptionWriter {
    override suspend fun deliver(rec: SyncGeoFencesDescriptionRecord) {
        val request = calculateGeoFenceRequest {
            this.geoFence = geoFence { this.name = rec.message }
            this.details = CalculateGeoFenceRequestKt.details { this.currencyCode = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceCalculationService/CalculateGeoFence") { stub.calculateGeoFence(request) }
    }
}

/** Hands records to acme.audit.v1.AuditEventValidationService. */
class SyncGeoFencesDescriptionAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : SyncGeoFencesDescriptionWriter {
    override suspend fun deliver(rec: SyncGeoFencesDescriptionRecord) {
        val request = normalizeAuditEventRequest {
            this.auditEvent = auditEvent { this.correlationId = rec.message; this.kind = rec.geoFenceId }
        }
        stub.normalizeAuditEvent(request)
    }
}

/** Hands records to acme.search.v1.SearchIndexValidationService. */
class SyncGeoFencesDescriptionSearchIndexValidation(private val stub: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub) : SyncGeoFencesDescriptionWriter {
    override suspend fun deliver(rec: SyncGeoFencesDescriptionRecord) {
        val request = previewSearchIndexRequest {
            this.searchIndex = searchIndex { this.searchIndexId = rec.status; this.code = rec.description }
            this.searchIndexId = rec.message
        }
        stub.previewSearchIndex(request)
    }
}
