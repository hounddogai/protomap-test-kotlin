package com.acme.transport.ops.emissionspublisher

import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.explainDutyStatusDecisionRequest
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.compareCryptoKeyOptionsRequest
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.signalServiceScheduleWorkflowRequest
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.peekSecretRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleWorkflowService/SignalServiceScheduleWorkflow and hands what it keeps to the sink of its kind. */
suspend fun backfillServiceScheduleWorkflowStatus(channel: ManagedChannel) {
    val upstream = ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.signalServiceScheduleWorkflow(signalServiceScheduleWorkflowRequest { this.serviceScheduleId = "live-backfill-service-schedule-workflow-status" }) }
    val row = BackfillServiceScheduleWorkflowStatusRecord(
        status = normalize(current.result.status),
        title = normalize(current.serviceSchedule.title),
    )
    actOnBackfillServiceScheduleWorkflowStatus("secret-query", row, channel)
}

/** Where backfillServiceScheduleWorkflowStatus hands each record. */
interface BackfillServiceScheduleWorkflowStatusDestination {
    suspend fun hand(rec: BackfillServiceScheduleWorkflowStatusRecord)
}

/** What backfillServiceScheduleWorkflowStatus keeps of each SignalServiceScheduleWorkflow response. */
data class BackfillServiceScheduleWorkflowStatusRecord(val status: String, val title: String)

/** Sends records to acme.keymanagement.v1.CryptoKeyCalculationService. */
class BackfillServiceScheduleWorkflowStatusCryptoKeyCalculation(private val stub: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub) : BackfillServiceScheduleWorkflowStatusDestination {
    override suspend fun hand(rec: BackfillServiceScheduleWorkflowStatusRecord) {
        stub.compareCryptoKeyOptions(compareCryptoKeyOptionsRequest { this.cryptoKeyId = rec.status })
    }
}

/** Sends records to acme.drivers.v1.DutyStatusValidationService. */
class BackfillServiceScheduleWorkflowStatusDutyStatusValidation(private val stub: DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub) : BackfillServiceScheduleWorkflowStatusDestination {
    override suspend fun hand(rec: BackfillServiceScheduleWorkflowStatusRecord) {
        RpcBoundary.run("acme.drivers.v1.DutyStatusValidationService/ExplainDutyStatusDecision") { stub.explainDutyStatusDecision(explainDutyStatusDecisionRequest { this.dutyStatusId = rec.title }) }
    }
}

/** Sends records to acme.secrets.v1.SecretQueryService. */
class BackfillServiceScheduleWorkflowStatusSecretQuery(private val stub: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub) : BackfillServiceScheduleWorkflowStatusDestination {
    override suspend fun hand(rec: BackfillServiceScheduleWorkflowStatusRecord) {
        stub.peekSecret(peekSecretRequest { this.secretId = rec.title })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnBackfillServiceScheduleWorkflowStatus(kind: String, rec: BackfillServiceScheduleWorkflowStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "crypto-key-calculation" to { BackfillServiceScheduleWorkflowStatusCryptoKeyCalculation(CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(channel)).hand(rec) },
        "duty-status-validation" to { BackfillServiceScheduleWorkflowStatusDutyStatusValidation(DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub(channel)).hand(rec) },
        "secret-query" to { BackfillServiceScheduleWorkflowStatusSecretQuery(SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
