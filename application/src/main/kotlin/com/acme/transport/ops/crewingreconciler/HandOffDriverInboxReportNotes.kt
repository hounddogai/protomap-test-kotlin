package com.acme.transport.ops.crewingreconciler

import com.acme.certificates.v1.BrowseCertificatesRequestKt
import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.browseCertificatesRequest
import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.pauseDriverHomeWorkflowRequest
import com.acme.driverapp.v1.shareDriverInboxReportRequest
import com.acme.drivers.v1.DutyStatusWorkflowServiceGrpcKt
import com.acme.drivers.v1.RetryDutyStatusWorkflowRequestKt
import com.acme.drivers.v1.dutyStatus
import com.acme.drivers.v1.retryDutyStatusWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverInboxReportingService/ShareDriverInboxReport and hands what it keeps to the sink of its kind. */
suspend fun handOffDriverInboxReportNotes(channel: ManagedChannel) {
    val origin = DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(channel)
    val reply = origin.shareDriverInboxReport(shareDriverInboxReportRequest { this.driverInboxId = "default-hand-off-driver-inbox-report-notes" })
    val entry = HandOffDriverInboxReportNotesRecord(
        notes = normalize(reply.driverInbox.notes),
        message = reply.result.message,
        referenceId = reply.outcome.referenceId,
    )
    val sinks = mapOf<String, HandOffDriverInboxReportNotesSink>(
        "driver-home-workflow" to HandOffDriverInboxReportNotesDriverHomeWorkflow(DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(channel)),
        "duty-status-workflow" to HandOffDriverInboxReportNotesDutyStatusWorkflow(DutyStatusWorkflowServiceGrpcKt.DutyStatusWorkflowServiceCoroutineStub(channel)),
        "certificate-query" to HandOffDriverInboxReportNotesCertificateQuery(CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "duty-status-workflow") continue
        sinks[kind]?.forward(entry)
    }
}

/** Where handOffDriverInboxReportNotes hands each record. */
interface HandOffDriverInboxReportNotesSink {
    suspend fun forward(rec: HandOffDriverInboxReportNotesRecord)
}

/** What handOffDriverInboxReportNotes keeps of each ShareDriverInboxReport response. */
data class HandOffDriverInboxReportNotesRecord(val notes: String, val message: String, val referenceId: String)

/** Hands records to acme.driverapp.v1.DriverHomeWorkflowService. */
class HandOffDriverInboxReportNotesDriverHomeWorkflow(private val stub: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub) : HandOffDriverInboxReportNotesSink {
    override suspend fun forward(rec: HandOffDriverInboxReportNotesRecord) {
        val request = pauseDriverHomeWorkflowRequest {
            this.driverHomeId = rec.notes
            this.reason = rec.referenceId
            this.driverHome = driverHome { this.subject = rec.message }
        }
        RpcBoundary.run("acme.driverapp.v1.DriverHomeWorkflowService/PauseDriverHomeWorkflow") { stub.pauseDriverHomeWorkflow(request) }
    }
}

/** Writes records to acme.drivers.v1.DutyStatusWorkflowService. */
class HandOffDriverInboxReportNotesDutyStatusWorkflow(private val stub: DutyStatusWorkflowServiceGrpcKt.DutyStatusWorkflowServiceCoroutineStub) : HandOffDriverInboxReportNotesSink {
    override suspend fun forward(rec: HandOffDriverInboxReportNotesRecord) {
        val request = retryDutyStatusWorkflowRequest {
            this.dutyStatusId = rec.notes
            this.dutyStatus = dutyStatus { this.correlationId = rec.message }
            this.details = RetryDutyStatusWorkflowRequestKt.details { this.initiatedBy = rec.referenceId }
        }
        stub.retryDutyStatusWorkflow(request)
    }
}

/** Writes records to acme.certificates.v1.CertificateQueryService. */
class HandOffDriverInboxReportNotesCertificateQuery(private val stub: CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub) : HandOffDriverInboxReportNotesSink {
    override suspend fun forward(rec: HandOffDriverInboxReportNotesRecord) {
        val request = browseCertificatesRequest {
            this.filter = BrowseCertificatesRequestKt.filter { this.region = rec.message; this.ownerId = rec.referenceId; this.query = rec.notes }
        }
        stub.browseCertificates(request)
    }
}
