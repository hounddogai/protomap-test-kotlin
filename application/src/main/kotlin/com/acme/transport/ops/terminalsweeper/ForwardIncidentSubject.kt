package com.acme.transport.ops.terminalsweeper

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.SearchAdmissionsRequestKt
import com.acme.admissions.v1.searchAdmissionsRequest
import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.deleteAuditEventRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.lookupCarePlanRequest
import com.acme.common.v1.pageRequest
import com.acme.itsm.v1.IncidentServiceGrpcKt
import com.acme.itsm.v1.cloneIncidentRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.SuggestSponsorsRequestKt
import com.acme.sponsorships.v1.suggestSponsorsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.itsm.v1.IncidentService/CloneIncident and hands what it keeps to the sink of its kind. */
suspend fun forwardIncidentSubject(channel: ManagedChannel) {
    val upstream = IncidentServiceGrpcKt.IncidentServiceCoroutineStub(channel)
    val found = upstream.cloneIncident(cloneIncidentRequest { this.requestId = "default-forward-incident-subject" })
    val row = ForwardIncidentSubjectRecord(
        subject = found.incident.subject,
        correlationId = found.result.correlationId,
        status = found.result.status,
    )
    val registry = mutableMapOf<String, ForwardIncidentSubjectSink>()
    for (registration in listOf(
        ForwardIncidentSubjectRegistration("sponsor-query", ForwardIncidentSubjectSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel))),
        ForwardIncidentSubjectRegistration("admission", ForwardIncidentSubjectAdmission(AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub(channel))),
        ForwardIncidentSubjectRegistration("care-plan-query", ForwardIncidentSubjectCarePlanQuery(CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel))),
        ForwardIncidentSubjectRegistration("audit-event", ForwardIncidentSubjectAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["audit-event"] ?: return
    sink.relay(row)
}

/** Where forwardIncidentSubject hands each record. */
interface ForwardIncidentSubjectSink {
    suspend fun relay(rec: ForwardIncidentSubjectRecord)
}

/** What forwardIncidentSubject keeps of each CloneIncident response. */
data class ForwardIncidentSubjectRecord(val subject: String, val correlationId: String, val status: String)

/** Writes records to acme.sponsorships.v1.SponsorQueryService. */
class ForwardIncidentSubjectSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : ForwardIncidentSubjectSink {
    override suspend fun relay(rec: ForwardIncidentSubjectRecord) {
        val request = suggestSponsorsRequest {
            this.orderBy = rec.status
            this.page = pageRequest { this.pageToken = rec.subject }
            this.filter = SuggestSponsorsRequestKt.filter { this.ownerId = rec.correlationId }
        }
        stub.suggestSponsors(request)
    }
}

/** Hands records to acme.admissions.v1.AdmissionService. */
class ForwardIncidentSubjectAdmission(private val stub: AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub) : ForwardIncidentSubjectSink {
    override suspend fun relay(rec: ForwardIncidentSubjectRecord) {
        val request = searchAdmissionsRequest {
            this.filter = SearchAdmissionsRequestKt.filter { this.region = rec.correlationId }
        }
        RpcBoundary.run("acme.admissions.v1.AdmissionService/SearchAdmissions") { stub.searchAdmissions(request) }
    }
}

/** Writes records to acme.careplans.v1.CarePlanQueryService. */
class ForwardIncidentSubjectCarePlanQuery(private val stub: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub) : ForwardIncidentSubjectSink {
    override suspend fun relay(rec: ForwardIncidentSubjectRecord) {
        stub.lookupCarePlan(lookupCarePlanRequest { this.carePlanId = rec.status })
    }
}

/** Writes records to acme.audit.v1.AuditEventService. */
class ForwardIncidentSubjectAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : ForwardIncidentSubjectSink {
    override suspend fun relay(rec: ForwardIncidentSubjectRecord) {
        stub.deleteAuditEvent(deleteAuditEventRequest { this.auditEventId = rec.subject })
    }
}

/** A sink and the kind of record it takes. */
class ForwardIncidentSubjectRegistration(val kind: String, val sink: ForwardIncidentSubjectSink)
