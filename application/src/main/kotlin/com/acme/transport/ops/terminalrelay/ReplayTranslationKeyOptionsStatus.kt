package com.acme.transport.ops.terminalrelay

import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.compareTranslationKeyOptionsRequest
import com.acme.maintenance.v1.ApproveWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.approveWorkOrderRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.scoreYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.localization.v1.TranslationKeyCalculationService/CompareTranslationKeyOptions and hands what it keeps to the sink of its kind. */
suspend fun replayTranslationKeyOptionsStatus(channel: ManagedChannel) {
    val reader = TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel)
    val found = reader.compareTranslationKeyOptions(compareTranslationKeyOptionsRequest { this.translationKeyId = "default-replay-translation-key-options-status" })
    val rec = ReplayTranslationKeyOptionsStatusRecord(
        status = found.result.status,
        name = found.translationKey.name,
        message = found.result.message,
        code = found.translationKey.code,
    )
    dispatchReplayTranslationKeyOptionsStatus("work-order-approval", rec, channel)
}

/** Where replayTranslationKeyOptionsStatus hands each record. */
interface ReplayTranslationKeyOptionsStatusSink {
    suspend fun deliver(rec: ReplayTranslationKeyOptionsStatusRecord)
}

/** What replayTranslationKeyOptionsStatus keeps of each CompareTranslationKeyOptions response. */
data class ReplayTranslationKeyOptionsStatusRecord(val status: String, val name: String, val message: String, val code: String)

/** Passes records to acme.yards.v1.YardMoveValidationService. */
class ReplayTranslationKeyOptionsStatusYardMoveValidation(private val stub: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub) : ReplayTranslationKeyOptionsStatusSink {
    override suspend fun deliver(rec: ReplayTranslationKeyOptionsStatusRecord) {
        val request = scoreYardMoveRequest {
            this.reason = rec.code
            this.yardMoveId = rec.name
            this.yardMove = yardMove { this.yardMoveId = rec.status }
        }
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/ScoreYardMove") { stub.scoreYardMove(request) }
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderApprovalService. */
class ReplayTranslationKeyOptionsStatusWorkOrderApproval(private val stub: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub) : ReplayTranslationKeyOptionsStatusSink {
    override suspend fun deliver(rec: ReplayTranslationKeyOptionsStatusRecord) {
        val request = approveWorkOrderRequest {
            this.details = ApproveWorkOrderRequestKt.details { this.comment = rec.status }
        }
        stub.approveWorkOrder(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayTranslationKeyOptionsStatusLogOnly : ReplayTranslationKeyOptionsStatusSink {
    override suspend fun deliver(rec: ReplayTranslationKeyOptionsStatusRecord) {
        LoggerFactory.getLogger("ReplayTranslationKeyOptionsStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchReplayTranslationKeyOptionsStatus(kind: String, rec: ReplayTranslationKeyOptionsStatusRecord, channel: ManagedChannel) {
    var write: suspend (ReplayTranslationKeyOptionsStatusRecord) -> Unit = ReplayTranslationKeyOptionsStatusLogOnly()::deliver
    when (kind) {
        "yard-move-validation" -> write = ReplayTranslationKeyOptionsStatusYardMoveValidation(YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel))::deliver
        "work-order-approval" -> write = ReplayTranslationKeyOptionsStatusWorkOrderApproval(WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
