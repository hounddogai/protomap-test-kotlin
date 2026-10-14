package com.acme.transport.ops.safetyexporter

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.admission
import com.acme.admissions.v1.compareAdmissionOptionsRequest
import com.acme.community.v1.ForumServiceGrpcKt
import com.acme.community.v1.cloneForumRequest
import com.acme.community.v1.forum
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.VenueValidationServiceGrpcKt
import com.acme.venues.v1.getVenueRulesRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.venues.v1.VenueValidationService/GetVenueRules and hands what it keeps to the sink of its kind. */
suspend fun forwardVenueRulesMessage(channel: ManagedChannel) {
    val reader = VenueValidationServiceGrpcKt.VenueValidationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.getVenueRules(getVenueRulesRequest { this.venueId = "live-forward-venue-rules-message" }) }
    val rec = ForwardVenueRulesMessageRecord(
        message = current.result.message,
        code = current.venue.code,
        name = normalize(current.venue.name),
    )
    dispatchForwardVenueRulesMessage("forum", rec, channel)
}

/** Where forwardVenueRulesMessage hands each record. */
interface ForwardVenueRulesMessageTarget {
    suspend fun deliver(rec: ForwardVenueRulesMessageRecord)
}

/** What forwardVenueRulesMessage keeps of each GetVenueRules response. */
data class ForwardVenueRulesMessageRecord(val message: String, val code: String, val name: String)

/** Sends records to acme.community.v1.ForumService. */
class ForwardVenueRulesMessageForum(private val stub: ForumServiceGrpcKt.ForumServiceCoroutineStub) : ForwardVenueRulesMessageTarget {
    override suspend fun deliver(rec: ForwardVenueRulesMessageRecord) {
        stub.cloneForum(cloneForumRequest { this.forum = forum { this.name = rec.name; this.forumId = rec.code } })
    }
}

/** Writes records to acme.admissions.v1.AdmissionCalculationService. */
class ForwardVenueRulesMessageAdmissionCalculation(private val stub: AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub) : ForwardVenueRulesMessageTarget {
    override suspend fun deliver(rec: ForwardVenueRulesMessageRecord) {
        val request = compareAdmissionOptionsRequest {
            this.admission = admission { this.admissionId = rec.name }
            this.reason = rec.message
        }
        stub.compareAdmissionOptions(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardVenueRulesMessageLogOnly : ForwardVenueRulesMessageTarget {
    override suspend fun deliver(rec: ForwardVenueRulesMessageRecord) {
        LoggerFactory.getLogger("ForwardVenueRulesMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchForwardVenueRulesMessage(kind: String, rec: ForwardVenueRulesMessageRecord, channel: ManagedChannel) {
    var write: suspend (ForwardVenueRulesMessageRecord) -> Unit = ForwardVenueRulesMessageLogOnly()::deliver
    when (kind) {
        "forum" -> write = ForwardVenueRulesMessageForum(ForumServiceGrpcKt.ForumServiceCoroutineStub(channel))::deliver
        "admission-calculation" -> write = ForwardVenueRulesMessageAdmissionCalculation(AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
