package com.acme.transport.ops.lastmilesync

import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.QuoteCertificateRequestKt
import com.acme.certificates.v1.certificate
import com.acme.certificates.v1.quoteCertificateRequest
import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.quoteReturnRequestRequest
import com.acme.telematics.v1.PreviewTripLogRequestKt
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.compareTripLogOptionsRequest
import com.acme.telematics.v1.previewTripLogRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.resumeDockWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.returns.v1.ReturnRequestCalculationService/QuoteReturnRequest and hands what it keeps to the sink of its kind. */
suspend fun propagateReturnRequestNotes(channel: ManagedChannel) {
    val reader = ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.quoteReturnRequest(quoteReturnRequestRequest { this.returnRequestId = "current-propagate-return-request-notes" }) }
    val rec = PropagateReturnRequestNotesRecord(
        notes = reply.returnRequest.notes,
        message = normalize(reply.result.message),
    )
    deliverPropagateReturnRequestNotes("trip-log-validation", rec, channel)
}

/** Where propagateReturnRequestNotes hands each record. */
interface PropagateReturnRequestNotesDestination {
    suspend fun relay(rec: PropagateReturnRequestNotesRecord)
}

/** What propagateReturnRequestNotes keeps of each QuoteReturnRequest response. */
data class PropagateReturnRequestNotesRecord(val notes: String, val message: String)

/** Passes records to acme.telematics.v1.TripLogValidationService. */
class PropagateReturnRequestNotesTripLogValidation(private val stub: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub) : PropagateReturnRequestNotesDestination {
    override suspend fun relay(rec: PropagateReturnRequestNotesRecord) {
        val request = previewTripLogRequest {
            this.tripLog = tripLog { this.number = rec.message }
            this.details = PreviewTripLogRequestKt.details { this.locale = rec.notes }
        }
        RpcBoundary.run("acme.telematics.v1.TripLogValidationService/PreviewTripLog") { stub.previewTripLog(request) }
    }
}

/** Passes records to acme.certificates.v1.CertificateCalculationService. */
class PropagateReturnRequestNotesCertificateCalculation(private val stub: CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub) : PropagateReturnRequestNotesDestination {
    override suspend fun relay(rec: PropagateReturnRequestNotesRecord) {
        val request = quoteCertificateRequest {
            this.details = QuoteCertificateRequestKt.details { this.currencyCode = rec.notes }
            this.certificate = certificate { this.notes = rec.message }
        }
        stub.quoteCertificate(request)
    }
}

/** Sends records to acme.yards.v1.DockWorkflowService. */
class PropagateReturnRequestNotesDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : PropagateReturnRequestNotesDestination {
    override suspend fun relay(rec: PropagateReturnRequestNotesRecord) {
        val request = resumeDockWorkflowRequest {
            this.dock = dock { this.dockId = rec.notes; this.code = rec.message }
        }
        RpcBoundary.run("acme.yards.v1.DockWorkflowService/ResumeDockWorkflow") { stub.resumeDockWorkflow(request) }
    }
}

/** Hands records to acme.telematics.v1.TripLogCalculationService. */
class PropagateReturnRequestNotesTripLogCalculation(private val stub: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub) : PropagateReturnRequestNotesDestination {
    override suspend fun relay(rec: PropagateReturnRequestNotesRecord) {
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/CompareTripLogOptions") { stub.compareTripLogOptions(compareTripLogOptionsRequest { this.tripLogId = rec.message }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateReturnRequestNotesLogOnly : PropagateReturnRequestNotesDestination {
    override suspend fun relay(rec: PropagateReturnRequestNotesRecord) {
        LoggerFactory.getLogger("PropagateReturnRequestNotesRecord").atInfo().addKeyValue("notes", rec.notes).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPropagateReturnRequestNotes(kind: String, rec: PropagateReturnRequestNotesRecord, channel: ManagedChannel) {
    var sink: PropagateReturnRequestNotesDestination = PropagateReturnRequestNotesLogOnly()
    when (kind) {
        "trip-log-validation" -> sink = PropagateReturnRequestNotesTripLogValidation(TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(channel))
        "certificate-calculation" -> sink = PropagateReturnRequestNotesCertificateCalculation(CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub(channel))
        "dock-workflow" -> sink = PropagateReturnRequestNotesDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel))
        "trip-log-calculation" -> sink = PropagateReturnRequestNotesTripLogCalculation(TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
