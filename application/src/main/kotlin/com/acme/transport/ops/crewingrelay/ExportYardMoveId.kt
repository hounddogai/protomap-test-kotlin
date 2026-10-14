package com.acme.transport.ops.crewingrelay

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.simulateAdmissionRequest
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.peekVideoVisitRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.RejectYardRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.rejectYardRequest
import com.acme.yards.v1.validateYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardMoveValidationService/ValidateYardMove and hands what it keeps to the sink of its kind. */
suspend fun exportYardMoveId(channel: ManagedChannel) {
    val source = YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel)
    val got = source.validateYardMove(validateYardMoveRequest { this.yardMoveId = "live-export-yard-move-id" })
    val rec = ExportYardMoveIdRecord(
        correlationId = got.result.correlationId,
        status = got.outcome.status,
        message = normalize(got.outcome.message),
        resultMessage = normalize(got.result.message),
    )
    actOnExportYardMoveId("admission-calculation", rec, channel)
}

/** Where exportYardMoveId hands each record. */
interface ExportYardMoveIdWriter {
    suspend fun forward(rec: ExportYardMoveIdRecord)
}

/** What exportYardMoveId keeps of each ValidateYardMove response. */
data class ExportYardMoveIdRecord(val correlationId: String, val status: String, val message: String, val resultMessage: String)

/** Passes records to acme.admissions.v1.AdmissionCalculationService. */
class ExportYardMoveIdAdmissionCalculation(private val stub: AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub) : ExportYardMoveIdWriter {
    override suspend fun forward(rec: ExportYardMoveIdRecord) {
        val request = simulateAdmissionRequest {
            this.admissionId = rec.status
            this.reason = rec.correlationId
        }
        stub.simulateAdmission(request)
    }
}

/** Hands records to acme.telehealth.v1.VideoVisitQueryService. */
class ExportYardMoveIdVideoVisitQuery(private val stub: VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub) : ExportYardMoveIdWriter {
    override suspend fun forward(rec: ExportYardMoveIdRecord) {
        stub.peekVideoVisit(peekVideoVisitRequest { this.videoVisitId = rec.resultMessage })
    }
}

/** Writes records to acme.yards.v1.YardApprovalService. */
class ExportYardMoveIdYardApproval(private val stub: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub) : ExportYardMoveIdWriter {
    override suspend fun forward(rec: ExportYardMoveIdRecord) {
        val request = rejectYardRequest {
            this.details = RejectYardRequestKt.details { this.comment = rec.correlationId; this.approverEmail = rec.status }
            this.reason = rec.message
        }
        stub.rejectYard(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnExportYardMoveId(kind: String, rec: ExportYardMoveIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "admission-calculation" to { ExportYardMoveIdAdmissionCalculation(AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(channel)).forward(rec) },
        "video-visit-query" to { ExportYardMoveIdVideoVisitQuery(VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(channel)).forward(rec) },
        "yard-approval" to { ExportYardMoveIdYardApproval(YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
