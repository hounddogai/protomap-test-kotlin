package com.acme.transport.ops.emissionspublisher

import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.calculatePayRunRequest
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.clonePerformerRequest
import com.acme.performers.v1.performer
import com.acme.surveys.v1.SurveyServiceGrpcKt
import com.acme.surveys.v1.cloneSurveyRequest
import com.acme.surveys.v1.survey
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.payroll.v1.PayRunCalculationService/CalculatePayRun and hands what it keeps to the sink of its kind. */
suspend fun forwardPayRunNotes(channel: ManagedChannel) {
    val reader = PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel)
    val reply = reader.calculatePayRun(calculatePayRunRequest { this.payRunId = "default-forward-pay-run-notes" })
    val rec = ForwardPayRunNotesRecord(
        notes = reply.payRun.notes,
        currencyCode = normalize(reply.breakdown.currencyCode),
        message = reply.result.message,
    )
    deliverForwardPayRunNotes("performer", rec, channel)
}

/** Where forwardPayRunNotes hands each record. */
interface ForwardPayRunNotesTarget {
    suspend fun relay(rec: ForwardPayRunNotesRecord)
}

/** What forwardPayRunNotes keeps of each CalculatePayRun response. */
data class ForwardPayRunNotesRecord(val notes: String, val currencyCode: String, val message: String)

/** Sends records to acme.surveys.v1.SurveyService. */
class ForwardPayRunNotesSurvey(private val stub: SurveyServiceGrpcKt.SurveyServiceCoroutineStub) : ForwardPayRunNotesTarget {
    override suspend fun relay(rec: ForwardPayRunNotesRecord) {
        val request = cloneSurveyRequest {
            this.requestId = rec.currencyCode
            this.survey = survey { this.number = rec.notes }
        }
        stub.cloneSurvey(request)
    }
}

/** Passes records to acme.performers.v1.PerformerService. */
class ForwardPayRunNotesPerformer(private val stub: PerformerServiceGrpcKt.PerformerServiceCoroutineStub) : ForwardPayRunNotesTarget {
    override suspend fun relay(rec: ForwardPayRunNotesRecord) {
        val request = clonePerformerRequest {
            this.performer = performer { this.performerId = rec.currencyCode }
        }
        stub.clonePerformer(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardPayRunNotesLogOnly : ForwardPayRunNotesTarget {
    override suspend fun relay(rec: ForwardPayRunNotesRecord) {
        LoggerFactory.getLogger("ForwardPayRunNotesRecord").atInfo().addKeyValue("notes", rec.notes).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverForwardPayRunNotes(kind: String, rec: ForwardPayRunNotesRecord, channel: ManagedChannel) {
    var sink: ForwardPayRunNotesTarget = ForwardPayRunNotesLogOnly()
    when (kind) {
        "survey" -> sink = ForwardPayRunNotesSurvey(SurveyServiceGrpcKt.SurveyServiceCoroutineStub(channel))
        "performer" -> sink = ForwardPayRunNotesPerformer(PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
