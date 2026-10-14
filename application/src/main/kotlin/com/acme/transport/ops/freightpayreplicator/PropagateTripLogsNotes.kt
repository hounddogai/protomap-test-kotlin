package com.acme.transport.ops.freightpayreplicator

import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.commentConsentRecordApprovalRequest
import com.acme.customers.v1.consentRecord
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.peekVideoVisitRequest
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.lintTripLogsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogValidationService/LintTripLogs and hands what it keeps to the sink of its kind. */
suspend fun propagateTripLogsNotes(channel: ManagedChannel) {
    val reader = TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(channel)
    val reply = reader.lintTripLogs(lintTripLogsRequest { this.tripLogId = "live-propagate-trip-logs-notes" })
    val entry = PropagateTripLogsNotesRecord(
        notes = reply.tripLog.notes,
        tripLogId = normalize(reply.tripLog.tripLogId),
    )
    val sinks = mapOf<String, PropagateTripLogsNotesSink>(
        "video-visit-query" to PropagateTripLogsNotesVideoVisitQuery(VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(channel)),
        "consent-record-approval" to PropagateTripLogsNotesConsentRecordApproval(ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(channel)),
    )
    routePropagateTripLogsNotes(sinks, "video-visit-query", entry)
}

/** Where propagateTripLogsNotes hands each record. */
interface PropagateTripLogsNotesSink {
    suspend fun forward(rec: PropagateTripLogsNotesRecord)
}

/** What propagateTripLogsNotes keeps of each LintTripLogs response. */
data class PropagateTripLogsNotesRecord(val notes: String, val tripLogId: String)

/** Hands records to acme.telehealth.v1.VideoVisitQueryService. */
class PropagateTripLogsNotesVideoVisitQuery(private val stub: VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub) : PropagateTripLogsNotesSink {
    override suspend fun forward(rec: PropagateTripLogsNotesRecord) {
        RpcBoundary.run("acme.telehealth.v1.VideoVisitQueryService/PeekVideoVisit") { stub.peekVideoVisit(peekVideoVisitRequest { this.videoVisitId = rec.notes }) }
    }
}

/** Passes records to acme.customers.v1.ConsentRecordApprovalService. */
class PropagateTripLogsNotesConsentRecordApproval(private val stub: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub) : PropagateTripLogsNotesSink {
    override suspend fun forward(rec: PropagateTripLogsNotesRecord) {
        val request = commentConsentRecordApprovalRequest {
            this.consentRecordId = rec.tripLogId
            this.consentRecord = consentRecord { this.consentRecordId = rec.notes }
        }
        stub.commentConsentRecordApproval(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routePropagateTripLogsNotes(sinks: Map<String, PropagateTripLogsNotesSink>, kind: String, rec: PropagateTripLogsNotesRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
